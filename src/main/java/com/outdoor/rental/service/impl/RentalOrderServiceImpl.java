package com.outdoor.rental.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.outdoor.rental.common.PageResult;
import com.outdoor.rental.config.RentalLockProperties;
import com.outdoor.rental.dto.CreateRentalOrderDTO;
import com.outdoor.rental.dto.RentalOrderQueryDTO;
import com.outdoor.rental.entity.GearInfo;
import com.outdoor.rental.entity.RentalOrder;
import com.outdoor.rental.exception.BusinessException;
import com.outdoor.rental.mapper.GearInfoMapper;
import com.outdoor.rental.mapper.RentalOrderMapper;
import com.outdoor.rental.security.SecurityUtils;
import com.outdoor.rental.service.RedisLockService;
import com.outdoor.rental.service.RentalOrderService;
import com.outdoor.rental.service.RentalOrderTxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalOrderServiceImpl implements RentalOrderService {

    private final RentalOrderMapper rentalOrderMapper;
    private final GearInfoMapper gearInfoMapper;
    private final RedisLockService redisLockService;
    private final RentalOrderTxService rentalOrderTxService;
    private final RentalLockProperties lockProperties;

    @Override
    public PageResult<RentalOrder> pageQuery(RentalOrderQueryDTO query) {
        Page<RentalOrder> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<RentalOrder> result = rentalOrderMapper.selectOrderPage(page, query);
        return PageResult.of(result);
    }

    @Override
    public RentalOrder getById(Long id) {
        RentalOrder order = rentalOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        return order;
    }

    @Override
    public RentalOrder createRentalOrder(CreateRentalOrderDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        validateCreateRequest(dto);

        log.info("用户 [{}] 开始抢租装备 [{}]", userId, dto.getGearId());

        // 第一层：Redisson 分布式锁，串行化同一装备的并发下单
        return redisLockService.executeWithLock(
                dto.getGearId(),
                lockProperties.getWaitSeconds(),
                lockProperties.getLeaseSeconds(),
                () -> rentalOrderTxService.createOrderInTransaction(userId, dto)
        );
    }

    @Override
    public void update(RentalOrder rentalOrder) {
        if (rentalOrder.getId() == null) {
            throw new BusinessException(400, "订单ID不能为空");
        }
        getById(rentalOrder.getId());
        validateOrder(rentalOrder);
        rentalOrderMapper.updateById(rentalOrder);
    }

    @Override
    public void deleteById(Long id) {
        getById(id);
        rentalOrderMapper.deleteById(id);
    }

    private void validateCreateRequest(CreateRentalOrderDTO dto) {
        if (dto.getGearId() == null) {
            throw new BusinessException(400, "装备ID不能为空");
        }
        if (dto.getRentalDays() == null || dto.getRentalDays() <= 0) {
            throw new BusinessException(400, "租赁天数必须大于0");
        }
        GearInfo gearInfo = gearInfoMapper.selectById(dto.getGearId());
        if (gearInfo == null) {
            throw new BusinessException(400, "装备不存在");
        }
    }

    private void validateOrder(RentalOrder order) {
        if (order.getUserId() == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        if (order.getGearId() == null) {
            throw new BusinessException(400, "装备ID不能为空");
        }
        GearInfo gearInfo = gearInfoMapper.selectById(order.getGearId());
        if (gearInfo == null) {
            throw new BusinessException(400, "关联装备不存在");
        }
        if (order.getRentalDays() == null || order.getRentalDays() <= 0) {
            throw new BusinessException(400, "租赁天数必须大于0");
        }
        if (order.getTotalFee() == null || order.getTotalFee().signum() < 0) {
            throw new BusinessException(400, "订单总费用不能为负数");
        }
    }
}
