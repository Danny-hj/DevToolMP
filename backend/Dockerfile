# 多阶段构建 - 构建阶段
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# 复制 pom.xml 并下载依赖（利用Docker缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src

# 跳过测试构建项目
RUN mvn clean package -DskipTests -B && \
    mv target/*.jar app.jar

# 运行阶段
FROM eclipse-temurin:21-jre-alpine

# 安装curl用于健康检查
RUN apk add --no-cache curl

WORKDIR /app

# 从构建阶段复制jar文件
COPY --from=build /app/app.jar app.jar

# 暴露端口
EXPOSE 8080

# JVM优化参数
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/tools || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
