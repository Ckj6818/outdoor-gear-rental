<div align="center">

# ⚡ 3 分钟快速体验指南

**面向 HR / 面试官** — 无需阅读全部代码，按以下步骤即可快速了解项目完成度与交互体验

<br/>

[![GitHub 仓库](https://img.shields.io/badge/GitHub-outdoor--gear--rental-181717?style=flat-square&logo=github)](https://github.com/Ckj6818/outdoor-gear-rental)
[![HR 一页纸](https://img.shields.io/badge/📄-HR_一页纸-2563eb?style=flat-square)](./HR.md)
[![← 返回 README](https://img.shields.io/badge/←-返回_README-64748b?style=flat-square)](../README.md)

<br/>

| 步骤 | 内容 | 预计耗时 |
|:--:|:--|:--:|
| **1** | [启动项目](#第一步启动约-2-分钟) | ~2 min |
| **2** | [AI 导购体验](#第二步ai-导购体验约-30-秒) | ~30 s |
| **3** | [用户端租赁](#第三步用户端租赁体验约-1-分钟) | ~1 min |
| **4** | [管理端体验](#第四步管理端体验约-2-分钟) | ~2 min |

</div>

---

## 第一步：启动（约 2 分钟）

### 选项 A：Docker（MySQL + Redis）

```powershell
docker compose -p outdoor-gear up -d mysql redis
$env:MYSQL_PASSWORD = "123456"
.\scripts\run-dev.ps1
```

### 选项 B：纯本地

```powershell
# 1. 初始化数据库（含 gear_item 实例种子数据）
mysql -u root -p < sql/init.sql

# 已有库若下单报「暂无可用实例」，执行：
# mysql -u root -p outdoor_gear_rental < sql/seed_gear_items.sql

# 2. 复制本地配置（可选：填入 AI_API_KEY 启用真实大模型）
copy src\main\resources\application-local.yml.example src\main\resources\application-local.yml

# 3. 一键启动前后端
$env:MYSQL_PASSWORD = "123456"
.\scripts\run-dev.ps1
```

> 🌐 浏览器访问：**http://localhost:5173**

> ⚠️ 修改后端代码后需**重启 Spring Boot**，新 API 才会生效。  
> 在 Cursor 中请用 `run-dev.ps1` 或 Run Task 启动，勿用断点调试模式跑日常开发。

---

## 第二步：AI 导购体验（约 30 秒）

<table>
<tr>
<th width="8%">#</th>
<th width="22%">操作</th>
<th width="70%">可观察的能力</th>
</tr>
<tr>
<td align="center">1️⃣</td>
<td>点击右下角 <strong>「AI 导购」</strong> 悬浮按钮</td>
<td>CUI 对话式入口，悬浮窗交互</td>
</tr>
<tr>
<td align="center">2️⃣</td>
<td>输入「去武功山」或「徒步需要租什么」</td>
<td>后端 RAG 检索可租库存上下文</td>
</tr>
<tr>
<td align="center">3️⃣</td>
<td>查看回复下方的 <strong>推荐装备卡片</strong></td>
<td>LLM / Mock 结构化 JSON → 前端动态渲染</td>
</tr>
<tr>
<td align="center">4️⃣</td>
<td>点击卡片 <strong>「去租赁」</strong></td>
<td>跳转装备大厅并打开详情，缩短决策链路</td>
</tr>
</table>

> 💡 未配置 API Key 时走 **Mock 模式**，仍可演示推荐卡片效果。

<details>
<summary><strong>🔍 搜索提示</strong></summary>

装备名称与品牌分字段存储：

- 搜索「Hikelite」或「Osprey」即可命中
- 完整「品牌 + 型号」需分词匹配（如同时包含两个词）

</details>

---

## 第三步：用户端租赁体验（约 1 分钟）

<table>
<tr>
<th width="8%">#</th>
<th width="28%">操作</th>
<th width="64%">可观察的能力</th>
</tr>
<tr>
<td align="center">1️⃣</td>
<td>使用 <code>zhangsan</code> / <code>123456</code> 登录</td>
<td>Sa-Token 鉴权、路由守卫</td>
</tr>
<tr>
<td align="center">2️⃣</td>
<td>浏览 <strong>装备大厅</strong>，点击装备卡片</td>
<td>筛选侧栏、详情弹窗、技术参数展示</td>
</tr>
<tr>
<td align="center">3️⃣</td>
<td>「立即租赁」→ <strong>选择起租/归还日期</strong></td>
<td>占用档期灰色禁用、跨占用区间不可选</td>
</tr>
<tr>
<td align="center">4️⃣</td>
<td>查看 <strong>预计总租金</strong></td>
<td>天数 × 日租金 + 押金，费用明细自动计算</td>
</tr>
<tr>
<td align="center">5️⃣</td>
<td><strong>我的订单</strong> → 支付 → 归还</td>
<td>卡片式 UI、完整状态流转</td>
</tr>
</table>

> 🔑 若登录失败，对已有库执行 `sql/fix_user_passwords.sql` 重置测试账号密码。

---

## 第四步：管理端体验（约 2 分钟）

使用 `admin` / `123456` 登录，点击右上角头像进入管理后台：

| 菜单 | 路径 | 可观察的能力 |
|:--|:--|:--|
| 📊 **运营数据大屏** | `/admin/dashboard` | ECharts 折线/饼图、今日营收等 KPI |
| 📦 **全部订单管理** | `/admin/orders` | 订单图表大盘 + 表格筛选与质检 |
| 🎒 **装备管理** | `/admin/gears` | 装备 CRUD、图片预览、上下架 |
| 👥 **管理员账号** | `/admin/users` | 管理员增删改、状态开关 |

📖 **API 文档（Knife4j）：** http://localhost:8081/doc.html

---

## ✨ 加分项（UI / 产品感）

<table>
<tr>
<td width="33%" align="center">

**📅 档期日历选租**

类似 Airbnb / 神州租车的日期区间交互，已占用日期自动禁用

</td>
<td width="33%" align="center">

**🤖 AI 推荐卡片**

对话 + 可点击租赁，缩短用户决策链路

</td>
<td width="33%" align="center">

**📰 杂志风内容页**

装备评测、户外技能、周边路线、环保倡议

</td>
</tr>
</table>

---

## 🔍 建议关注的代码入口

| 关注点 | 文件路径 |
|:--|:--|
| ⚡ 高并发抢租（三层防护） | `RentalOrderServiceImpl.java` + `RedisGearStockService.java` |
| 🔴 Redis Lua 库存预扣减 | `RedisGearStockService.java` |
| 📦 SKU 实例锁定下单 | `RentalOrderTxServiceImpl.java` + `GearItemMapper.xml` |
| 📅 租赁档期冲突校验 | `RentalOrderServiceImpl.java` → `listOccupiedDates` / `checkDateAvailable` |
| 🗓 档期选择下单 UI | `frontend/src/components/GearBookingModal.vue` |
| 🤖 AI 导购（RAG + LLM） | `AiConsultantServiceImpl.java` + `AiChatBox.vue` |
| 🔐 Sa-Token 鉴权 | `SaTokenConfigure.java` + `AuthServiceImpl.java` |
| ⏱ 待支付订单超时取消 | `OrderCancelRedisServiceImpl.java` + Redis 过期监听 |
| ✅ 质检闭环 | `AdminRentalOrderController` + `inspectOrder` |
| 🎒 装备管理后台 | `SysGearController.java` + `GearManage.vue` |
| 🏔 装备大厅 UI | `frontend/src/views/GearList.vue` |

---

## 🎤 答辩话术参考（可选）

> 为解决传统租赁平台转化率低、新手门槛高的痛点，我设计并落地了**基于轻量级 RAG 的对话式导购（CUI）模块**，通过在服务端动态挂载实时库存上下文，结合 Prompt Engineering 约束 LLM 输出格式化 JSON，在前端实现**动态推荐卡片的无缝渲染**；下单链路基于 **gear_item SKU 实例**锁定库存，配合**档期冲突校验、日历选租**与 **Redis Lua 预扣减**，保障高并发下的库存安全。

---

<div align="center">

<br/>

[← 返回 README](../README.md) · [HR 一页纸说明](./HR.md)

</div>
