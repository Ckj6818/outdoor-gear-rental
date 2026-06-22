package com.outdoor.rental.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.outdoor.rental.common.PageResult;
import com.outdoor.rental.common.Result;
import com.outdoor.rental.dto.GearInfoQueryDTO;
import com.outdoor.rental.entity.GearInfo;
import com.outdoor.rental.service.GearInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "装备大厅", description = "首页装备列表、详情查询（公开）及管理员维护")
@RestController
@RequestMapping("/api/gears")
@RequiredArgsConstructor
public class GearInfoController {

    private final GearInfoService gearInfoService;

    @Operation(summary = "分页查询装备列表", description = "首页推荐/全部装备，支持 keyword、category、brand 筛选，结果带 Redis 缓存")
    @GetMapping
    public Result<PageResult<GearInfo>> page(GearInfoQueryDTO query) {
        return Result.success(gearInfoService.pageQuery(query));
    }

    @Operation(summary = "查询装备详情", description = "根据装备 ID 获取 SPU 详情")
    @GetMapping("/{id}")
    public Result<GearInfo> getById(
            @Parameter(description = "装备 ID", required = true, example = "1")
            @PathVariable Long id) {
        return Result.success(gearInfoService.getById(id));
    }

    /**
     * 新增装备
     * POST /api/gears
     */
    @PostMapping
    @SaCheckRole("ADMIN")
    public Result<Void> create(@RequestBody GearInfo gearInfo) {
        gearInfoService.create(gearInfo);
        return Result.success("新增成功", null);
    }

    /**
     * 更新装备
     * PUT /api/gears/{id}
     */
    @PutMapping("/{id}")
    @SaCheckRole("ADMIN")
    public Result<Void> update(@PathVariable Long id, @RequestBody GearInfo gearInfo) {
        gearInfo.setId(id);
        gearInfoService.update(gearInfo);
        return Result.success("更新成功", null);
    }

    /**
     * 删除装备
     * DELETE /api/gears/{id}
     */
    @DeleteMapping("/{id}")
    @SaCheckRole("ADMIN")
    public Result<Void> delete(@PathVariable Long id) {
        gearInfoService.deleteById(id);
        return Result.success("删除成功", null);
    }
}
