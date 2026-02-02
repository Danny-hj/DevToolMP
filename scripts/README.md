# DevToolMP 启停脚本使用说明

## 脚本列表

| 脚本 | 说明 |
|------|------|
| `start.sh` | 启动所有服务(Docker + 后端 + 前端) |
| `stop.sh` | 停止所有服务 |
| `restart.sh` | 重启所有服务 |
| `dev.sh` | 开发模式(仅启动Docker服务) |
| `status.sh` | 查看所有服务运行状态 |
| `logs.sh` | 查看服务日志 |
| `clean.sh` | 清理所有数据和缓存 |

---

## 快速开始

### 1. 一键启动(推荐)
```bash
./scripts/start.sh
```
此命令将:
- 启动 Docker 服务(MySQL, Redis)
- 等待数据库初始化完成
- 自动编译并启动后端服务(如需要)
- 自动安装依赖并启动前端服务(如需要)
- 在 `logs/` 目录记录日志

### 2. 开发模式
如果你需要在独立终端启动前后端进行调试:
```bash
./scripts/dev.sh
```
然后在不同终端中手动启动:
- **后端**: `cd backend && mvn spring-boot:run`
- **前端**: `cd frontend && npm run dev`

### 3. 查看状态
```bash
./scripts/status.sh
```
显示所有服务的运行状态和端口占用情况。

### 4. 查看日志
```bash
# 查看后端日志
./scripts/logs.sh backend

# 查看前端日志
./scripts/logs.sh frontend

# 查看Docker服务日志
./scripts/logs.sh docker
```

### 5. 停止服务
```bash
./scripts/stop.sh
```

### 6. 重启服务
```bash
./scripts/restart.sh
```

### 7. 清理数据
```bash
./scripts/clean.sh
```
⚠️ **警告**: 此操作将删除所有数据,包括:
- Docker 数据卷(数据库数据)
- 日志文件
- Maven 构建缓存
- 前端构建缓存

---

## 服务端口

| 服务 | 地址 | 端口 |
|------|------|------|
| 前端 | http://localhost | 5173 |
| 后端 API | http://localhost/api | 8080 |
| MySQL | localhost | 3306 |
| Redis | localhost | 6379 |

---

## 目录结构

```
DevToolMP/
├── backend/              # Spring Boot 后端
│   └── target/          # Maven 编译输出
├── frontend/            # Vue 3 前端
│   ├── node_modules/    # npm 依赖
│   └── dist/           # 构建输出
├── logs/               # 服务日志目录
│   ├── backend.log     # 后端日志
│   ├── frontend.log    # 前端日志
│   ├── backend.pid     # 后端进程ID
│   └── frontend.pid    # 前端进程ID
├── scripts/            # 启停脚本目录
│   ├── start.sh        # 启动脚本
│   ├── stop.sh         # 停止脚本
│   ├── restart.sh      # 重启脚本
│   ├── dev.sh          # 开发模式
│   ├── status.sh       # 状态查看
│   ├── logs.sh         # 日志查看
│   └── clean.sh        # 清理脚本
└── docker-compose.yml  # Docker 服务配置
```

---

## 常见问题

### 1. 首次启动很慢?
- 首次启动需要初始化 MySQL 数据库,可能需要 1-2 分钟
- 首次启动会自动编译后端和安装前端依赖

### 2. 端口冲突?
如果 5173 或 8080 端口被占用,可以修改:
- 前端端口: `frontend/vite.config.js`
- 后端端口: `backend/src/main/resources/application.yml`

### 3. 数据库连接失败?
确保 Docker 服务已启动:
```bash
docker ps
```
检查 MySQL 容器是否正常运行。

### 4. 如何查看实时日志?
```bash
# 查看后端实时日志
tail -f logs/backend.log

# 查看前端实时日志
tail -f logs/frontend.log
```

### 5. 服务启动失败?
1. 检查日志: `./scripts/logs.sh backend` 或 `frontend`
2. 检查状态: `./scripts/status.sh`
3. 尝试清理并重启: `./scripts/clean.sh && ./scripts/start.sh`

---

## 开发环境要求

- Docker & Docker Compose
- Java 21+
- Maven 3.6+
- Node.js 16+
- npm 或 yarn

---

## 注意事项

1. **日志管理**: 日志文件会持续增长,定期使用 `clean.sh` 清理
2. **数据备份**: `clean.sh` 会删除所有数据,执行前请备份
3. **进程管理**: 脚本使用 PID 文件管理进程,不要手动删除 `logs/*.pid`
4. **Docker 数据**: Docker 数据卷会持久化数据,需要用 `clean.sh` 清除

---

## 快捷命令

```bash
# 快速查看后端日志
tail -f logs/backend.log

# 快速查看前端日志
tail -f logs/frontend.log

# 快速重启后端
cd backend && mvn spring-boot:run

# 快速重启前端
cd frontend && npm run dev

# 查看 Docker 日志
docker logs -f devtoolmp-mysql
docker logs -f devtoolmp-redis
```
