package com.outdoor.rental.service;

import com.outdoor.rental.common.PageResult;
import com.outdoor.rental.dto.CreateRentalOrderDTO;
import com.outdoor.rental.dto.InspectOrderDTO;
import com.outdoor.rental.dto.RentalOrderQueryDTO;
import com.outdoor.rental.entity.RentalOrder;
import com.outdoor.rental.vo.OccupiedDateRangeVO;

import java.time.LocalDate;
import java.util.List;

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

    /**
     * 查询某装备已被占用的租赁档期列表。
     */
    List<OccupiedDateRangeVO> listOccupiedDates(Long gearId);

    /**
     * 校验租赁档期是否可用；与已有订单档期冲突时抛出业务异常。
     * <p>
     * 边界规则：归还日当天不可作为下一单起租日（半开区间按「含首含尾」占用处理）。
     * </p>
     *
     * @return 档期可用时返回 {@code true}
     */
    boolean checkDateAvailable(Long gearId, LocalDate startDate, LocalDate endDate);
}
