# 给 HR / 面试官 · 一页纸项目说明

> **项目名称：** 山行 · 户外装备租赁系统  
> **仓库地址：** https://github.com/Ckj6818/outdoor-gear-rental  
> **项目性质：** 个人全栈实战作品（可本地运行、可在线浏览代码）  
> **建议体验时长：** 3～5 分钟（见 [DEMO.md](./DEMO.md)）

---

## 30 秒看懂

这是一套**完整可运行**的装备租赁 Web 系统，模拟真实业务：

**用户选装备 → AI 导购推荐 → 档期日历选租 → 下单抢租 → 支付借出 → 归还 → 后台质检 → 库存恢复**

不是简单的 CRUD Demo，而是包含 **SKU 单件追踪、Redis Lua 防超卖、租赁档期冲突校验、轻量级 RAG 对话导购、Sa-Token 权限、运营大屏** 等工程化能力。

---

## 我能证明什么能力

| 能力方向 | 具体体现 |
|----------|----------|
| **全栈开发** | Spring Boot 3 后端 + Vue 3 前端，前后端分离，RESTful API 联调 |
| **数据库设计** | MySQL 5 张核心表，SPU/SKU 双层库存模型，订单状态机 |
| **高并发 & 缓存** | Redisson 分布式锁 + Redis Lua 原子预扣减 + MySQL 行锁三层防超卖 |
| **业务建模** | 租赁档期占用区间查询、闭区间冲突算法（归还日不可连租） |
| **AI 应用** | 轻量级 RAG（动态挂载可租库存）+ Prompt 约束结构化 JSON + 前端推荐卡片渲染 |
| **工程化思维** | Redis 缓存、AOP 操作日志、统一异常与返回体、配置化 AI Key |
| **安全意识** | Sa-Token + BCrypt + RBAC 管理员/用户隔离 |
| **产品 & UI** | 档期日期选择器、AI 悬浮导购、装备大厅商业化卡片、管理后台大屏 |
| **部署与交付** | Docker Compose、README/DEMO 文档、SQL 种子与迁移脚本 |

---

## 3 分钟怎么体验（无需看代码）

1. 打开 **http://localhost:5173**（按 README 启动后）
2. 点击右下角 **「AI 导购」**，输入「去露营」→ 查看推荐卡片与「去租赁」跳转
3. 用 `zhangsan / 123456` 登录 → 装备大厅 **立即租赁** → 选择日期区间 → 我的订单支付/归还
4. 用 `admin / 123456` 登录 → 运营大屏 → 订单管理 → 装备管理 → 管理员账号

详细步骤见 **[DEMO.md](./DEMO.md)**。

---

## 核心页面一览

| 模块 | 路径 / 入口 | 说明 |
|------|-------------|------|
| 装备大厅 | `/gears` | 筛选、搜索、详情、档期选租下单 |
| AI 导购 | 右下角悬浮按钮 | RAG 对话 + 装备推荐卡片 |
| 我的订单 | `/orders` | 卡片式订单、支付/归还/取消 |
| 运营大屏 | `/admin/dashboard` | ECharts 营收与品类可视化 |
| 订单管理 | `/admin/orders` | 图表大盘 + 订单表格与质检 |
| 装备管理 | `/admin/gears` | 装备 CRUD、上下架 |
| 管理员账号 | `/admin/users` | 管理员 CRUD、启用/禁用 |
| 内容专栏 | `/reviews` 等 | 杂志风户外内容页（UI 能力展示） |

---

## 技术栈（面试可展开）

- **后端：** Java 17 · Spring Boot 3 · MyBatis-Plus · Sa-Token · Redis · Redisson · Knife4j  
- **前端：** Vue 3 · Element Plus · ECharts · Vite（中文 locale）  
- **数据库：** MySQL 8 · Docker Compose 可选  
- **AI（可选）：** DeepSeek 等 OpenAI 兼容 Chat Completions API  

---

## 联系方式

**GitHub：** [@Ckj6818](https://github.com/Ckj6818)

欢迎 Star 本仓库。如需进一步了解实现细节，可按 [DEMO.md](./DEMO.md) 中的「建议关注的代码入口」快速定位核心逻辑。
