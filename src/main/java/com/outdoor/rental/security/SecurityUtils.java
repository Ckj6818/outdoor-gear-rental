package com.outdoor.rental.security;

import com.outdoor.rental.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static SecurityUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser user)) {
            throw new BusinessException(401, "用户未登录");
        }
        return user;
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }
}
