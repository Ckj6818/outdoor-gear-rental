package com.outdoor.rental.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.outdoor.rental.annotation.LogOperation;
import com.outdoor.rental.common.PageResult;
import com.outdoor.rental.common.Result;
import com.outdoor.rental.dto.SysGearQueryDTO;
import com.outdoor.rental.dto.SysGearSaveDTO;
import com.outdoor.rental.dto.SysGearStatusDTO;
import com.outdoor.rental.entity.SysGear;
import com.outdoor.rental.service.SysGearService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/admin/system/gear")
@RequiredArgsConstructor
@SaCheckRole("ADMIN")
public class SysGearController {

    private final SysGearService sysGearService;

    /**
     * 分页查询装备
     * GET /api/admin/system/gear?name=Osprey&category=重装背包&status=1&pageNum=1&pageSize=10
     */
    @GetMapping
    public Result<PageResult<SysGear>> page(SysGearQueryDTO query) {
        return Result.success(sysGearService.pageQuery(query));
    }

    /**
     * 新增装备
     * POST /api/admin/system/gear
     */
    @PostMapping
    @LogOperation("新增装备")
    public Result<SysGear> create(@RequestBody @Valid SysGearSaveDTO dto) {
        SysGear gear = sysGearService.create(dto);
        return Result.success("新增成功", gear);
    }

    /**
     * 修改装备
     * PUT /api/admin/system/gear/{id}
     */
    @PutMapping("/{id}")
    @LogOperation("修改装备")
    public Result<SysGear> update(@PathVariable Long id, @RequestBody @Valid SysGearSaveDTO dto) {
        SysGear gear = sysGearService.update(id, dto);
        return Result.success("更新成功", gear);
    }

    /**
     * 删除装备
     * DELETE /api/admin/system/gear/{id}
     */
    @DeleteMapping("/{id}")
    @LogOperation("删除装备")
    public Result<Void> delete(@PathVariable Long id) {
        sysGearService.deleteById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 上下架状态切换
     * PUT /api/admin/system/gear/{id}/status
     * Body: { "status": 1 } 上架 | { "status": 0 } 下架
     */
    @PutMapping("/{id}/status")
    @LogOperation("切换装备上下架")
    public Result<SysGear> updateStatus(@PathVariable Long id, @RequestBody @Valid SysGearStatusDTO dto) {
        SysGear gear = sysGearService.updateStatus(id, dto.getStatus());
        return Result.success("状态更新成功", gear);
    }
}
