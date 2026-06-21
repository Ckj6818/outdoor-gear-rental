package com.outdoor.rental.service.impl;

import com.outdoor.rental.service.DashboardService;
import com.outdoor.rental.vo.DashboardStatsVO;
import com.outdoor.rental.vo.DashboardStatsVO.CategoryShareItem;
import com.outdoor.rental.vo.DashboardStatsVO.WeeklyTrendItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter DATE_LABEL = DateTimeFormatter.ofPattern("MM-dd");

    // TODO: 注入业务 Mapper，替换下方 Mock 查询
    // private final RentalOrderMapper rentalOrderMapper;
    // private final GearInfoMapper gearInfoMapper;
    // private final SysUserMapper sysUserMapper;

    @Override
    public DashboardStatsVO getStats() {
        DashboardStatsVO stats = new DashboardStatsVO();
        stats.setTodayRevenue(loadTodayRevenue());
        stats.setRentingCount(loadRentingCount());
        stats.setPendingInspectionCount(loadPendingInspectionCount());
        stats.setTotalPlayers(loadTotalPlayers());
        stats.setWeeklyTrend(loadWeeklyTrend());
        stats.setCategoryShare(loadCategoryShare());
        return stats;
    }

    /**
     * 今日营收：统计当日已支付订单（order_status >= 1）的 total_fee 之和。
     */
    private BigDecimal loadTodayRevenue() {
        // TODO: 在 RentalOrderMapper 中新增方法，例如：
        // SELECT COALESCE(SUM(total_fee), 0)
        // FROM rental_order
        // WHERE DATE(pay_time 或 rent_out_time) = CURDATE()
        //   AND order_status IN (1, 2, 3, 4, 5)
        // return rentalOrderMapper.sumRevenueByDate(LocalDate.now());
        return new BigDecimal("3280.00");
    }

    /**
     * 出借中数量：order_status = 1（借出中）+ order_status = 2（已逾期，装备仍未归还）。
     */
    private Integer loadRentingCount() {
        // TODO: 在 RentalOrderMapper 中新增方法，例如：
        // SELECT COUNT(*) FROM rental_order WHERE order_status IN (1, 2)
        // return rentalOrderMapper.countByStatuses(List.of(1, 2));
        return 14;
    }

    /**
     * 待质检数量：order_status = 4（用户已归还，等待管理员质检入库）。
     */
    private Integer loadPendingInspectionCount() {
        // TODO: 在 RentalOrderMapper 中新增方法，例如：
        // SELECT COUNT(*) FROM rental_order WHERE order_status = 4
        // return rentalOrderMapper.countByStatus(4);
        return 6;
    }

    /**
     * 总用户数：sys_user 表中 role = 1 的普通玩家数量。
     */
    private Integer loadTotalPlayers() {
        // TODO: 使用 SysUserMapper + LambdaQueryWrapper 统计：
        // SELECT COUNT(*) FROM sys_user WHERE role = 1
        // return sysUserMapper.selectCount(
        //         new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, 1)).intValue();
        return 86;
    }

    /**
     * 近 7 日趋势：按自然日聚合订单量与营收，供折线图展示。
     */
    private List<WeeklyTrendItem> loadWeeklyTrend() {
        // TODO: 在 RentalOrderMapper 中新增方法，按日期分组统计近 7 日数据，例如：
        // SELECT DATE(create_time) AS stat_date,
        //        COUNT(*) AS order_count,
        //        COALESCE(SUM(total_fee), 0) AS revenue
        // FROM rental_order
        // WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
        // GROUP BY DATE(create_time)
        // 再将查询结果补齐缺失日期（无订单日记为 0）
        // return rentalOrderMapper.selectWeeklyTrend(LocalDate.now().minusDays(6), LocalDate.now());

        int[] orderCounts = {8, 11, 9, 15, 12, 18, 14};
        double[] revenues = {1420.0, 1980.0, 1560.0, 2680.0, 2140.0, 3050.0, 3280.0};

        List<WeeklyTrendItem> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            int index = 6 - i;

            WeeklyTrendItem item = new WeeklyTrendItem();
            item.setDate(day.format(DATE_LABEL));
            item.setRevenue(BigDecimal.valueOf(revenues[index]));
            item.setOrderCount(orderCounts[index]);
            trend.add(item);
        }
        return trend;
    }

    /**
     * 装备分类出借占比：按 gear_info.category 统计当前出借中 / 历史出借订单分布。
     */
    private List<CategoryShareItem> loadCategoryShare() {
        // TODO: 在 RentalOrderMapper + GearInfoMapper 中联表统计，例如：
        // SELECT g.category, COUNT(*) AS cnt
        // FROM rental_order o
        // INNER JOIN gear_info g ON o.gear_id = g.id
        // WHERE o.order_status IN (1, 2, 3, 4)
        // GROUP BY g.category
        // ORDER BY cnt DESC
        // return gearInfoMapper.selectCategoryRentShare();

        List<CategoryShareItem> shares = new ArrayList<>();
        shares.add(buildCategoryShare("重装背包", 38));
        shares.add(buildCategoryShare("轻量化背包", 24));
        shares.add(buildCategoryShare("帐篷", 31));
        shares.add(buildCategoryShare("徒步鞋", 22));
        shares.add(buildCategoryShare("睡袋", 9));
        shares.add(buildCategoryShare("炉具", 11));
        shares.add(buildCategoryShare("登山杖", 7));
        return shares;
    }

    private CategoryShareItem buildCategoryShare(String category, int count) {
        CategoryShareItem item = new CategoryShareItem();
        item.setCategory(category);
        item.setCount(count);
        return item;
    }
}
