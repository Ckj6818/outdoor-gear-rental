package com.outdoor.rental.service;

import com.outdoor.rental.common.PageResult;
import com.outdoor.rental.dto.SysUserCreateDTO;
import com.outdoor.rental.dto.SysUserQueryDTO;
import com.outdoor.rental.dto.SysUserUpdateDTO;
import com.outdoor.rental.entity.SysUser;

public interface SysUserService {

    PageResult<SysUser> pageQuery(SysUserQueryDTO query);

    SysUser createAdmin(SysUserCreateDTO dto);

    SysUser updateAdmin(Long id, SysUserUpdateDTO dto);

    void deleteById(Long id);

    SysUser updateStatus(Long id, Integer status);
}
