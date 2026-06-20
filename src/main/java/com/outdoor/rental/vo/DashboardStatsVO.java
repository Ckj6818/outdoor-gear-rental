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

    /** 总玩家数 */
    private Integer totalPlayers;

    /** 近 7 日营收与订单量趋势 */
    private List<DailyTrendVO> weeklyTrend;

    /** 各分类装备出借占比 */
    private List<CategoryShareVO> categoryShare;
}
