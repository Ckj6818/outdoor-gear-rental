package com.outdoor.rental.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardStatsVO {

    /** 今日营收（元） */
    private BigDecimal todayRevenue;

    /** 出借中订单数 */
    private Integer rentingCount;

    /** 待质检订单数 */
    private Integer pendingInspectionCount;

    /** 总用户数（role=1 的普通玩家） */
    private Integer totalPlayers;

    /** 近 7 日营收与订单量趋势 */
    private List<WeeklyTrendItem> weeklyTrend;

    /** 各分类装备出借占比 */
    private List<CategoryShareItem> categoryShare;

    @Data
    public static class WeeklyTrendItem {

        /** 日期标签，如 06-14 */
        private String date;

        /** 当日营收（元） */
        private BigDecimal revenue;

        /** 当日订单量 */
        private Integer orderCount;
    }

    @Data
    public static class CategoryShareItem {

        /** 装备分类名称 */
        private String category;

        /** 出借次数 / 订单量 */
        private Integer count;
    }
}
