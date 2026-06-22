package com.outdoor.rental.service.impl;

import com.outdoor.rental.config.OrderCancelRedisProperties;
import com.outdoor.rental.service.OrderCancelRedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OrderCancelRedisServiceImpl implements OrderCancelRedisService {

    private final OrderCancelRedisProperties properties;
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public OrderCancelRedisServiceImpl(
            OrderCancelRedisProperties properties,
            @Autowired(required = false) StringRedisTemplate stringRedisTemplate) {
        this.properties = properties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void scheduleAutoCancel(Long orderId) {
        if (orderId == null || !isRedisAvailable()) {
            return;
        }
        String key = KEY_PREFIX + orderId;
        int timeoutMinutes = Math.max(properties.getPayTimeoutMinutes(), 1);
        stringRedisTemplate.opsForValue().set(key, String.valueOf(orderId), timeoutMinutes, TimeUnit.MINUTES);
        log.info("已注册订单超时取消计时 orderId={} ttl={}min key={}", orderId, timeoutMinutes, key);
    }

    @Override
    public void removeAutoCancelTimer(Long orderId) {
        if (orderId == null || !isRedisAvailable()) {
            return;
        }
        Boolean deleted = stringRedisTemplate.delete(KEY_PREFIX + orderId);
        log.debug("移除订单超时取消计时 orderId={} deleted={}", orderId, deleted);
    }

    @Override
    public boolean isOrderCancelKey(String redisKey) {
        return redisKey != null && redisKey.startsWith(KEY_PREFIX);
    }

    @Override
    public Long parseOrderId(String redisKey) {
        if (!isOrderCancelKey(redisKey)) {
            return null;
        }
        try {
            return Long.parseLong(redisKey.substring(KEY_PREFIX.length()));
        } catch (NumberFormatException ex) {
            log.warn("无法解析订单超时 Key: {}", redisKey);
            return null;
        }
    }

    private boolean isRedisAvailable() {
        if (stringRedisTemplate == null) {
            log.warn("StringRedisTemplate 未就绪，跳过订单超时取消计时");
            return false;
        }
        return true;
    }
}
