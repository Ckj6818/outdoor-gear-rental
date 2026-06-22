# 山行 · 户外装备租赁系统

<p align="center">
  <strong>Spring Boot 3 + Vue 3 全栈实战项目</strong><br/>
  面向 HR / 面试官可快速体验 · 涵盖抢租、档期日历、AI 导购、质检闭环与完整管理后台
</p>

<p align="center">
  <a href="https://github.com/Ckj6818/outdoor-gear-rental"><img src="https://img.shields.io/badge/GitHub-在线仓库-181717?logo=github" alt="GitHub"></a>
  <a href="docs/HR.md"><img src="https://img.shields.io/badge/📄-HR一页纸说明-blue" alt="HR Guide"></a>
  <a href="docs/DEMO.md"><img src="https://img.shields.io/badge/⚡-3分钟体验-green" alt="Demo"></a>
  <img src="https://img.shields.io/badge/Java-17-orange?logo=openjdk" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-3.5-42b883?logo=vuedotjs" alt="Vue 3">
  <img src="https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql" alt="MySQL">
  <img src="https://img.shields.io/badge/Redis-缓存%26分布式锁-red?logo=redis" alt="Redis">
</p>

<p align="center">
  <a href="#-给-hr--面试官">给 HR</a> ·
  <a href="#-功能模块">功能模块</a> ·
  <a href="#-技术亮点">技术亮点</a> ·
  <a href="#-快速开始">快速开始</a> ·
  <a href="docs/HR.md">一页纸说明</a> ·
  <a href="docs/DEMO.md">3 分钟体验</a>
</p>

---

## 👔 给 HR / 面试官

| 项目 | 说明 |
|------|------|
| **是什么** | 一套完整可运行的户外装备租赁 Web 系统（用户端 + 管理后台 + AI 导购） |
| **解决什么问题** | 模拟真实租赁流程：选品 → AI 推荐 → 档期选择 → 抢租 → 支付 → 归还 → 质检 → 库存恢复 |
| **和普通 Demo 的区别** | SKU 单件追踪、Redis Lua 防超卖、租赁档期冲突校验、轻量级 RAG 对话导购、RBAC 权限、运营可视化 |
| **怎么快速看** | 本地启动后 3 分钟走通主流程 → **[docs/DEMO.md](docs/DEMO.md)** |
| **一页纸摘要** | 能力矩阵 + 页面清单 → **[docs/HR.md](docs/HR.md)** |

**仓库地址：** https://github.com/Ckj6818/outdoor-gear-rental

---

## 📌 项目概述

**山行（Outdoor Gear Rental）** 是个人全栈实战项目，业务闭环完整、代码结构清晰，适合作为**校招 / 实习 / 全栈岗位**的作品集展示。

> 浏览选品 → AI 导购推荐 → 档期日历选租 → 高并发抢租 → 支付借出 → 归还 → 质检 → 库存恢复 / 维修 → 资产折旧 → 运营分析

与「只按数量扣库存」的 Demo 不同，本项目实现 **SPU + SKU/SN 双层模型**：每件装备拥有唯一实例编号，借还全程可溯源。

| 维度 | 说明 |
|------|------|
| **项目类型** | 前后端分离全栈 Web 应用 |
| **业务领域** | O2O 租赁 / 库存与资产管理 |
| **代码规模** | 后端 50+ Java 类 · 前端 12+ 页面/组件 · MySQL 核心表 5+ |
| **适用场景** | 作品集 · 技术面试 Live Demo · 工程能力证明 |

---

## ✨ 功能模块

### 用户端

| 模块 | 亮点 |
|------|------|
| **装备大厅** | 侧栏多选筛选、排序分页、卡片 hover 换图、详情弹窗 |
| **租赁下单** | 日期区间选择器、占用档期禁用、租金 + 押金自动估算（`GearBookingModal`） |
| **AI 导购** | 右下角悬浮对话窗，RAG 挂载实时库存，结构化推荐卡片 +「去租赁」跳转 |
| **我的订单** | Dark Minimalism 卡片式订单页、Tab 筛选、支付/归还/取消 |
| **内容专栏** | 装备评测 / 户外技能 / 周边路线 / 环保倡议（杂志风 UI） |

### 管理后台（RBAC · 仅管理员）

| 模块 | 路径 | 亮点 |
|------|------|------|
| **运营数据大屏** | `/admin/dashboard` | ECharts 营收趋势、品类占比、核心 KPI |
| **订单管理** | `/admin/orders` | 上图下表：状态分布 / 流水趋势 / TOP 装备 + 订单表格 |
| **装备管理** | `/admin/gears` | 装备 CRUD、图片预览、上下架切换 |
| **管理员账号** | `/admin/users` | 管理员增删改、启用/禁用 |

### 业务流程

```mermaid
sequenceDiagram
    participant U as 用户
    participant AI as AI 导购
    participant S as 后端
    participant R as Redis
    participant D as MySQL

    U->>AI: 咨询「去露营租什么」
    AI->>D: RAG 检索可租装备
    AI-->>U: JSON 回复 + 推荐卡片
    U->>S: 选档期 + 抢租下单
    S->>R: 分布式锁 + Lua 预扣库存
    S->>D: 档期校验 + 锁定 gear_item + 扣库存
    U->>S: 归还装备
    S->>D: 订单→待质检
    Note over S,D: 管理员质检 → 实例回库 + 库存恢复 + 折旧
```

---

## 🛠 技术亮点

