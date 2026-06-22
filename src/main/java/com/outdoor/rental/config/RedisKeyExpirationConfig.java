package com.outdoor.rental.config;

import com.outdoor.rental.listener.OrderCancelExpirationListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * 应用启动完成后再注册 Redis 键过期监听，避免 Redis 未启动时阻塞 Spring Boot 启动。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rental.order", name = "timeout-cancel-enabled", havingValue = "true", matchIfMissing = true)
public class RedisKeyExpirationConfig {

    private static final PatternTopic EXPIRED_TOPIC = new PatternTopic("__keyevent@*__:expired");

    @Bean
    ApplicationListener<ApplicationReadyEvent> orderCancelExpirationRegistrar(
            RedisConnectionFactory connectionFactory,
            OrderCancelExpirationListener orderCancelExpirationListener) {
        return event -> startExpirationListener(connectionFactory, orderCancelExpirationListener);
    }

    private void startExpirationListener(
            RedisConnectionFactory connectionFactory,
            OrderCancelExpirationListener listener) {
        try {
            connectionFactory.getConnection().ping();
            RedisMessageListenerContainer container = new RedisMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);
            container.addMessageListener(listener, EXPIRED_TOPIC);
            container.start();
            log.info("订单超时取消监听已启动，订阅 {}", EXPIRED_TOPIC.getTopic());
        } catch (Exception ex) {
            log.warn("Redis 未就绪，订单超时自动取消监听未启动（应用仍可运行）。"
                    + "请启动 Redis（localhost:6379）后重启后端，或设置 rental.order.timeout-cancel-enabled=false");
        }
    }
}
