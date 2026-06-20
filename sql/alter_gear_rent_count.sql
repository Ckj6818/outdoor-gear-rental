-- 装备累计出借次数（用于成色与租金自动折旧）
USE outdoor_gear_rental;

ALTER TABLE gear_info
    ADD COLUMN rent_count INT NOT NULL DEFAULT 0 COMMENT '累计出借次数（质检通过后累加）' AFTER condition_grade;
