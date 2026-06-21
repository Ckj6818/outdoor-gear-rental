# 3 分钟快速体验指南

> 面向 **HR / 面试官**：无需阅读全部代码，按以下步骤即可快速了解项目完成度与交互体验。

**在线代码：** https://github.com/Ckj6818/outdoor-gear-rental

---

## 第一步：启动（约 2 分钟）

```bash
# 1. 初始化数据库
mysql -u root -p < sql/init.sql

# 2. 启动后端（PowerShell）
$env:MYSQL_PASSWORD = "你的MySQL密码"
mvn spring-boot:run

# 3. 启动前端
cd frontend && npm install && npm run dev
```

浏览器访问：**http://localhost:5173**

---

## 第二步：用户端体验（约 1 分钟）

| 步骤 | 操作 | 可观察的能力 |
|------|------|--------------|
| 1 | 使用 `zhangsan` / `123456` 登录 | JWT 鉴权、路由守卫 |
| 2 | 浏览**装备大厅**，点击装备卡片 | 筛选侧栏、详情弹窗、技术参数 |
| 3 | 「立即租赁」→ 选租期 → 确认下单 | 前后端联调、表单校验 |
| 4 | **我的订单** → 支付 → 归还 | 卡片式 UI、完整状态流转 |

---

## 第三步：管理端体验（约 2 分钟）

使用 `admin` / `123456` 登录，点击右上角头像：

| 菜单 | 路径 | 可观察的能力 |
|------|------|--------------|
| **运营数据大屏** | `/admin/dashboard` | ECharts 折线/饼图、今日营收等指标 |
| **全部订单管理** | `/admin/orders` | 订单图表大盘 + 表格筛选与质检 |
| **装备管理** | `/admin/gears` | 装备 CRUD、图片预览、上下架 |
| **管理员账号** | `/admin/users` | 管理员增删改、状态开关 |

---

## 加分项（UI / 产品感）

顶部二级导航可访问杂志风内容页（装备评测、户外技能、周边路线、环保倡议），展示前端排版与交互能力。

---

## 建议关注的代码入口

| 关注点 | 文件路径 |
|--------|----------|
| 高并发抢租 + SKU 锁定 | `RentalOrderTxServiceImpl.java` |
| Redis 缓存 | `GearInfoServiceImpl.java` + `RedisConfig.java` |
| JWT + RBAC | `SecurityConfig.java` + `JwtAuthenticationFilter.java` |
| 质检闭环 | `AdminRentalOrderController` + `inspectOrder` |
| 装备管理后台 | `SysGearController.java` + `GearManage.vue` |
| 管理员 CRUD | `SysUserController.java` + `AdminManage.vue` |
| 装备大厅 UI | `frontend/src/views/GearList.vue` |
| 订单卡片 UI | `frontend/src/views/MyOrders.vue` |
