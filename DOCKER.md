# DevToolMP Docker 配置与使用指南

## 概述

本文档描述 DevToolMP 项目的 Docker 配置和使用方法。

## 服务架构

```
┌─────────────────────────────────────────────┐
│              DevToolMP 系统                  │
├─────────────────────────────────────────────┤
│  Frontend (Vue 3 + Vite)  → 端口 5173       │
│  Backend (Spring Boot)    → 端口 8080       │
│  MySQL 8.0                → 端口 3306       │
│  Redis 7                  → 端口 6379       │
└─────────────────────────────────────────────┘
```

## 前置要求

- Docker Desktop (Mac/Windows) 或 Docker Engine (Linux)
- Docker Compose V2
- 或者本地环境：
  - Maven 3.6+
  - JDK 21
  - Node.js 18+

## Docker 配置文件

### docker-compose.yml
开发环境配置，包含：
- MySQL 8.0（自动挂载schema.sql和data.sql进行初始化）
- Redis 7 Alpine（开启AOF持久化）
- 后端服务（可本地运行或Docker）
- 前端服务（可本地运行或Docker）

**主要特性**：
- ✅ 健康检查配置（确保服务可用性）
- ✅ 自定义网络（devtoolmp-network）
- ✅ 数据持久化卷（mysql-data、redis-data）
- ✅ 服务依赖管理（depends_on + healthcheck）
- ✅ 环境变量配置
- ✅ 重启策略（unless-stopped）

### Dockerfile

#### backend/Dockerfile
多阶段构建，优化镜像大小：
```dockerfile
# 构建阶段
FROM maven:3.9-eclipse-temurin-21-alpine AS build
...

# 运行阶段
FROM eclipse-temurin:21-jre-alpine
...
```

#### frontend/Dockerfile
使用 Node.js 20 Alpine：
```dockerfile
FROM node:20-alpine
...
```

## 使用方法

### 方式一：完全 Docker 部署

```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down

# 停止并清理数据
docker-compose down -v
```

### 方式二：混合部署（推荐用于开发）

```bash
# 1. 只启动基础设施（MySQL + Redis）
docker-compose up -d mysql redis

# 2. 等待服务就绪
docker-compose ps

# 3. 启动后端（本地）
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 4. 启动前端（本地）
cd frontend
npm run dev
```

### 方式三：完全本地运行

```bash
# 1. 启动 MySQL 和 Redis
docker-compose up -d mysql redis

# 2. 配置 application-dev.yml
# spring.datasource.url=jdbc:mysql://localhost:3306/devtoolmp
# spring.data.redis.host=localhost

# 3. 启动后端和前端
cd backend && mvn spring-boot:run
cd frontend && npm run dev
```

## 服务端口

| 服务 | 端口 | 用途 |
|------|------|------|
| Frontend | 5173 | Vue.js 开发服务器 |
| Backend API | 8080 | Spring Boot REST API |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存/会话 |

## 环境变量

### MySQL
- `MYSQL_ROOT_PASSWORD`: root密码 (默认: rootpassword)
- `MYSQL_DATABASE`: 数据库名 (默认: devtoolmp)
- `MYSQL_USER`: 应用用户 (默认: devtool)
- `MYSQL_PASSWORD`: 应用密码 (默认: devtool123)

### Redis
- 无需密码（开发环境）

### Backend
- `SPRING_PROFILES_ACTIVE`: Spring配置文件 (dev/test/prod)
- `SPRING_DATASOURCE_URL`: JDBC URL
- `SPRING_DATASOURCE_USERNAME`: 数据库用户
- `SPRING_DATASOURCE_PASSWORD`: 数据库密码
- `SPRING_DATA_REDIS_HOST`: Redis主机
- `SPRING_DATA_REDIS_PORT`: Redis端口

### Frontend
- `VITE_API_BASE_URL`: API基础URL

## 数据库初始化

### 自动初始化
MySQL容器首次启动时，会自动执行挂载的SQL文件：
- `backend/src/main/resources/schema.sql`: 创建所有表结构
- `backend/src/main/resources/data.sql`: 插入示例数据

Docker Compose配置：
```yaml
volumes:
  - mysql-data:/var/lib/mysql
  - ./backend/src/main/resources/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql
  - ./backend/src/main/resources/data.sql:/docker-entrypoint-initdb.d/02-data.sql
```

### 重新初始化数据库
```bash
# 停止并删除容器和数据卷
docker-compose down -v

# 重新启动（会重新执行SQL文件）
docker-compose up -d mysql redis
```

## 健康检查

### MySQL健康检查
```yaml
healthcheck:
  test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-prootpassword"]
  timeout: 20s
  retries: 10
  interval: 10s
  start_period: 30s
```

### Redis健康检查
```yaml
healthcheck:
  test: ["CMD", "redis-cli", "ping"]
  interval: 10s
  timeout: 3s
  retries: 5
  start_period: 10s
```

### Backend健康检查
```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/api/tools"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 60s
```

### Frontend健康检查
```yaml
healthcheck:
  test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:5173"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 30s
```

