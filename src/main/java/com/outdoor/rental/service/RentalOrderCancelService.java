package com.outdoor.rental.service;

/**
 * 待支付订单超时取消与库存回滚。
 */
public interface RentalOrderCancelService {

    /**
     * 若订单仍为待支付，则取消订单并释放库存（幂等安全）。
     */
    void cancelUnpaidOrderIfNeeded(Long orderId);
}
