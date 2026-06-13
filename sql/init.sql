-- ============================================================
-- 户外装备租赁系统 - 数据库初始化脚本
-- 引擎: InnoDB | 字符集: utf8mb4 | 规范: 主外键规范化
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- 创建数据库
-- ------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS outdoor_gear_rental
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE outdoor_gear_rental;

-- ------------------------------------------------------------
-- 1. 用户表 sys_user
-- ------------------------------------------------------------
DROP TABLE IF EXISTS rental_order;
DROP TABLE IF EXISTS gear_info;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '用户主键ID',
    username        VARCHAR(50)     NOT NULL COMMENT '登录用户名',
    password        VARCHAR(128)    NOT NULL COMMENT '登录密码（建议存储BCrypt哈希值）',
    role            TINYINT         NOT NULL DEFAULT 1 COMMENT '角色：0-管理员，1-普通玩家',
    phone           VARCHAR(20)     DEFAULT NULL COMMENT '联系电话',
    email           VARCHAR(100)    DEFAULT NULL COMMENT '电子邮箱',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '账号状态：0-禁用，1-正常',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_role (role),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- ------------------------------------------------------------
-- 2. 装备信息表 gear_info
-- ------------------------------------------------------------
CREATE TABLE gear_info (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '装备主键ID',
    gear_name           VARCHAR(100)    NOT NULL COMMENT '装备名称',
    brand               VARCHAR(50)     NOT NULL COMMENT '品牌',
    category            VARCHAR(50)     NOT NULL COMMENT '装备分类（如：重装背包、帐篷、徒步鞋）',
    daily_rent          DECIMAL(10, 2)  NOT NULL COMMENT '日租金（元/天）',
    total_stock         INT             NOT NULL DEFAULT 0 COMMENT '总库存量',
    available_stock     INT             NOT NULL DEFAULT 0 COMMENT '当前可用库存',
    description         VARCHAR(500)    DEFAULT NULL COMMENT '装备描述',
    status              TINYINT         NOT NULL DEFAULT 1 COMMENT '上架状态：0-下架，1-上架',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_category (category),
    KEY idx_brand (brand),
    KEY idx_status (status),
    CONSTRAINT chk_gear_stock CHECK (available_stock >= 0 AND available_stock <= total_stock),
    CONSTRAINT chk_daily_rent CHECK (daily_rent >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='户外装备信息表';

-- ------------------------------------------------------------
-- 3. 租赁订单表 rental_order
-- ------------------------------------------------------------
CREATE TABLE rental_order (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '订单主键ID',
    order_no                VARCHAR(32)     NOT NULL COMMENT '订单编号（业务唯一标识）',
    user_id                 BIGINT          NOT NULL COMMENT '租赁用户ID',
    gear_id                 BIGINT          NOT NULL COMMENT '租赁装备ID',
    rental_days             INT             NOT NULL COMMENT '租赁天数',
    rent_out_time           DATETIME        DEFAULT NULL COMMENT '借出时间',
    expected_return_time    DATETIME        DEFAULT NULL COMMENT '预计归还时间',
    actual_return_time      DATETIME        DEFAULT NULL COMMENT '实际归还时间',
    order_status            TINYINT         NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-借出中，2-已逾期，3-已归还',
    total_fee               DECIMAL(10, 2)  NOT NULL DEFAULT 0.00 COMMENT '订单总费用（元）',
    remark                  VARCHAR(255)    DEFAULT NULL COMMENT '备注',
    create_time             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_gear_id (gear_id),
    KEY idx_order_status (order_status),
    KEY idx_rent_out_time (rent_out_time),
    CONSTRAINT fk_rental_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_rental_gear FOREIGN KEY (gear_id) REFERENCES gear_info (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_rental_days CHECK (rental_days > 0),
    CONSTRAINT chk_total_fee CHECK (total_fee >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='装备租赁订单表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 测试数据
-- ============================================================

-- ------------------------------------------------------------
-- 用户测试数据
-- 密码均为 123456 的 BCrypt 哈希（$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH 为示例占位）
-- 实际开发中请使用后端统一加密策略
-- ------------------------------------------------------------
INSERT INTO sys_user (username, password, role, phone, email, status) VALUES
('admin',    '$2a$10$7EqJtq98hPCHXmF6i5gK0e7vK8Y5xQZ3mN2pL1oK9jH8gF7dS6aW5', 0, '13800000001', 'admin@outdoor-rental.com',  1),
('zhangsan', '$2a$10$7EqJtq98hPCHXmF6i5gK0e7vK8Y5xQZ3mN2pL1oK9jH8gF7dS6aW5', 1, '13800000002', 'zhangsan@example.com',      1),
('lisi',     '$2a$10$7EqJtq98hPCHXmF6i5gK0e7vK8Y5xQZ3mN2pL1oK9jH8gF7dS6aW5', 1, '13800000003', 'lisi@example.com',          1),
('wangwu',   '$2a$10$7EqJtq98hPCHXmF6i5gK0e7vK8Y5xQZ3mN2pL1oK9jH8gF7dS6aW5', 1, '13800000004', 'wangwu@example.com',        1);

-- ------------------------------------------------------------
-- 装备测试数据
-- ------------------------------------------------------------
INSERT INTO gear_info (gear_name, brand, category, daily_rent, total_stock, available_stock, description, status) VALUES
-- 背包类
('Stratos 36L 徒步背包',       'Osprey',        '重装背包',   45.00, 8,  6,  'Osprey Stratos 36L，AirSpeed 空速背板，适合2-3日轻装徒步，含防雨罩。',           1),
('Terraframe 65 重装背包',     'Mystery Ranch', '重装背包',   80.00, 5,  4,  'Mystery Ranch Terraframe 65，Guide Light 背负系统，适合多日重装穿越与登山。',       1),
('Hikelite 26 轻量化背包',     'Osprey',        '轻量化背包', 35.00, 10, 9,  'Osprey Hikelite 26，AirSpeed 背板，自重约740g，适合一日徒步与轻装出行。',          1),
('Exos 58 超轻背包',           'Osprey',        '轻量化背包', 55.00, 6,  5,  'Osprey Exos 58，Frameless 超轻设计，适合长距离轻装徒步。',                          1),
('Radix 31 多日背包',          'Mystery Ranch', '重装背包',   60.00, 4,  3,  'Mystery Ranch Radix 31，Dynamic Flex 背负，兼顾多日徒步与单日速穿。',               1),

-- 帐篷类
('Hubba Hubba NX 2人帐篷',     'MSR',           '帐篷',       70.00, 6,  5,  'MSR Hubba Hubba NX 2，经典双人轻量化帐篷，三季适用，快速搭建。',                    1),
('Cloud Up 2 双人帐篷',        'Naturehike',    '帐篷',       40.00, 12, 10, '挪客 Cloud Up 2，210T 涤纶面料，适合入门露营与徒步露营。',                          1),

-- 平底徒步鞋类
('X Ultra 4 GTX 徒步鞋',       'Salomon',       '徒步鞋',     38.00, 15, 12, 'Salomon X Ultra 4 GTX，Gore-Tex 防水，Contagrip 大底，适合中低海拔徒步与单日穿越。', 1),
('Moab 3 GTX 中帮徒步鞋',      'Merrell',       '徒步鞋',     32.00, 12, 10, 'Merrell Moab 3 GTX，Vibram 大底，经典平底徒步鞋，舒适耐用。',                       1),
('Renegade GTX Mid 中帮鞋',    'Lowa',          '徒步鞋',     42.00, 8,  7,  'Lowa Renegade GTX Mid，德国工艺，DuraPU 中底，适合复杂地形徒步。',                  1),
('Trail GTX 低帮徒步鞋',       'Hoka',          '徒步鞋',     36.00, 10, 8,  'Hoka Trail GTX 低帮款，Meta-Rocker 滚动感中底，长距离徒步脚感舒适。',               1);

-- ------------------------------------------------------------
-- 租赁订单测试数据
-- ------------------------------------------------------------
INSERT INTO rental_order (
    order_no, user_id, gear_id, rental_days,
    rent_out_time, expected_return_time, actual_return_time,
    order_status, total_fee, remark
) VALUES
-- 已归还订单
('RO202506010001', 2, 1, 3,
 '2025-06-01 09:00:00', '2025-06-04 18:00:00', '2025-06-04 16:30:00',
 3, 135.00, '张三租赁 Osprey Stratos 36L，按时归还'),

('RO202506020002', 3, 2, 5,
 '2025-06-02 10:00:00', '2025-06-07 18:00:00', '2025-06-07 17:00:00',
 3, 400.00, '李四租赁 Mystery Ranch 重装包，用于武功山穿越'),

-- 借出中订单
('RO202506100003', 4, 3, 2,
 '2025-06-10 08:30:00', '2025-06-12 18:00:00', NULL,
 1, 70.00, '王五租赁 Hikelite 轻量化背包，一日徒步'),

('RO202506110004', 2, 8, 4,
 '2025-06-11 14:00:00', '2025-06-15 18:00:00', NULL,
 1, 152.00, '张三租赁 Salomon X Ultra 4 GTX 徒步鞋'),

-- 已逾期订单
('RO202506050005', 3, 9, 3,
 '2025-06-05 09:00:00', '2025-06-08 18:00:00', NULL,
 2, 96.00, '李四租赁 Merrell Moab 3 GTX，尚未归还'),

-- 待支付订单
('RO202506130006', 4, 6, 2,
 NULL, NULL, NULL,
 0, 140.00, '王五预约 MSR Hubba Hubba NX 2 人帐篷，待支付');
