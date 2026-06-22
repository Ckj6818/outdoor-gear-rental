# ============================================================
# 户外装备租赁系统 - Spring Boot 后端（多阶段构建）
# 构建：docker build -t outdoor-gear-rental-backend .
# 运行：docker run -p 8081:8081 outdoor-gear-rental-backend
# ============================================================

# ---------- 阶段 1：Maven 编译打包 ----------
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# 先复制 pom，利用 Docker 层缓存加速依赖下载
COPY pom.xml .
RUN mvn -B dependency:go-offline -DskipTests || true

COPY src ./src
RUN mvn -B -DskipTests package

# ---------- 阶段 2：JRE 运行 ----------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=build /app/target/gear-rental-*.jar app.jar

USER app

EXPOSE 8081

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
