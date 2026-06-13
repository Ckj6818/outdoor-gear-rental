-- 将测试用户密码更新为 123456 的 BCrypt 哈希
-- 执行方式：mysql -u root -p outdoor_gear_rental < sql/update_password_bcrypt.sql

UPDATE sys_user
SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iK6HZKLy'
WHERE username IN ('admin', 'zhangsan', 'lisi', 'wangwu');
