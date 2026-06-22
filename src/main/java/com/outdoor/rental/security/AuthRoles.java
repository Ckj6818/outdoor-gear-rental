package com.outdoor.rental.security;

/**
 * Sa-Token 角色标识（与 sys_user.role 映射：0→ADMIN，1→USER）。
 */
public final class AuthRoles {

    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";

    /** Session 中存储角色的键名 */
    public static final String SESSION_ROLE = "role";
    public static final String SESSION_USERNAME = "username";

    private AuthRoles() {
    }

    public static String fromDbRole(Integer role) {
        return role != null && role == 0 ? ADMIN : USER;
    }
}
