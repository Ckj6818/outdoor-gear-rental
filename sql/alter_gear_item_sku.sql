-- ============================================================
-- SKU/SN 重构：装备实例表 gear_item + rental_order.item_id
-- 适用：已有 outdoor_gear_rental 数据库的增量迁移
-- ============================================================

USE outdoor_gear_rental;

SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- 1. 新建装备实例表 gear_item（SKU/SN 级别追踪）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS gear_item (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '装备实例主键ID',
    gear_id         BIGINT          NOT NULL COMMENT '关联装备SPU ID（gear_info.id）',
    sn_code         VARCHAR(64)     NOT NULL COMMENT '唯一序列号（如 OSP-36L-0001）',
    status          TINYINT         NOT NULL DEFAULT 0 COMMENT '实例状态：0-在库，1-借出中，2-待质检，3-维修中，4-报废',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sn_code (sn_code),
    KEY idx_gear_id (gear_id),
    KEY idx_status (status),
    CONSTRAINT fk_item_gear FOREIGN KEY (gear_id) REFERENCES gear_info (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_item_status CHECK (status >= 0 AND status <= 4)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='装备实例表（SKU/SN）';

-- ------------------------------------------------------------
-- 2. rental_order 新增 item_id，记录本次借出的具体装备实例
-- ------------------------------------------------------------
ALTER TABLE rental_order
    ADD COLUMN item_id BIGINT DEFAULT NULL COMMENT '借出的装备实例ID（gear_item.id）' AFTER gear_id;

ALTER TABLE rental_order
    ADD KEY idx_item_id (item_id);

ALTER TABLE rental_order
    ADD CONSTRAINT fk_rental_item FOREIGN KEY (item_id) REFERENCES gear_item (id)
        ON UPDATE CASCADE ON DELETE RESTRICT;

SET FOREIGN_KEY_CHECKS = 1;
