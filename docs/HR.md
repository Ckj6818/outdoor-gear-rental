<div align="center">

# 👔 给 HR / 面试官 · 一页纸项目说明

<br/>

[![项目名称](https://img.shields.io/badge/项目-山行_·_户外装备租赁系统-14532d?style=for-the-badge)](https://github.com/Ckj6818/outdoor-gear-rental)
[![体验时长](https://img.shields.io/badge/建议体验-3～5_分钟-2563eb?style=for-the-badge)](./DEMO.md)
[![项目性质](https://img.shields.io/badge/性质-个人全栈实战作品-7c3aed?style=for-the-badge)](https://github.com/Ckj6818/outdoor-gear-rental)

<br/>

[![← 返回 README](https://img.shields.io/badge/←-返回_README-64748b?style=flat-square)](../README.md)
[![3 分钟体验](https://img.shields.io/badge/⚡-3_分钟体验-16a34a?style=flat-square)](./DEMO.md)

<br/>

**仓库地址：** https://github.com/Ckj6818/outdoor-gear-rental  
**详细体验步骤：** [DEMO.md](./DEMO.md)

</div>

---

## ⏱ 30 秒看懂

这是一套**完整可运行**的装备租赁 Web 系统，模拟真实业务全流程：

```
用户选装备 → AI 导购推荐 → 档期日历选租 → 下单抢租 → 支付借出 → 归还 → 后台质检 → 库存恢复
```

> 不是简单的 CRUD Demo，而是包含 **SKU 单件追踪、Redis Lua 防超卖、租赁档期冲突校验、轻量级 RAG 对话导购、Sa-Token 权限、运营大屏** 等工程化能力。

<table>
<tr>
<td align="center" width="25%"><strong>50+</strong><br/>Java 类</td>
<td align="center" width="25%"><strong>12+</strong><br/>前端页面/组件</td>
<td align="center" width="25%"><strong>5+</strong><br/>MySQL 核心表</td>
<td align="center" width="25%"><strong>3 min</strong><br/>走通主流程</td>
</tr>
</table>

---

## 💪 我能证明什么能力

| 能力方向 | 具体体现 |
|:--|:--|
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

## 🚀 3 分钟怎么体验（无需看代码）

<table>
<tr>
<th width="8%">步骤</th>
<th width="92%">操作</th>
</tr>
<tr>
<td align="center"><strong>1</strong></td>
<td>打开 <strong>http://localhost:5173</strong>（按 <a href="../README.md">README</a> 启动后）</td>
</tr>
<tr>
<td align="center"><strong>2</strong></td>
<td>点击右下角 <strong>「AI 导购」</strong>，输入「去露营」→ 查看推荐卡片与「去租赁」跳转</td>
</tr>
<tr>
<td align="center"><strong>3</strong></td>
<td>用 <code>zhangsan / 123456</code> 登录 → 装备大厅 <strong>立即租赁</strong> → 选择日期区间 → 我的订单支付/归还</td>
</tr>
<tr>
<td align="center"><strong>4</strong></td>
<td>用 <code>admin / 123456</code> 登录 → 运营大屏 → 订单管理 → 装备管理 → 管理员账号</td>
</tr>
</table>

详细步骤见 **[DEMO.md](./DEMO.md)**。

### 🔑 测试账号

| 角色 | 账号 | 密码 |
|:--|:--|:--|
| 👑 管理员 | `admin` | `123456` |
| 👤 用户 | `zhangsan` | `123456` |

---

## 🗺 核心页面一览

| 模块 | 路径 / 入口 | 说明 |
|:--|:--|:--|
| 🏔 装备大厅 | `/gears` | 筛选、搜索、详情、档期选租下单 |
| 🤖 AI 导购 | 右下角悬浮按钮 | RAG 对话 + 装备推荐卡片 |
| 📋 我的订单 | `/orders` | 卡片式订单、支付/归还/取消 |
| 📊 运营大屏 | `/admin/dashboard` | ECharts 营收与品类可视化 |
| 📦 订单管理 | `/admin/orders` | 图表大盘 + 订单表格与质检 |
| 🎒 装备管理 | `/admin/gears` | 装备 CRUD、上下架 |
| 👥 管理员账号 | `/admin/users` | 管理员 CRUD、启用/禁用 |
| 📰 内容专栏 | `/reviews` 等 | 杂志风户外内容页（UI 能力展示） |

---

## 🧱 技术栈（面试可展开）

```
┌──────────────── Backend ────────────────┐
│  Java 17 · Spring Boot 3 · MyBatis-Plus │
│  Sa-Token · Redis · Redisson · Knife4j  │
└─────────────────────────────────────────┘
┌──────────────── Frontend ───────────────┐
│  Vue 3 · Element Plus · ECharts · Vite  │
└─────────────────────────────────────────┘
┌──────────────── Data & AI ────────────────┐
│  MySQL 8 · Docker Compose               │
│  DeepSeek 等 OpenAI 兼容 API（可选）     │
└─────────────────────────────────────────┘
```

| 层级 | 技术 |
|:--|:--|
| **后端** | Java 17 · Spring Boot 3 · MyBatis-Plus · Sa-Token · Redis · Redisson · Knife4j |
| **前端** | Vue 3 · Element Plus · ECharts · Vite（中文 locale） |
| **数据库** | MySQL 8 · Docker Compose 可选 |
| **AI（可选）** | DeepSeek 等 OpenAI 兼容 Chat Completions API |

---

## 📞 联系方式

<div align="center">

**GitHub：** [@Ckj6818](https://github.com/Ckj6818)

欢迎 ⭐ Star 本仓库。如需进一步了解实现细节，可按 [DEMO.md](./DEMO.md) 中的「建议关注的代码入口」快速定位核心逻辑。

<br/>

[← 返回 README](../README.md) · [3 分钟体验指南](./DEMO.md)

</div>
