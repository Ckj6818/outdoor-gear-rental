package com.outdoor.rental.service.impl;

import com.outdoor.rental.service.DashboardService;
import com.outdoor.rental.vo.CategoryShareVO;
import com.outdoor.rental.vo.DailyTrendVO;
import com.outdoor.rental.vo.DashboardStatsVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter DATE_LABEL = DateTimeFormatter.ofPattern("MM-dd");

    @Override
    public DashboardStatsVO getStats() {
        DashboardStatsVO stats = new DashboardStatsVO();
        stats.setTodayRevenue(new BigDecimal("2860.00"));
        stats.setRentingCount(18);
        stats.setPendingInspectionCount(5);
        stats.setTotalPlayers(128);
        stats.setWeeklyTrend(buildWeeklyTrend());
        stats.setCategoryShare(buildCategoryShare());
        return stats;
    }

    private List<DailyTrendVO> buildWeeklyTrend() {
        int[] orderCounts = {12, 15, 9, 18, 14, 21, 16};
        double[] revenues = {1680.0, 2140.0, 1320.0, 2560.0, 1980.0, 3020.0, 2860.0};

        List<DailyTrendVO> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            int index = 6 - i;
            DailyTrendVO item = new DailyTrendVO();
            item.setDate(day.format(DATE_LABEL));
            item.setRevenue(BigDecimal.valueOf(revenues[index]));
            item.setOrderCount(orderCounts[index]);
            trend.add(item);
        }
        return trend;
    }

    private List<CategoryShareVO> buildCategoryShare() {
        List<CategoryShareVO> shares = new ArrayList<>();
        shares.add(buildShare("重装背包", 42));
        shares.add(buildShare("帐篷", 28));
        shares.add(buildShare("徒步鞋", 18));
        shares.add(buildShare("睡袋", 12));
        shares.add(buildShare("电子设备", 8));
        shares.add(buildShare("配件", 22));
        return shares;
    }

    private CategoryShareVO buildShare(String category, int count) {
        CategoryShareVO share = new CategoryShareVO();
        share.setCategory(category);
        share.setCount(count);
        return share;
    }
}
