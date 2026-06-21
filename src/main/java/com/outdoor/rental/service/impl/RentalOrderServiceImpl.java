package com.outdoor.rental.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.outdoor.rental.common.PageResult;
import com.outdoor.rental.config.RentalLockProperties;
import com.outdoor.rental.dto.CreateRentalOrderDTO;
import com.outdoor.rental.dto.InspectOrderDTO;
import com.outdoor.rental.dto.RentalOrderQueryDTO;
import com.outdoor.rental.entity.GearInfo;
import com.outdoor.rental.entity.GearItem;
import com.outdoor.rental.entity.RentalOrder;
import com.outdoor.rental.exception.BusinessException;
import com.outdoor.rental.mapper.GearInfoMapper;
import com.outdoor.rental.mapper.GearItemMapper;
import com.outdoor.rental.mapper.RentalOrderMapper;
import com.outdoor.rental.security.SecurityUtils;
import com.outdoor.rental.service.GearInfoService;
import com.outdoor.rental.service.RedisGearStockService;
import com.outdoor.rental.service.RedisLockService;
import com.outdoor.rental.service.RentalOrderService;
import com.outdoor.rental.service.RentalOrderTxService;
import com.outdoor.rental.vo.OccupiedDateRangeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalOrderServiceImpl implements RentalOrderService {

    private static final int ORDER_STATUS_RETURNED = 3;
    private static final int ORDER_STATUS_PENDING_INSPECTION = 4;
    private static final int ORDER_STATUS_ABNORMAL = 5;

    private final RentalOrderMapper rentalOrderMapper;
    private final GearInfoMapper gearInfoMapper;
    private final GearItemMapper gearItemMapper;
    private final GearInfoService gearInfoService;
    private final RedisLockService redisLockService;
    private final RedisGearStockService redisGearStockService;
    private final RentalOrderTxService rentalOrderTxService;
    private final RentalLockProperties lockProperties;

    @Override
    public PageResult<RentalOrder> pageQuery(RentalOrderQueryDTO query) {
        Page<RentalOrder> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<RentalOrder> result = rentalOrderMapper.selectOrderPage(page, query);
        return PageResult.of(result);
    }

    @Override
    public PageResult<RentalOrder> adminPageQuery(RentalOrderQueryDTO query) {
        return pageQuery(query);
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

        /*
         * ========== 高并发防超卖：三层防护（由外到内） ==========
         *
         * 第 1 层 — 分布式锁（RedisLockService，Key = gear:rent:lock:{gearId}）
         *   同一装备的并发下单请求在此串行化，避免大量线程同时冲击 Redis / MySQL。
         *   锁带 waitSeconds 超时与 leaseSeconds 自动过期，finally 中仅释放本线程持有的锁，防范死锁与误释放。
         *
         * 第 2 层 — Redis Lua 预扣减（RedisGearStockService，Key = gear:stock:{gearId}）
         *   在锁内以 Lua 脚本原子完成「读 available_stock → 判断 → DECRBY」，消除 check-then-act 竞态。
         *   预扣减成功后才进入 MySQL 事务；若 DB 失败则 rollbackPreDeduct 补偿 Redis，保持一致性。
         *
         * 第 3 层 — MySQL 本地事务（RentalOrderTxServiceImpl）
         *   SELECT FOR UPDATE 锁定可用装备实例 + 条件 UPDATE 扣减 available_stock，作为最终兜底。
         */
        return redisLockService.executeWithLock(
                dto.getGearId(),
                lockProperties.getWaitSeconds(),
                lockProperties.getLeaseSeconds(),
                () -> createOrderWithRedisStockGuard(userId, dto)
        );
    }

    /**
     * 在分布式锁临界区内：先 Redis 预扣减，再 MySQL 本地事务落库；DB 失败时回滚 Redis。
     */
    private RentalOrder createOrderWithRedisStockGuard(Long userId, CreateRentalOrderDTO dto) {
        Long gearId = dto.getGearId();
        boolean redisPreDeducted = false;

        if (redisGearStockService.isAvailable()) {
            boolean reserved = redisGearStockService.tryPreDeduct(gearId, 1);
            if (!reserved) {
                throw new BusinessException(400, "库存不足，抢租失败");
            }
            redisPreDeducted = true;
        }

        try {
            return rentalOrderTxService.createOrderInTransaction(userId, dto);
        } catch (Exception ex) {
            // MySQL 事务回滚后，必须补偿 Redis 预扣减，否则 Redis 库存会低于 DB 导致后续误拒单
            if (redisPreDeducted) {
                redisGearStockService.rollbackPreDeduct(gearId, 1);
            }
            throw ex;
        }
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

        if (order.getItemId() == null) {
            throw new BusinessException(400, "订单未关联装备实例，无法归还");
        }

        LocalDateTime now = LocalDateTime.now();
        order.setOrderStatus(ORDER_STATUS_PENDING_INSPECTION);
        order.setActualReturnTime(now);

        if (order.getExpectedReturnTime() != null && now.isAfter(order.getExpectedReturnTime())) {
            order.setRemark(appendRemark(order.getRemark(), "逾期归还"));
        }

        int itemRows = gearItemMapper.markAsPendingInspection(order.getItemId());
        if (itemRows == 0) {
            throw new BusinessException(400, "装备实例状态异常，无法归还");
        }

        rentalOrderMapper.updateById(order);
        log.info("用户 [{}] 归还订单 [{}]，进入待质检流程", userId, order.getOrderNo());
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RentalOrder inspectOrder(InspectOrderDTO dto) {
        if (dto.getOrderId() == null) {
            throw new BusinessException(400, "订单ID不能为空");
        }
        if (dto.getIsPassed() == null) {
            throw new BusinessException(400, "质检结果不能为空");
        }

        RentalOrder order = getById(dto.getOrderId());
        if (order.getOrderStatus() == null || order.getOrderStatus() != ORDER_STATUS_PENDING_INSPECTION) {
            throw new BusinessException(400, "仅待质检订单可执行质检");
        }
        if (order.getItemId() == null) {
            throw new BusinessException(400, "订单未关联装备实例，无法质检");
        }

        GearItem gearItem = gearItemMapper.selectById(order.getItemId());
        if (gearItem == null) {
            throw new BusinessException(400, "装备实例不存在");
        }

        GearInfo gearInfo = gearInfoMapper.selectById(order.getGearId());
        if (gearInfo == null) {
            throw new BusinessException(400, "关联装备不存在");
        }

        order.setRemark(appendRemark(order.getRemark(), dto.getRemark()));

        if (Boolean.TRUE.equals(dto.getIsPassed())) {
            order.setOrderStatus(ORDER_STATUS_RETURNED);
            int itemRows = gearItemMapper.markInspectionPassed(order.getItemId());
            if (itemRows == 0) {
                throw new BusinessException(400, "装备实例状态异常，无法完成质检");
            }
            int stockRows = gearInfoMapper.restoreAvailableStock(order.getGearId());
            if (stockRows == 0) {
                throw new BusinessException(400, "可用库存恢复失败，请联系管理员");
            }
            // 同步恢复 Redis 预扣减库存，保持 gear:stock:{gearId} 与 MySQL available_stock 一致
            redisGearStockService.restoreStock(order.getGearId(), 1);
            gearInfoService.applyLifecycleAfterInspectionPass(order.getGearId());
            log.info("管理员质检通过订单 [{}]，装备实例 [{}] 已回库", order.getOrderNo(), gearItem.getSnCode());
        } else {
            order.setOrderStatus(ORDER_STATUS_ABNORMAL);
            int itemRows = gearItemMapper.markAsRepairing(order.getItemId());
            if (itemRows == 0) {
                throw new BusinessException(400, "装备实例状态异常，无法完成质检");
            }
            log.info("管理员质检异常订单 [{}]，装备实例 [{}] 进入维修", order.getOrderNo(), gearItem.getSnCode());
        }

        rentalOrderMapper.updateById(order);
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

    @Override
    public List<OccupiedDateRangeVO> listOccupiedDates(Long gearId) {
        if (gearId == null) {
            throw new BusinessException(400, "装备ID不能为空");
        }
        GearInfo gearInfo = gearInfoMapper.selectById(gearId);
        if (gearInfo == null) {
            throw new BusinessException(404, "装备不存在");
        }

        return rentalOrderMapper.selectActiveOccupiedOrdersByGearId(gearId).stream()
                .map(this::toOccupiedDateRange)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public boolean checkDateAvailable(Long gearId, LocalDate startDate, LocalDate endDate) {
        validateDateRangeParams(gearId, startDate, endDate);

        List<RentalOrder> activeOrders = rentalOrderMapper.selectActiveOccupiedOrdersByGearId(gearId);
        for (RentalOrder order : activeOrders) {
            OccupiedDateRangeVO occupied = toOccupiedDateRange(order);
            if (occupied == null) {
                continue;
            }
            if (isOccupiedRangeOverlap(
                    occupied.getStartDate(), occupied.getEndDate(), startDate, endDate)) {
                log.warn("档期冲突 gearId={} 已有订单 [{}] 占用 {} ~ {}，请求 {} ~ {}",
                        gearId, order.getOrderNo(),
                        occupied.getStartDate(), occupied.getEndDate(), startDate, endDate);
                throw new BusinessException(400, "该档期已被占用");
            }
        }
        return true;
    }

    /**
     * 将有效订单转换为占用档期。起租日 = rent_out_time，归还日 = expected/actual_return_time。
     */
    private OccupiedDateRangeVO toOccupiedDateRange(RentalOrder order) {
        if (order.getRentOutTime() == null) {
            return null;
        }
        LocalDate startDate = order.getRentOutTime().toLocalDate();
        LocalDate endDate = resolveOccupiedEndDate(order);
        if (endDate.isBefore(startDate)) {
            endDate = startDate;
        }
        return new OccupiedDateRangeVO(startDate, endDate);
    }

    /**
     * 计算订单占用结束日：借出中/逾期取预计归还日；待质检装备尚未可租，结束日至少延续至今天。
     */
    private LocalDate resolveOccupiedEndDate(RentalOrder order) {
        Integer status = order.getOrderStatus();
        LocalDate today = LocalDate.now();

        if (status != null && status == ORDER_STATUS_PENDING_INSPECTION) {
            LocalDate endDate = today;
            if (order.getActualReturnTime() != null) {
                endDate = order.getActualReturnTime().toLocalDate();
            } else if (order.getExpectedReturnTime() != null) {
                endDate = order.getExpectedReturnTime().toLocalDate();
            }
            return endDate.isBefore(today) ? today : endDate;
        }

        if (order.getExpectedReturnTime() != null) {
            return order.getExpectedReturnTime().toLocalDate();
        }
        if (order.getActualReturnTime() != null) {
            return order.getActualReturnTime().toLocalDate();
        }
        return today;
    }

    /**
     * 判断两个闭区间档期是否冲突。
     * <p>
     * 占用区间含起租日与归还日；归还日当天不可再起租下一单（故新单起租日 &lt;= 已有归还日时视为冲突）。
     * </p>
     */
    private boolean isOccupiedRangeOverlap(LocalDate occupiedStart, LocalDate occupiedEnd,
                                           LocalDate requestStart, LocalDate requestEnd) {
        return !requestStart.isAfter(occupiedEnd) && !occupiedStart.isAfter(requestEnd);
    }

    private void validateDateRangeParams(Long gearId, LocalDate startDate, LocalDate endDate) {
        if (gearId == null) {
            throw new BusinessException(400, "装备ID不能为空");
        }
        if (startDate == null || endDate == null) {
            throw new BusinessException(400, "起租日期与归还日期不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(400, "起租日期不能晚于归还日期");
        }
        GearInfo gearInfo = gearInfoMapper.selectById(gearId);
        if (gearInfo == null) {
            throw new BusinessException(404, "装备不存在");
        }
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
