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
 * 分布式锁服务：优先 Redisson；Redis 不可用时降级为 JVM 本地锁（开发环境）
 */
@Slf4j
@Service
public class RedisLockService {

    private static final String LOCK_PREFIX = "gear:rent:lock:";

    private final RedissonClient redissonClient;
    private final ConcurrentHashMap<Long, ReentrantLock> localLocks = new ConcurrentHashMap<>();

    @Autowired
    public RedisLockService(@Autowired(required = false) RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        if (redissonClient == null) {
            log.warn("RedissonClient 未就绪，分布式锁已降级为本地锁（仅适用于单机开发环境）");
        }
    }

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
            locked = lock.tryLock(waitSeconds, leaseSeconds, TimeUnit.SECONDS);
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
