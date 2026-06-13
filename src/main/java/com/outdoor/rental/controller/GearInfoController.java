package com.outdoor.rental.controller;

import com.outdoor.rental.common.PageResult;
import com.outdoor.rental.common.Result;
import com.outdoor.rental.dto.GearInfoQueryDTO;
import com.outdoor.rental.entity.GearInfo;
import com.outdoor.rental.service.GearInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gears")
@RequiredArgsConstructor
public class GearInfoController {

    private final GearInfoService gearInfoService;

    /**
     * 分页查询装备列表（支持名称模糊搜索、分类筛选）
     * GET /api/gears?gearName=Osprey&category=重装背包&pageNum=1&pageSize=10
     */
    @GetMapping
    public Result<PageResult<GearInfo>> page(GearInfoQueryDTO query) {
        return Result.success(gearInfoService.pageQuery(query));
    }

    /**
     * 根据 ID 查询装备详情
     * GET /api/gears/{id}
     */
    @GetMapping("/{id}")
    public Result<GearInfo> getById(@PathVariable Long id) {
        return Result.success(gearInfoService.getById(id));
    }

    /**
     * 新增装备
     * POST /api/gears
     */
    @PostMapping
    public Result<Void> create(@RequestBody GearInfo gearInfo) {
        gearInfoService.create(gearInfo);
        return Result.success("新增成功", null);
    }

    /**
     * 更新装备
     * PUT /api/gears/{id}
     */
    @PutMapping("/{id}")
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
    public Result<Void> delete(@PathVariable Long id) {
        gearInfoService.deleteById(id);
        return Result.success("删除成功", null);
    }
}
