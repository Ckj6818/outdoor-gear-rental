package com.outdoor.rental.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/**
 * Redis 不可用时降级：缓存读写失败仅记录日志，不阻断业务（回源数据库）。
 */
@Slf4j
public class LoggingCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn("缓存读取失败，回源数据库: cache={}, key={}, reason={}",
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.warn("缓存写入失败，已忽略: cache={}, key={}, reason={}",
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.warn("缓存淘汰失败，已忽略: cache={}, key={}, reason={}",
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.warn("缓存清空失败，已忽略: cache={}, reason={}", cache.getName(), exception.getMessage());
    }
}
