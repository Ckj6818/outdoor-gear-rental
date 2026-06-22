-- ============================================================
-- 订单资金字段 + 装备实例状态码迁移（0~4 -> 1~4 租赁生命周期）
-- ============================================================

USE outdoor_gear_rental;

-- 1. 订单表增加租金/押金/赔偿/退还字段
ALTER TABLE rental_order
    ADD COLUMN rent_amount           DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '租金（元，不含豁免金）' AFTER total_fee,
    ADD COLUMN deposit_amount        DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '押金（元）' AFTER rent_amount,
    ADD COLUMN compensation_amount   DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '损坏赔偿金（元）' AFTER deposit_amount,
    ADD COLUMN actual_refund         DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '实际退还金额（元）= 押金 - 赔偿金' AFTER compensation_amount;

-- 历史订单回填：租金 = total_fee - waiver_fee；押金从 gear_info 关联获取
UPDATE rental_order ro
    JOIN gear_info gi ON ro.gear_id = gi.id
SET ro.rent_amount    = GREATEST(ro.total_fee - IFNULL(ro.waiver_fee, 0), 0),
    ro.deposit_amount = IFNULL(gi.deposit, 0)
WHERE ro.rent_amount = 0;

-- 2. 装备实例状态码迁移：旧 0~3 -> 新 1~4；旧 4(报废) -> 新 4(清洗/维修中)
UPDATE gear_item SET status = status + 1 WHERE status BETWEEN 0 AND 3;
UPDATE gear_item SET status = 4 WHERE status = 5;

ALTER TABLE gear_item DROP CHECK chk_item_status;
ALTER TABLE gear_item
    ADD CONSTRAINT chk_item_status CHECK (status >= 1 AND status <= 4);

-- 3. 更新订单状态注释（新增 6-已完成）
ALTER TABLE rental_order
    MODIFY COLUMN order_status TINYINT NOT NULL DEFAULT 0
        COMMENT '订单状态：0-待支付，1-借出中，2-已逾期，3-已归还/待结算，4-待质检，5-异常/需赔偿，6-已完成';

ALTER TABLE gear_item
    MODIFY COLUMN status TINYINT NOT NULL DEFAULT 1
        COMMENT '实例状态：1-待租，2-租赁中，3-归还待检查，4-清洗/维修中';
