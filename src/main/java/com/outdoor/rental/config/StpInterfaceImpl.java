package com.outdoor.rental.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.outdoor.rental.security.AuthRoles;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 角色接口：为 {@code @SaCheckRole("ADMIN")} 等注解提供角色列表。
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Object role = StpUtil.getSession().get(AuthRoles.SESSION_ROLE);
        return role == null ? Collections.emptyList() : List.of(String.valueOf(role));
    }
}
