package com.outdoor.rental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.outdoor.rental.config.JwtProperties;
import com.outdoor.rental.dto.LoginDTO;
import com.outdoor.rental.entity.SysUser;
import com.outdoor.rental.exception.BusinessException;
import com.outdoor.rental.mapper.SysUserMapper;
import com.outdoor.rental.security.JwtUtil;
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
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

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

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return LoginVO.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getExpiration() / 1000)
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
