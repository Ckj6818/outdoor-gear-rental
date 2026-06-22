package com.outdoor.rental.service;

/**
 * 待支付订单 Redis 超时 Key 管理（order:cancel:{orderId}）。
 */
public interface OrderCancelRedisService {

    /** Redis Key 前缀：order:cancel:{orderId} */
    String KEY_PREFIX = "order:cancel:";

    /**
     * 下单成功后写入 15 分钟过期的取消计时 Key。
     */
    void scheduleAutoCancel(Long orderId);

    /**
     * 支付成功或手动取消时删除计时 Key，避免误触发过期取消。
     */
    void removeAutoCancelTimer(Long orderId);

    /**
     * 判断是否为订单超时取消 Key。
     */
    boolean isOrderCancelKey(String redisKey);

    /**
     * 从 Key 中解析订单 ID。
     */
    Long parseOrderId(String redisKey);
}