### 手动检查
```bash
# 查看所有服务健康状态
docker-compose ps

# 手动检查各个服务
curl http://localhost:8080/api/tools              # 后端API
curl http://localhost:5173                        # 前端页面
docker exec devtoolmp-mysql mysqladmin ping       # MySQL
docker exec devtoolmp-redis redis-cli ping        # Redis
```

## 日志查看

```bash
# 查看所有日志
docker-compose logs

# 查看特定服务日志
docker-compose logs backend
docker-compose logs mysql

# 实时跟踪日志
docker-compose logs -f backend
```

## 故障排除

### 端口冲突
**症状**: 端口已被占用，容器无法启动

**解决方案**:
```bash
# 查看端口占用
lsof -i :3306  # MySQL
lsof -i :6379  # Redis
lsof -i :8080  # Backend
lsof -i :5173  # Frontend

# 修改docker-compose.yml中的端口映射
ports:
  - "3307:3306"  # MySQL改为3307
  - "6380:6379"  # Redis改为6380
  - "8081:8080"  # Backend改为8081
  - "5174:5173"  # Frontend改为5174
```

### 数据库连接失败
**症状**: Backend无法连接到MySQL

**排查步骤**:
1. 检查MySQL容器健康状态：
   ```bash
   docker-compose ps
   ```

2. 查看MySQL日志：
   ```bash
   docker-compose logs mysql
   ```

3. 等待健康检查完成（约30秒）：
   ```bash
   # 观察健康状态变化
   watch -n 2 'docker-compose ps'
   ```

4. 测试数据库连接：
   ```bash
   docker exec -it devtoolmp-mysql mysql -udevtool -pdevtool123 devtoolmp
   ```

### 后端启动失败
**症状**: Backend容器启动后立即退出

**排查步骤**:
1. 检查环境变量配置：
   ```bash
   docker-compose config
   ```

2. 确认MySQL和Redis已就绪：
   ```bash
   docker-compose ps
   ```

3. 查看后端详细日志：
   ```bash
   docker-compose logs backend
   ```

4. 进入容器调试：
   ```bash
   docker exec -it devtoolmp-backend sh
   ```

### 前端无法访问后端
**症状**: 前端页面报错，无法调用API

**排查步骤**:
1. 确认后端已启动：
   ```bash
   curl http://localhost:8080/api/tools
   ```

2. 检查Vite代理配置（`frontend/vite.config.js`）：
   ```javascript
   server: {
     proxy: {
       '/api': {
         target: 'http://localhost:8080',
         changeOrigin: true
       }
     }
   }
   ```

3. 确认环境变量：
   ```bash
   docker-compose exec frontend env | grep API
   ```

### 数据未正确初始化
**症状**: 数据库为空，缺少示例数据

**解决方案**:
```bash
# 完全删除容器和数据卷
docker-compose down -v

# 重新启动（会重新执行SQL文件）
docker-compose up -d mysql redis

# 等待初始化完成
docker-compose logs -f mysql
```

## 生产环境部署

### 使用生产配置
```bash
# 构建并启动生产环境
docker-compose -f docker-compose.prod.yml up -d --build

# 查看日志
docker-compose -f docker-compose.prod.yml logs -f

# 停止服务
docker-compose -f docker-compose.prod.yml down
```

### 生产环境特性
生产环境配置（`docker-compose.prod.yml`）包括：
- 🌐 **前端优化**: 使用Nginx提供静态文件服务，支持Gzip压缩
- 🔧 **后端优化**: JVM参数调优，G1GC垃圾回收器
- 💾 **数据持久化**: Redis开启AOF持久化，MySQL数据卷持久化
- 📊 **监控配置**: 健康检查和重启策略
- 🔒 **安全配置**: 生产级环境变量配置
- 📈 **性能优化**: 连接池调优、资源限制

## 数据备份

### MySQL 备份
```bash
# 备份
docker exec devtoolmp-mysql mysqldump -udevtool -pdevtool123 devtoolmp > backup.sql

# 恢复
docker exec -i devtoolmp-mysql mysql -udevtool -pdevtool123 devtoolmp < backup.sql
```

### Redis 备份
```bash
# Redis 数据在卷中自动持久化
docker volume inspect devtoolmp_redis-data
```

## 性能优化

### 后端 JVM 参数
已在 Dockerfile 中配置：
```dockerfile
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
```

### MySQL 优化
在 `my.cnf` 中可根据需要调整：
```ini
innodb_buffer_pool_size = 1G
max_connections = 200
```

### Redis 优化
```yaml
command: redis-server --appendonly yes --maxmemory 256mb
```

## 监控

### 容器资源使用
```bash
docker stats
```

### 服务健康检查
```bash
watch -n 5 'docker-compose ps'
```

### API 健康检查
```bash
curl http://localhost:8080/api/tools
```

## 相关文档

- [架构设计文档](ARCHITECTURE_DESIGN.md)
- [实现设计文档](IMPLEMENTATION_DESIGN.md)
- [Spring Boot 文档](https://spring.io/projects/spring-boot)
- [Vue.js 文档](https://vuejs.org/)
- [Docker 文档](https://docs.docker.com/)
