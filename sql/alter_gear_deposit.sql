-- 为 gear_info 增加押金字段，供 SysGear 管理模块使用
USE outdoor_gear_rental;

ALTER TABLE gear_info
    ADD COLUMN deposit DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '押金（元）' AFTER daily_rent;

-- 按品类设置示例押金
UPDATE gear_info SET deposit = 800.00  WHERE category LIKE '%背包%' AND deposit = 0;
UPDATE gear_info SET deposit = 1200.00 WHERE category = '帐篷' AND deposit = 0;
UPDATE gear_info SET deposit = 600.00  WHERE category = '徒步鞋' AND deposit = 0;
UPDATE gear_info SET deposit = 400.00  WHERE category IN ('睡袋', '炉具', '登山杖') AND deposit = 0;
