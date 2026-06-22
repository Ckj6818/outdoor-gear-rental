package com.outdoor.rental.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.outdoor.rental.annotation.LogOperation;
import com.outdoor.rental.common.Result;
import com.outdoor.rental.dto.GearItemStatusUpdateDTO;
import com.outdoor.rental.entity.GearItem;
import com.outdoor.rental.service.GearItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 装备实例（SKU）生命周期管理。
 */
@RestController
@RequestMapping("/api/admin/gear-items")
@RequiredArgsConstructor
public class GearItemController {

    private final GearItemService gearItemService;

    /**
     * 查询某 SPU 下全部装备实例
     * GET /api/admin/gear-items/by-gear/{gearId}
     */
    @GetMapping("/by-gear/{gearId}")
    @SaCheckRole("ADMIN")
    public Result<List<GearItem>> listByGearId(@PathVariable Long gearId) {
        return Result.success(gearItemService.listByGearId(gearId));
    }

    /**
     * 更新装备实例状态
     * PUT /api/admin/gear-items/{id}/status
     * <p>
     * 状态码：1-待租、2-租赁中、3-归还待检查、4-清洗/维修中
     * </p>
     */
    @PutMapping("/{id}/status")
    @SaCheckRole("ADMIN")
    @LogOperation("更新装备实例状态")
    public Result<GearItem> updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid GearItemStatusUpdateDTO dto) {
        GearItem item = gearItemService.updateStatus(id, dto);
        return Result.success("状态更新成功", item);
    }
}
