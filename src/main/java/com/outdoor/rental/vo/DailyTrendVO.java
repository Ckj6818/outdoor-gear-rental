package com.outdoor.rental.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DailyTrendVO {

    /** 日期标签，如 06-14 */
    private String date;

    /** 当日营收 */
    private BigDecimal revenue;

    /** 当日订单量 */
    private Integer orderCount;
}
