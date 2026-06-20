-- 装备详情：技术参数 & 使用须知
USE outdoor_gear_rental;

ALTER TABLE gear_info
    ADD COLUMN specifications VARCHAR(500) DEFAULT NULL COMMENT '技术参数(以分号分隔，如：重量:1.2kg;材质:尼龙)' AFTER description,
    ADD COLUMN usage_instructions VARCHAR(500) DEFAULT NULL COMMENT '使用须知及注意事项' AFTER specifications;

UPDATE gear_info SET specifications = '重量:1.49kg;容量:36L;背负:AirSpeed空速背板', usage_instructions = '请勿在背包内放置尖锐物品；清洗时请用湿布擦拭，不可机洗。' WHERE gear_name LIKE 'Stratos 36L%';
UPDATE gear_info SET specifications = '重量:2.20kg;容量:65L;背负:Guide Light背负系统', usage_instructions = '装载重物时请调节腰垫与肩带；长期存放请保持干燥通风，避免背板变形。' WHERE gear_name LIKE 'Terraframe 65%';
UPDATE gear_info SET specifications = '重量:1.54kg;人数:2人;季节:三季;面料:20D尼龙', usage_instructions = '搭建前请清理地面尖锐物；收帐后请完全晾干再入库，避免产生霉味。' WHERE gear_name LIKE 'Hubba Hubba NX%';
