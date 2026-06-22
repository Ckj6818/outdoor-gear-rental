package com.outdoor.rental.security;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.outdoor.rental.exception.BusinessException;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long getCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (NotLoginException ex) {
            throw new BusinessException(401, "用户未登录");
        }
    }

    public static String getCurrentUsername() {
        try {
            Object username = StpUtil.getSession().get(AuthRoles.SESSION_USERNAME);
            return username != null ? String.valueOf(username) : StpUtil.getLoginIdAsString();
        } catch (NotLoginException ex) {
            throw new BusinessException(401, "用户未登录");
        }
    }

    public static String getCurrentRole() {
        try {
            Object role = StpUtil.getSession().get(AuthRoles.SESSION_ROLE);
            return role != null ? String.valueOf(role) : AuthRoles.USER;
        } catch (NotLoginException ex) {
            throw new BusinessException(401, "用户未登录");
        }
    }

    public static boolean isAdmin() {
        return AuthRoles.ADMIN.equals(getCurrentRole());
    }
}
