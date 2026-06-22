package com.outdoor.rental.service.impl;

import com.outdoor.rental.entity.GearInfo;
import com.outdoor.rental.entity.GearStockCalendar;
import com.outdoor.rental.exception.BusinessException;
import com.outdoor.rental.exception.StockDeductionException;
import com.outdoor.rental.mapper.GearInfoMapper;
import com.outdoor.rental.mapper.GearStockCalendarMapper;
import com.outdoor.rental.service.GearStockCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GearStockCalendarServiceImpl implements GearStockCalendarService {

    /** 单日库存乐观锁扣减最大重试次数（含首次） */
    private static final int MAX_DEDUCT_RETRIES = 3;

    private final GearStockCalendarMapper gearStockCalendarMapper;
    private final GearInfoMapper gearInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductStock(Long gearId, LocalDate startDate, LocalDate endDate, Integer count) {
        validateParams(gearId, startDate, endDate, count);

        GearInfo gearInfo = gearInfoMapper.selectById(gearId);
        if (gearInfo == null) {
            throw new BusinessException(400, "装备不存在");
        }
        if (gearInfo.getStatus() == null || gearInfo.getStatus() != 1) {
            throw new BusinessException(400, "装备已下架，无法扣减库存");
        }

        List<LocalDate> requiredDates = enumerateDates(startDate, endDate);

        List<GearStockCalendar> lockedRows = gearStockCalendarMapper.selectForUpdateByGearAndDateRange(
                gearId, startDate, endDate);

        Map<LocalDate, GearStockCalendar> rowByDate = lockedRows.stream()
                .collect(Collectors.toMap(GearStockCalendar::getStockDate, Function.identity()));

        for (LocalDate date : requiredDates) {
            GearStockCalendar row = rowByDate.get(date);
            if (row == null) {
                throw new BusinessException(400,
                        "装备在 " + date + " 未配置日历库存，请先初始化该日期的库存");
            }
            if (row.getStock() == null || row.getStock() < count) {
                throw StockDeductionException.insufficient(
                        "装备在 " + date + " 库存不足，剩余 "
                                + (row.getStock() == null ? 0 : row.getStock())
                                + " 件，需要 " + count + " 件");
            }
        }

        for (LocalDate date : requiredDates) {
            GearStockCalendar row = rowByDate.get(date);
            deductOneDayWithRetry(row.getId(), date, count);
        }
    }

    /**
     * 对单日库存执行乐观锁扣减；失败时重新读取并重试，耗尽重试后抛出 {@link StockDeductionException}。
     */
    private void deductOneDayWithRetry(Long rowId, LocalDate date, Integer count) {
        for (int attempt = 1; attempt <= MAX_DEDUCT_RETRIES; attempt++) {
            GearStockCalendar current = gearStockCalendarMapper.selectById(rowId);
            if (current == null) {
                throw new BusinessException(400, "装备在 " + date + " 日历库存记录不存在");
            }
            if (current.getStock() == null || current.getStock() < count) {
                throw StockDeductionException.insufficient(
                        "装备在 " + date + " 库存不足，剩余 "
                                + (current.getStock() == null ? 0 : current.getStock())
                                + " 件，需要 " + count + " 件");
            }

            int affected = gearStockCalendarMapper.deductStockWithVersion(
                    current.getId(), count, current.getVersion());
            if (affected > 0) {
                return;
            }

            if (attempt >= MAX_DEDUCT_RETRIES) {
                throw StockDeductionException.busy();
            }
        }
    }

    private void validateParams(Long gearId, LocalDate startDate, LocalDate endDate, Integer count) {
        if (gearId == null) {
            throw new BusinessException(400, "装备ID不能为空");
        }
        if (startDate == null || endDate == null) {
            throw new BusinessException(400, "租借开始日期和结束日期不能为空");
        }
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(400, "租借结束日期不能早于开始日期");
        }
        if (count == null || count <= 0) {
            throw new BusinessException(400, "扣减数量必须大于 0");
        }
    }

    /** 生成闭区间 [startDate, endDate] 内的全部日期（含首尾）。 */
    private List<LocalDate> enumerateDates(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            dates.add(cursor);
            cursor = cursor.plusDays(1);
        }
        return dates;
    }
}
