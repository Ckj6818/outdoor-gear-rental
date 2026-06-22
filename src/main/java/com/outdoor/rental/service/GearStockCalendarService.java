package com.outdoor.rental.service;

import java.time.LocalDate;

/**
 * 装备日历库存服务：按日期维度管理可租库存。
 */
public interface GearStockCalendarService {

    /**
     * 扣减指定租赁档期内的日历库存。
     * <p>
     * 校验 [startDate, endDate] 区间内每一天的 stock 均 &gt;= count，
     * 全部满足后逐日扣减 count（含首尾日期）。
     * </p>
     *
     * @param gearId    装备 ID
     * @param startDate 租借开始日期（含）
     * @param endDate   租借结束日期（含）
     * @param count     扣减数量
     */
    void deductStock(Long gearId, LocalDate startDate, LocalDate endDate, Integer count);
}
