package com.outdoor.rental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.outdoor.rental.common.PageResult;
import com.outdoor.rental.dto.SysUserCreateDTO;
import com.outdoor.rental.dto.SysUserQueryDTO;
import com.outdoor.rental.dto.SysUserUpdateDTO;
import com.outdoor.rental.entity.SysUser;
import com.outdoor.rental.exception.BusinessException;
import com.outdoor.rental.mapper.SysUserMapper;
import com.outdoor.rental.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private static final int ROLE_ADMIN = 0;
    private static final int STATUS_DISABLED = 0;
    private static final int STATUS_NORMAL = 1;
    private static final String DEFAULT_PASSWORD = "123456";

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<SysUser> pageQuery(SysUserQueryDTO query) {
        SysUserQueryDTO params = normalizeQuery(query);

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(params.getUsername()), SysUser::getUsername, params.getUsername())
                .eq(StringUtils.hasText(params.getPhone()), SysUser::getPhone, params.getPhone())
                .eq(params.getStatus() != null, SysUser::getStatus, params.getStatus())
                .eq(params.getRole() != null, SysUser::getRole, params.getRole())
                .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> page = new Page<>(params.getPageNum(), params.getPageSize());
        IPage<SysUser> result = sysUserMapper.selectPage(page, wrapper);
        result.getRecords().forEach(this::clearPassword);
        return PageResult.of(result);
    }

    @Override
    public SysUser createAdmin(SysUserCreateDTO dto) {
        String username = dto.getUsername().trim();
        assertUsernameAvailable(username, null);

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setRole(ROLE_ADMIN);
        user.setPhone(trimToNull(dto.getPhone()));
        user.setEmail(trimToNull(dto.getEmail()));
        user.setStatus(STATUS_NORMAL);

        sysUserMapper.insert(user);
        clearPassword(user);
        return user;
    }

    @Override
    public SysUser updateAdmin(Long id, SysUserUpdateDTO dto) {
        SysUser existing = getById(id);
        if (existing.getRole() == null || existing.getRole() != ROLE_ADMIN) {
            throw new BusinessException(400, "仅支持修改管理员账户");
        }

        if (StringUtils.hasText(dto.getUsername())) {
            String username = dto.getUsername().trim();
            assertUsernameAvailable(username, id);
            existing.setUsername(username);
        }
        if (dto.getPhone() != null) {
            existing.setPhone(trimToNull(dto.getPhone()));
        }
        if (dto.getEmail() != null) {
            existing.setEmail(trimToNull(dto.getEmail()));
        }

        sysUserMapper.updateById(existing);
        clearPassword(existing);
        return existing;
    }

    @Override
    public void deleteById(Long id) {
        SysUser existing = getById(id);
        if (existing.getRole() == null || existing.getRole() != ROLE_ADMIN) {
            throw new BusinessException(400, "仅支持删除管理员账户");
        }
        sysUserMapper.deleteById(id);
    }

    @Override
    public SysUser updateStatus(Long id, Integer status) {
        if (status == null || (status != STATUS_DISABLED && status != STATUS_NORMAL)) {
            throw new BusinessException(400, "状态值无效，仅支持 0-禁用 或 1-正常");
        }

        SysUser existing = getById(id);
        if (existing.getRole() == null || existing.getRole() != ROLE_ADMIN) {
            throw new BusinessException(400, "仅支持切换管理员账户状态");
        }

        existing.setStatus(status);
        sysUserMapper.updateById(existing);
        clearPassword(existing);
        return existing;
    }

    private SysUser getById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    private void assertUsernameAvailable(String username, Long excludeId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username);
        if (excludeId != null) {
            wrapper.ne(SysUser::getId, excludeId);
        }
        Long count = sysUserMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException(400, "用户名已存在");
        }
    }

    private SysUserQueryDTO normalizeQuery(SysUserQueryDTO query) {
        SysUserQueryDTO params = query != null ? query : new SysUserQueryDTO();
        if (params.getPageNum() == null || params.getPageNum() < 1) {
            params.setPageNum(1L);
        }
        if (params.getPageSize() == null || params.getPageSize() < 1) {
            params.setPageSize(10L);
        }
        if (params.getPageSize() > 100) {
            params.setPageSize(100L);
        }
        return params;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private void clearPassword(SysUser user) {
        if (user != null) {
            user.setPassword(null);
        }
    }
}
