package com.outdoor.rental.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 待支付订单超时自动取消配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "rental.order")
public class OrderCancelRedisProperties {

    /** 待支付超时时间（分钟），默认 15 分钟 */
    private int payTimeoutMinutes = 15;

    /** 是否启用 Redis 键过期监听实现超时取消（Redis 未启动时可设为 false） */
    private boolean timeoutCancelEnabled = true;
}
