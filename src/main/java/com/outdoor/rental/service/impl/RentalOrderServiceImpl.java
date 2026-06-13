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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

        log.info("用户 [{}] 开始抢租装备 [{}]，豁免金选项={}", userId, dto.getGearId(), dto.getHasDamageWaiver());

        // 费用计算（基础租金 + 可选 10% 豁免金）在事务服务中完成，保证与库存扣减同事务
        return redisLockService.executeWithLock(
                dto.getGearId(),
                lockProperties.getWaitSeconds(),
                lockProperties.getLeaseSeconds(),
                () -> rentalOrderTxService.createOrderInTransaction(userId, dto)
        );
    }

    @Override
    public RentalOrder payOrder(Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        RentalOrder order = getOrderOwnedByUser(orderId, userId);

        if (order.getOrderStatus() == null || order.getOrderStatus() != 0) {
            throw new BusinessException(400, "仅待支付订单可执行支付");
        }
        if (order.getRentalDays() == null || order.getRentalDays() <= 0) {
            throw new BusinessException(400, "订单租赁天数异常，无法支付");
        }

        LocalDateTime now = LocalDateTime.now();
        order.setOrderStatus(1);
        order.setRentOutTime(now);
        order.setExpectedReturnTime(now.plusDays(order.getRentalDays()));
        rentalOrderMapper.updateById(order);

        log.info("用户 [{}] 支付订单 [{}]", userId, order.getOrderNo());
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RentalOrder returnGear(Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        RentalOrder order = getOrderOwnedByUser(orderId, userId);

        if (order.getOrderStatus() == null
                || (order.getOrderStatus() != 1 && order.getOrderStatus() != 2)) {
            throw new BusinessException(400, "仅借出中或已逾期订单可归还");
        }

        LocalDateTime now = LocalDateTime.now();
        order.setOrderStatus(3);
        order.setActualReturnTime(now);

        if (order.getExpectedReturnTime() != null && now.isAfter(order.getExpectedReturnTime())) {
            order.setRemark(appendRemark(order.getRemark(), "逾期归还"));
        }

        int affectedRows = gearInfoMapper.restoreAvailableStock(order.getGearId());
        if (affectedRows == 0) {
            throw new BusinessException(400, "库存恢复失败，请联系管理员");
        }

        rentalOrderMapper.updateById(order);
        log.info("用户 [{}] 归还订单 [{}]", userId, order.getOrderNo());
        return order;
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

    private RentalOrder getOrderOwnedByUser(Long orderId, Long userId) {
        RentalOrder order = getById(orderId);
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException(403, "无权操作该订单");
        }
        return order;
    }

    private String appendRemark(String remark, String note) {
        if (note == null || note.isBlank()) {
            return remark;
        }
        if (remark == null || remark.isBlank()) {
            return note;
        }
        if (remark.contains(note)) {
            return remark;
        }
        return remark + "；" + note;
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
