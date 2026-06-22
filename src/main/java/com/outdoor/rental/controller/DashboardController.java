package com.outdoor.rental.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.outdoor.rental.common.Result;
import com.outdoor.rental.service.DashboardService;
import com.outdoor.rental.vo.DashboardStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 运营大屏统计数据
     * GET /api/admin/dashboard/stats
     */
    @GetMapping("/stats")
    @SaCheckRole("ADMIN")
    public Result<DashboardStatsVO> stats() {
        return Result.success(dashboardService.getStats());
    }
}
