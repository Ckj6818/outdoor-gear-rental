package com.outdoor.rental.service;

import com.outdoor.rental.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * 分布式锁服务：优先使用 Redisson 实现 Redis 分布式锁；Redis 不可用时降级为 JVM 本地锁（单机开发环境）。
 * <p>
 * 租赁下单场景以 <b>装备 ID（gearId）</b> 为锁粒度，使同一装备的并发抢租请求串行执行，
 * 降低 Redis Lua 预扣减与 MySQL 事务被大量并发同时冲击的概率。
 * </p>
 *
 * <h3>死锁与锁泄漏防范</h3>
 * <ul>
 *   <li><b>waitSeconds</b>：获取锁的最大等待时间，超时返回 429，避免线程无限阻塞形成「活锁」。</li>
 *   <li><b>leaseSeconds</b>：锁自动过期时间（Redisson 看门狗可续期），防止持锁进程崩溃后锁永不释放导致<b>死锁</b>。</li>
 *   <li><b>finally + isHeldByCurrentThread()</b>：仅在当前线程确实持有锁时才 unlock，
 *       防止误释放其他线程/节点的锁，也避免对未成功加锁的 unlock 引发 IllegalMonitorStateException。</li>
 *   <li><b>tryLock 而非 lock()</b>：不使用阻塞式 lock()，避免在锁竞争激烈时大量线程永久挂起。</li>
 * </ul>
 */
@Slf4j
@Service
public class RedisLockService {

    /** 分布式锁 Key 前缀，完整 Key = gear:rent:lock:{gearId} */
    private static final String LOCK_PREFIX = "gear:rent:lock:";

    private final RedissonClient redissonClient;

    /** Redis 不可用时的本地锁降级表，按 gearId 隔离 */
    private final ConcurrentHashMap<Long, ReentrantLock> localLocks = new ConcurrentHashMap<>();

    @Autowired
    public RedisLockService(@Autowired(required = false) RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        if (redissonClient == null) {
            log.warn("RedissonClient 未就绪，分布式锁已降级为本地锁（仅适用于单机开发环境）");
        }
    }

    /**
     * 在装备级分布式锁保护下执行业务逻辑。
     *
     * @param gearId       装备 ID，作为锁 Key 的一部分
     * @param waitSeconds  获取锁最长等待秒数
     * @param leaseSeconds 锁持有最长秒数（过期自动释放，防死锁）
     * @param supplier     临界区业务逻辑（如 Redis 预扣减 + MySQL 下单事务）
     */
    public <T> T executeWithLock(Long gearId, long waitSeconds, long leaseSeconds, Supplier<T> supplier) {
        if (redissonClient != null) {
            return executeWithRedissonLock(gearId, waitSeconds, leaseSeconds, supplier);
        }
        return executeWithLocalLock(gearId, waitSeconds, supplier);
    }

    private <T> T executeWithRedissonLock(Long gearId, long waitSeconds, long leaseSeconds, Supplier<T> supplier) {
        String lockKey = LOCK_PREFIX + gearId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            // tryLock(wait, lease)：限时获取 + 自动过期，双重机制防范死锁
            locked = lock.tryLock(waitSeconds, leaseSeconds, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException(429, "系统繁忙，请稍后重试");
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(500, "抢租被中断，请重试");
        } finally {
            // 只释放「本线程、本次成功获取」的锁，绝不释放他人持有的锁
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private <T> T executeWithLocalLock(Long gearId, long waitSeconds, Supplier<T> supplier) {
        ReentrantLock lock = localLocks.computeIfAbsent(gearId, id -> new ReentrantLock());
        boolean locked = false;
        try {
            locked = lock.tryLock(waitSeconds, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException(429, "系统繁忙，请稍后重试");
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(500, "抢租被中断，请重试");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
