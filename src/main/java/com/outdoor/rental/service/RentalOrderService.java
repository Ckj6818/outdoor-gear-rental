package com.outdoor.rental.service;

import com.outdoor.rental.common.PageResult;
import com.outdoor.rental.dto.CreateRentalOrderDTO;
import com.outdoor.rental.dto.InspectOrderDTO;
import com.outdoor.rental.dto.RentalOrderQueryDTO;
import com.outdoor.rental.entity.RentalOrder;

public interface RentalOrderService {

    PageResult<RentalOrder> pageQuery(RentalOrderQueryDTO query);

    /**
     * 管理员全量订单分页查询
     */
    PageResult<RentalOrder> adminPageQuery(RentalOrderQueryDTO query);

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
     * 归还装备：订单进入待质检，装备实例同步进入待质检（不恢复可用库存）
     */
    RentalOrder returnGear(Long orderId);

    /**
     * 管理员质检：通过则完结并恢复库存，异常则进入赔偿流程
     */
    RentalOrder inspectOrder(InspectOrderDTO dto);

    void update(RentalOrder rentalOrder);

    void deleteById(Long id);
}
