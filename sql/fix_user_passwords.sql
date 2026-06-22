-- 修复测试账号密码为 123456（BCrypt）
UPDATE sys_user SET password = '$2b$10$ks3o0mVHM98G7AA5kX9YDuJwqDkk6UcyX0X0jRuRgNCyJnr/clVG.'
WHERE username IN ('admin', 'zhangsan', 'lisi', 'wangwu');
