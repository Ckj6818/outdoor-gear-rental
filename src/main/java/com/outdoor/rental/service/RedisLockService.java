package com.outdoor.rental.service;

import com.outdoor.rental.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 基于 Redisson 的分布式锁服务（底层实现 RLock，等价于增强版 SETNX）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLockService {

    private static final String LOCK_PREFIX = "gear:rent:lock:";

    private final RedissonClient redissonClient;

    public <T> T executeWithLock(Long gearId, long waitSeconds, long leaseSeconds, Supplier<T> supplier) {
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
}
