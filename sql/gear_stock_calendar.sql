-- ============================================================
-- 装备库存日历表 gear_stock_calendar
-- 按「装备 + 日期」维度记录当日剩余可租库存，支持乐观锁并发扣减
-- ============================================================

USE outdoor_gear_rental;

DROP TABLE IF EXISTS gear_stock_calendar;

CREATE TABLE gear_stock_calendar (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    gear_id     BIGINT      NOT NULL COMMENT '装备ID（关联 gear_info.id）',
    date        DATE        NOT NULL COMMENT '库存日期',
    stock       INT         NOT NULL DEFAULT 0 COMMENT '当日剩余可租库存',
    version     INT         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_gear_date (gear_id, date),
    KEY idx_gear_id (gear_id),
    KEY idx_date (date),
    CONSTRAINT fk_stock_calendar_gear
        FOREIGN KEY (gear_id) REFERENCES gear_info (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_stock_calendar_stock CHECK (stock >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='装备库存日历表';

-- 示例：为装备 ID=1 初始化未来 7 天库存（可按 gear_info.total_stock 批量生成）
-- INSERT INTO gear_stock_calendar (gear_id, date, stock, version)
-- SELECT 1, DATE_ADD(CURDATE(), INTERVAL n DAY), gi.total_stock, 0
-- FROM gear_info gi
-- CROSS JOIN (SELECT 0 n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6) days
-- WHERE gi.id = 1;
