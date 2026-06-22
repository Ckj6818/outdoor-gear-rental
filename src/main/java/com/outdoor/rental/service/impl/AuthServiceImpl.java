package com.outdoor.rental.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.outdoor.rental.dto.LoginDTO;
import com.outdoor.rental.entity.SysUser;
import com.outdoor.rental.exception.BusinessException;
import com.outdoor.rental.mapper.SysUserMapper;
import com.outdoor.rental.security.AuthRoles;
import com.outdoor.rental.service.AuthService;
import com.outdoor.rental.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, loginDTO.getUsername()));
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用");
        }
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // Sa-Token 登录：以 userId 作为 loginId，并在 Session 中附加角色等信息
        StpUtil.login(user.getId());
        StpUtil.getSession().set(AuthRoles.SESSION_ROLE, AuthRoles.fromDbRole(user.getRole()));
        StpUtil.getSession().set(AuthRoles.SESSION_USERNAME, user.getUsername());

        return LoginVO.builder()
                .token(StpUtil.getTokenValue())
                .tokenType("Bearer")
                .expiresIn(StpUtil.getTokenTimeout())
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
