package com.outdoor.rental.service.impl;

import com.outdoor.rental.entity.RentalOrder;
import com.outdoor.rental.enums.RentalOrderStatusEnum;
import com.outdoor.rental.mapper.GearInfoMapper;
import com.outdoor.rental.mapper.GearItemMapper;
import com.outdoor.rental.mapper.RentalOrderMapper;
import com.outdoor.rental.service.GearInfoService;
import com.outdoor.rental.service.RedisGearStockService;
import com.outdoor.rental.service.RentalOrderCancelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalOrderCancelServiceImpl implements RentalOrderCancelService {

    private final RentalOrderMapper rentalOrderMapper;
    private final GearItemMapper gearItemMapper;
    private final GearInfoMapper gearInfoMapper;
    private final RedisGearStockService redisGearStockService;
    private final GearInfoService gearInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelUnpaidOrderIfNeeded(Long orderId) {
        if (orderId == null) {
            return;
        }

        int updated = rentalOrderMapper.cancelIfPendingPayment(
                orderId,
                RentalOrderStatusEnum.CANCELLED.getCode(),
                RentalOrderStatusEnum.PENDING_PAYMENT.getCode(),
                "超时未支付自动取消"
        );
        if (updated == 0) {
            log.info("订单 {} 非待支付状态，跳过超时取消（可能已支付或已取消）", orderId);
            return;
        }

        RentalOrder order = rentalOrderMapper.selectById(orderId);
        if (order == null) {
            return;
        }

        if (order.getItemId() != null) {
            int itemRows = gearItemMapper.markAsAvailableFromRenting(order.getItemId());
            if (itemRows == 0) {
                log.warn("订单 {} 超时取消：装备实例 {} 状态未从租赁中回滚", orderId, order.getItemId());
            }
        }

        if (order.getGearId() != null) {
            int stockRows = gearInfoMapper.restoreAvailableStock(order.getGearId());
            if (stockRows == 0) {
                log.warn("订单 {} 超时取消：MySQL 可用库存恢复未生效 gearId={}", orderId, order.getGearId());
            }
            redisGearStockService.restoreStock(order.getGearId(), 1);
            gearInfoService.evictPageCache();
        }

        log.info("订单 {} [{}] 超时未支付已自动取消，库存已回滚", orderId, order.getOrderNo());
    }
}
