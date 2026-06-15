USE outdoor_gear_rental;

-- 装备成色
ALTER TABLE gear_info
    ADD COLUMN condition_grade VARCHAR(20) DEFAULT '9成新' COMMENT '装备成色：全新/9成新/轻微使用痕迹' AFTER hover_image;

-- 订单豁免金
ALTER TABLE rental_order
    ADD COLUMN has_damage_waiver TINYINT NOT NULL DEFAULT 0 COMMENT '是否购买意外损坏豁免金：0-否，1-是' AFTER total_fee,
    ADD COLUMN waiver_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '意外损坏豁免金费用（元）' AFTER has_damage_waiver;

-- 为现有装备分配成色（示例数据）
UPDATE gear_info SET condition_grade = '全新' WHERE id IN (7, 14, 16);
UPDATE gear_info SET condition_grade = '9成新' WHERE id IN (1, 3, 4, 8, 12, 13, 18);
UPDATE gear_info SET condition_grade = '轻微使用痕迹' WHERE id IN (2, 5, 6, 9, 10, 11, 15, 17);
