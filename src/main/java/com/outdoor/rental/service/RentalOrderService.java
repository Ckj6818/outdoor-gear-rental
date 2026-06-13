package com.outdoor.rental.service;

import com.outdoor.rental.common.PageResult;
import com.outdoor.rental.dto.CreateRentalOrderDTO;
import com.outdoor.rental.dto.RentalOrderQueryDTO;
import com.outdoor.rental.entity.RentalOrder;

public interface RentalOrderService {

    PageResult<RentalOrder> pageQuery(RentalOrderQueryDTO query);

    RentalOrder getById(Long id);

    /**
     * 高并发抢租下单：Redis 分布式锁 + 事务扣库存
     */
    RentalOrder createRentalOrder(CreateRentalOrderDTO dto);

    /**
     * 模拟支付：待支付 -> 借出中
     */
    RentalOrder payOrder(Long orderId);

    /**
     * 归还装备：恢复库存并更新订单为已归还
     */
    RentalOrder returnGear(Long orderId);

    void update(RentalOrder rentalOrder);

    void deleteById(Long id);
}
