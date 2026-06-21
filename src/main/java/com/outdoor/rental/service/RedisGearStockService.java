package com.outdoor.rental.service;

import com.outdoor.rental.entity.GearInfo;
import com.outdoor.rental.exception.BusinessException;
import com.outdoor.rental.mapper.GearInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 基于 Redis + Lua 的装备可用库存预扣减服务。
 * <p>
 * 与 {@link RedisLockService} 配合使用：分布式锁保证同一 gearId 的下单请求串行进入临界区，
 * Lua 脚本在 Redis 内原子完成「读库存 → 判断 → 扣减」，避免 check-then-act 竞态导致超卖。
 * </p>
 */
@Slf4j
@Service
public class RedisGearStockService {

    /** Redis 中可用库存 Key 前缀，完整 Key = gear:stock:{gearId} */
    public static final String STOCK_KEY_PREFIX = "gear:stock:";

    /**
     * Lua 预扣减脚本（在 Redis 单线程模型中原子执行，保证查询与扣减不可分割）。
     * <p>
     * KEYS[1] = 库存 Key<br>
     * ARGV[1] = 扣减数量<br>
     * 返回值：1=成功，0=库存不足，-1=Key 未初始化（需从 MySQL 同步后再试）
     * </p>
     */
    private static final String LUA_PRE_DEDUCT = """
            local stock = redis.call('GET', KEYS[1])
            if stock == false then
                return -1
            end
            stock = tonumber(stock)
            local amount = tonumber(ARGV[1])
            if stock < amount then
                return 0
            end
            redis.call('DECRBY', KEYS[1], amount)
            return 1
            """;

    /**
     * Lua 回滚脚本：MySQL 事务失败时归还 Redis 预扣减的库存，防止 Redis 与 DB 长期不一致。
     */
    private static final String LUA_ROLLBACK = """
            if redis.call('EXISTS', KEYS[1]) == 1 then
                redis.call('INCRBY', KEYS[1], ARGV[1])
            end
            return 1
            """;

    private static final long PRE_DEDUCT_SUCCESS = 1L;
    private static final long PRE_DEDUCT_INSUFFICIENT = 0L;
    private static final long PRE_DEDUCT_NOT_INITIALIZED = -1L;

    private final StringRedisTemplate redisTemplate;
    private final GearInfoMapper gearInfoMapper;

    private final DefaultRedisScript<Long> preDeductScript;
    private final DefaultRedisScript<Long> rollbackScript;

    @Autowired
    public RedisGearStockService(@Autowired(required = false) StringRedisTemplate redisTemplate,
                                 GearInfoMapper gearInfoMapper) {
        this.redisTemplate = redisTemplate;
        this.gearInfoMapper = gearInfoMapper;
        this.preDeductScript = new DefaultRedisScript<>(LUA_PRE_DEDUCT, Long.class);
        this.rollbackScript = new DefaultRedisScript<>(LUA_ROLLBACK, Long.class);
        if (redisTemplate == null) {
            log.warn("StringRedisTemplate 未就绪，Redis 库存预扣减已禁用，将仅依赖 MySQL 行锁与条件 UPDATE");
        }
    }

    /** Redis 是否可用（开发环境 Redis 未启动时可降级为纯 DB 路径） */
    public boolean isAvailable() {
        return redisTemplate != null;
    }

    /**
     * 尝试预扣减 Redis 可用库存。
     *
     * @return true 表示预扣减成功；false 表示库存不足
     */
    public boolean tryPreDeduct(Long gearId, int amount) {
        if (!isAvailable()) {
            // 降级：跳过 Redis 预扣减，由 MySQL 事务内的 deductAvailableStock 兜底
            return true;
        }

        String key = stockKey(gearId);
        Long result = executePreDeductScript(key, amount);

        // Key 不存在时，从 MySQL 懒加载可用库存并 SETNX，再重试一次 Lua 扣减
        if (result != null && result == PRE_DEDUCT_NOT_INITIALIZED) {
            syncStockFromDb(gearId);
            result = executePreDeductScript(key, amount);
        }

        if (result == null || result == PRE_DEDUCT_INSUFFICIENT) {
            return false;
        }
        return result == PRE_DEDUCT_SUCCESS;
    }

    /**
     * 回滚 Redis 预扣减（在 MySQL 本地事务失败时调用，补偿 Redis 侧已扣减的库存）。
     */
    public void rollbackPreDeduct(Long gearId, int amount) {
        if (!isAvailable()) {
            return;
        }
        try {
            redisTemplate.execute(
                    rollbackScript,
                    Collections.singletonList(stockKey(gearId)),
                    String.valueOf(amount)
            );
            log.debug("Redis 库存回滚成功 gearId={} amount={}", gearId, amount);
        } catch (Exception ex) {
            // 回滚失败需告警人工对账，但不吞掉原始业务异常
            log.error("Redis 库存回滚失败 gearId={} amount={}，请人工核对 Redis 与 MySQL 库存", gearId, amount, ex);
        }
    }

    /**
     * 质检通过后恢复 Redis 库存（与 MySQL restoreAvailableStock 保持一致）。
     */
    public void restoreStock(Long gearId, int amount) {
        rollbackPreDeduct(gearId, amount);
    }

    private Long executePreDeductScript(String key, int amount) {
        try {
            return redisTemplate.execute(
                    preDeductScript,
                    Collections.singletonList(key),
                    String.valueOf(amount)
            );
        } catch (Exception ex) {
            // Redis 短暂不可用：降级为 MySQL 单路径，避免因缓存层故障阻断下单
            log.warn("Redis 预扣减异常，降级为 MySQL 库存校验 gearId={}", key, ex);
            return PRE_DEDUCT_SUCCESS;
        }
    }

    /**
     * 从 MySQL gear_info.available_stock 懒同步到 Redis。
     * 使用 SETNX（setIfAbsent）避免覆盖并发线程已扣减过的 Redis 库存。
     */
    private void syncStockFromDb(Long gearId) {
        GearInfo gear = gearInfoMapper.selectById(gearId);
        if (gear == null) {
            throw new BusinessException(400, "装备不存在");
        }
        int available = gear.getAvailableStock() != null ? gear.getAvailableStock() : 0;
        redisTemplate.opsForValue().setIfAbsent(stockKey(gearId), String.valueOf(available));
    }

    private String stockKey(Long gearId) {
        return STOCK_KEY_PREFIX + gearId;
    }
}
