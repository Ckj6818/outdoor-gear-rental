package com.outdoor.rental.listener;

import com.outdoor.rental.service.OrderCancelRedisService;
import com.outdoor.rental.service.RentalOrderCancelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * 处理 Redis Key 过期消息：{@code order:cancel:{orderId}} 过期时触发待支付订单自动取消。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelExpirationListener implements MessageListener {

    private final OrderCancelRedisService orderCancelRedisService;
    private final RentalOrderCancelService rentalOrderCancelService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        if (!orderCancelRedisService.isOrderCancelKey(expiredKey)) {
            return;
        }

        Long orderId = orderCancelRedisService.parseOrderId(expiredKey);
        if (orderId == null) {
            return;
        }

        log.info("检测到订单超时 Key 过期 key={} orderId={}", expiredKey, orderId);
        try {
            rentalOrderCancelService.cancelUnpaidOrderIfNeeded(orderId);
        } catch (Exception ex) {
            log.error("订单超时自动取消失败 orderId={}", orderId, ex);
        }
    }
}
