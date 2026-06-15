package com.outdoor.rental.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 本地运行 main 方法，生成 BCrypt 密码哈希后写入数据库
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("123456 => " + encoder.encode("123456"));
    }
}
