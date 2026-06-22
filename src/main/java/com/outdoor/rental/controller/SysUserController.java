package com.outdoor.rental.controller;

import com.outdoor.rental.annotation.LogOperation;
import com.outdoor.rental.common.PageResult;
import com.outdoor.rental.common.Result;
import com.outdoor.rental.dto.SysUserCreateDTO;
import com.outdoor.rental.dto.SysUserQueryDTO;
import com.outdoor.rental.dto.SysUserStatusDTO;
import com.outdoor.rental.dto.SysUserUpdateDTO;
import com.outdoor.rental.entity.SysUser;
import com.outdoor.rental.service.SysUserService;
import jakarta.validation.Valid;
import cn.dev33.satoken.annotation.SaCheckRole;
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
@RequestMapping("/api/admin/system/user")
@RequiredArgsConstructor
@SaCheckRole("ADMIN")
public class SysUserController {

    private final SysUserService sysUserService;

    /**
     * 分页查询系统用户
     * GET /api/admin/system/user?username=admin&phone=13800000000&status=1&role=0&pageNum=1&pageSize=10
     * role 不传查全部；role=0 仅管理员；role=1 仅普通用户
     */
    @GetMapping
    public Result<PageResult<SysUser>> page(SysUserQueryDTO query) {
        return Result.success(sysUserService.pageQuery(query));
    }

    /**
     * 新增管理员（默认 role=0，初始密码 123456）
     * POST /api/admin/system/user
     */
    @PostMapping
    @LogOperation("新增管理员")
    public Result<SysUser> create(@RequestBody @Valid SysUserCreateDTO dto) {
        SysUser user = sysUserService.createAdmin(dto);
        return Result.success("新增成功", user);
    }

    /**
     * 修改管理员信息（不支持修改密码）
     * PUT /api/admin/system/user/{id}
     */
    @PutMapping("/{id}")
    @LogOperation("修改管理员")
    public Result<SysUser> update(@PathVariable Long id, @RequestBody SysUserUpdateDTO dto) {
        SysUser user = sysUserService.updateAdmin(id, dto);
        return Result.success("更新成功", user);
    }

    /**
     * 删除管理员
     * DELETE /api/admin/system/user/{id}
     */
    @DeleteMapping("/{id}")
    @LogOperation("删除管理员")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.deleteById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 启用/禁用管理员
     * PUT /api/admin/system/user/{id}/status
     * Body: { "status": 0 } 禁用 | { "status": 1 } 正常
     */
    @PutMapping("/{id}/status")
    @LogOperation("切换管理员状态")
    public Result<SysUser> updateStatus(@PathVariable Long id, @RequestBody @Valid SysUserStatusDTO dto) {
        SysUser user = sysUserService.updateStatus(id, dto.getStatus());
        return Result.success("状态更新成功", user);
    }
}
