# ============================================================
# 户外装备租赁系统 - Vue 前端（Node 构建 + Nginx 托管）
# 构建：docker build -t outdoor-gear-rental-web ./frontend
# 运行：docker run -p 80:80 outdoor-gear-rental-web
# ============================================================

# ---------- 阶段 1：Node 打包 ----------
FROM node:20-alpine AS build

WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm ci || npm install

COPY . .
RUN npm run build

# ---------- 阶段 2：Nginx 静态托管 ----------
FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
