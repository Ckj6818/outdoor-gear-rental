-- ============================================================
-- 根据 gear_info 库存批量生成 gear_item 实例（SKU/SN）
-- 适用：gear_item 为空但 gear_info.available_stock > 0 的环境
-- ============================================================

USE outdoor_gear_rental;

INSERT INTO gear_item (gear_id, sn_code, status)
SELECT
    g.id,
    CONCAT('SN-', g.id, '-', LPAD(n.n, 4, '0')),
    CASE WHEN n.n <= g.available_stock THEN 1 ELSE 2 END
FROM gear_info g
JOIN (
    SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
    UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
    UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
    UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
) n ON n.n <= g.total_stock
WHERE NOT EXISTS (SELECT 1 FROM gear_item gi WHERE gi.gear_id = g.id);