| 类别 | 实现 | 价值 |
|------|------|------|
| **库存模型** | `gear_info` + `gear_item` SKU 追踪 | 单件装备全生命周期 |
| **高并发抢租** | Redisson 锁 + Redis Lua 预扣减 + MySQL 行锁 | 三层防护防超卖 |
| **租赁档期** | 占用区间 API + 闭区间冲突算法 | 日历选租、归还日不可连租 |
| **AI 导购** | 轻量级 RAG + Prompt 结构化 JSON | 对话式推荐、缩短决策链路 |
| **缓存** | Spring Cache + Redis | 装备大厅读多写少加速 |
| **安全** | Sa-Token + BCrypt + RBAC | 无状态 Token 鉴权、权限隔离 |
| **审计** | `@LogOperation` AOP | 操作人 / IP / 耗时记录 |
| **前端** | Vue 3 + Element Plus + ECharts | 商业化 UI + 数据可视化 |

---

## 🚀 快速开始

### 环境

JDK 17+ · Maven 3.9+ · Node.js 18+ · MySQL 8+ · Redis 6+ · Docker（可选）

### 方式 A：Docker 一键编排（推荐演示）

```powershell
git clone https://github.com/Ckj6818/outdoor-gear-rental.git
cd outdoor-gear-rental

# 仅启动 MySQL + Redis（本地开发后端/前端）
docker compose -p outdoor-gear up -d mysql redis

# 或构建并启动全栈（MySQL + Redis + 后端 + 前端 Nginx）
docker compose up -d --build
```

- 全栈访问：http://localhost（前端） · http://localhost:8081/doc.html（Knife4j API 文档）
- 默认 MySQL 密码：`123456`（可通过环境变量 `MYSQL_PASSWORD` 覆盖）

### 方式 B：本地开发（前后端分离）

```powershell
# 1. 初始化数据库（首次）
mysql -u root -p < sql/init.sql

# 已有库时按需执行增量脚本，例如：
#   sql/seed_gear_items.sql      — 补全 gear_item 实例（下单必需）
#   sql/fix_user_passwords.sql   — 修复测试账号 BCrypt 密码

# 2. 复制本地配置（MySQL 密码、可选 AI Key）
copy src\main\resources\application-local.yml.example src\main\resources\application-local.yml

# 3. 一键启动（会打开两个 PowerShell 窗口）
.\scripts\run-dev.ps1
# 停止：.\scripts\stop-dev.ps1
```

手动启动：

```powershell
$env:MYSQL_PASSWORD = "123456"
.\scripts\run-backend.ps1    # → http://localhost:8081
.\scripts\run-frontend.ps1   # → http://localhost:5173
```

**Cursor / VS Code 提示：** 日常开发请用上方脚本或 **Tasks: Run Task → 全栈开发**，避免用「断点调试」启动 Vite/Spring Boot，否则停止调试会连带中断服务。

**AI 导购（可选）：** 在 `application-local.yml` 或环境变量 `AI_API_KEY` 中配置 DeepSeek 等 OpenAI 兼容 API Key。未配置时自动启用 Mock 模式（基于库存关键词推荐，便于答辩演示）。

> **注意：** 修改后端 Java 代码后需重启 Spring Boot，新接口才会生效。

### 3. 测试账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 管理员 | `admin` | `123456` |
| 用户 | `zhangsan` | `123456` |

---

## 📂 项目结构

```
outdoor-gear-rental/
├── docs/
│   ├── HR.md                 # 给 HR 的一页纸说明 ⭐
│   └── DEMO.md               # 3 分钟体验指南 ⭐
├── docker-compose.yml        # MySQL + Redis + 全栈编排
├── scripts/                  # run-dev / stop-dev 等启动脚本
├── sql/                      # init.sql + 增量迁移（gear_item 种子、密码修复等）
├── src/main/java/            # Spring Boot 后端
│   └── com/outdoor/rental/
│       ├── service/RedisGearStockService.java   # Lua 库存预扣减
│       ├── service/impl/AiConsultantServiceImpl.java  # RAG 导购
│       └── service/impl/RentalOrderServiceImpl.java   # 档期冲突校验
└── frontend/src/
    ├── components/
    │   ├── AiChatBox.vue           # AI 导购对话窗
    │   └── GearBookingModal.vue    # 档期选择下单弹窗
    ├── views/                      # 用户端 + admin 管理页
    └── api/                        # Axios 接口封装
```

---

## 🔌 主要 API（节选）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录 |
| GET | `/api/gears` | 装备列表（缓存） |
| POST | `/api/orders` | 下单抢租 |
| GET | `/api/orders/occupied-dates/{gearId}` | 装备已占用档期（日历禁用） |
| POST | `/api/ai/consult` | AI 导购对话（返回 JSON 字符串） |
| GET | `/api/admin/dashboard/stats` | 运营大屏 |
| GET | `/api/admin/system/gear` | 装备管理 |
| GET | `/api/admin/system/user` | 管理员管理 |
| POST | `/api/admin/orders/inspect` | 质检闭环 |
| GET | `/doc.html` | Knife4j 接口文档（Swagger UI） |

---

## 👤 作者

**GitHub：** [@Ckj6818](https://github.com/Ckj6818)

个人全栈实战项目 · 欢迎 **Star** · 问题请提 [Issue](https://github.com/Ckj6818/outdoor-gear-rental/issues)

---

## 📄 License

[MIT License](LICENSE)
