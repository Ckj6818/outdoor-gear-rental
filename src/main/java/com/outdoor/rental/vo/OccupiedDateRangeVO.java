package com.outdoor.rental.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 装备已被占用的租赁档期（日历展示用）。
 * <p>
 * startDate 对应订单借出时间 {@code rent_out_time} 的日期部分；
 * endDate 对应预计/实际归还时间的日期部分（待质检订单会延续占用至当前日期）。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OccupiedDateRangeVO {

    private LocalDate startDate;

    private LocalDate endDate;
}
