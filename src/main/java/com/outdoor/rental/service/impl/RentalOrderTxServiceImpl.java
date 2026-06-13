package com.outdoor.rental.service.impl;

import com.outdoor.rental.dto.CreateRentalOrderDTO;
import com.outdoor.rental.entity.GearInfo;
import com.outdoor.rental.entity.RentalOrder;
import com.outdoor.rental.exception.BusinessException;
import com.outdoor.rental.mapper.GearInfoMapper;
import com.outdoor.rental.mapper.RentalOrderMapper;
import com.outdoor.rental.service.RentalOrderTxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class RentalOrderTxServiceImpl implements RentalOrderTxService {

    private final GearInfoMapper gearInfoMapper;
    private final RentalOrderMapper rentalOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RentalOrder createOrderInTransaction(Long userId, CreateRentalOrderDTO dto) {
        GearInfo gearInfo = gearInfoMapper.selectById(dto.getGearId());
        if (gearInfo == null) {
            throw new BusinessException(400, "装备不存在");
        }
        if (gearInfo.getStatus() == null || gearInfo.getStatus() != 1) {
            throw new BusinessException(400, "装备已下架，无法租赁");
        }

        // 原子扣减库存：WHERE available_stock > 0 防止超卖
        int affectedRows = gearInfoMapper.deductAvailableStock(dto.getGearId());
        if (affectedRows == 0) {
            throw new BusinessException(400, "库存不足，抢租失败");
        }

        BigDecimal totalFee = gearInfo.getDailyRent()
                .multiply(BigDecimal.valueOf(dto.getRentalDays()));

        RentalOrder order = new RentalOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setGearId(dto.getGearId());
        order.setRentalDays(dto.getRentalDays());
        order.setTotalFee(totalFee);
        order.setOrderStatus(0);
        order.setRemark(dto.getRemark());
        rentalOrderMapper.insert(order);
        return order;
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "RO" + timestamp + random;
    }
}
