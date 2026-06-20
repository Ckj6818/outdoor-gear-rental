# 3 分钟快速体验指南

> 面向 HR / 面试官：无需阅读全部代码，按以下步骤即可快速了解项目完成度与交互体验。

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

## 第二步：用户端体验（约 1 分钟）

| 步骤 | 操作 | 可观察的能力 |
|------|------|--------------|
| 1 | 使用 `zhangsan` / `123456` 登录 | JWT 鉴权、路由守卫 |
| 2 | 浏览装备大厅，点击任意装备卡片 | 商品化 UI、详情弹窗、技术参数展示 |
| 3 | 点击「立即租赁」→ 选择租期 → 确认下单 | 前后端联调、表单校验 |
| 4 | 进入「我的订单」→ 支付 → 归还 | 完整租赁状态流转 |

## 第三步：管理端体验（约 1 分钟）

| 步骤 | 操作 | 可观察的能力 |
|------|------|--------------|
| 1 | 退出后使用 `admin` / `123456` 登录 | RBAC 角色区分 |
| 2 | 头像菜单 → **后台质检中心** | 待质检订单处理、SKU 实例状态联动 |
| 3 | 头像菜单 → **运营数据大屏** | ECharts 数据可视化 |
| 4 | 查看后端控制台 | AOP 操作审计日志（下单/归还/质检） |

## 建议关注的代码入口

| 关注点 | 文件路径 |
|--------|----------|
| 高并发抢租 + SKU 锁定 | `RentalOrderTxServiceImpl.java` |
| Redis 缓存 | `GearInfoServiceImpl.java` + `RedisConfig.java` |
| JWT + RBAC | `SecurityConfig.java` + `JwtAuthenticationFilter.java` |
| 质检闭环 | `RentalOrderController.java`（admin inspect） |
| 装备大厅 UI | `frontend/src/views/GearList.vue` |
| 运营大屏 | `frontend/src/views/admin/Dashboard.vue` |
