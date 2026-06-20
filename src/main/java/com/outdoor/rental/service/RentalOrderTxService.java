package com.outdoor.rental.service;

import com.outdoor.rental.dto.CreateRentalOrderDTO;
import com.outdoor.rental.entity.RentalOrder;

/**
 * 租赁下单事务服务：分配装备实例 + 扣减库存 + 创建订单在同一事务内完成
 */
public interface RentalOrderTxService {

    RentalOrder createOrderInTransaction(Long userId, CreateRentalOrderDTO dto);
}
