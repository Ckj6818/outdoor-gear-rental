package com.outdoor.rental.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import jakarta.annotation.PostConstruct;

/**
 * 启动时尝试开启 Redis 键过期事件通知（{@code notify-keyspace-events Ex}）。
 * <p>
 * 若 Redis 禁止运行时 CONFIG（如云托管 Redis），请手动配置后关闭 {@code rental.order.auto-config-redis-notify}。
 * </p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rental.order.auto-config-redis-notify", havingValue = "true", matchIfMissing = true)
public class RedisKeyspaceEventsInitializer {

    private final RedisConnectionFactory redisConnectionFactory;

    @PostConstruct
    public void enableKeyspaceNotifications() {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            connection.serverCommands().setConfig("notify-keyspace-events", "Ex");
            log.info("Redis notify-keyspace-events 已设置为 Ex（键过期事件）");
        } catch (Exception ex) {
            log.warn("无法自动设置 Redis notify-keyspace-events，请手动执行: CONFIG SET notify-keyspace-events Ex");
        }
    }
}
