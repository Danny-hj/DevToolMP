# DevToolMP - 开发者工具市场平台

一个一站式开发者工具发现、分享、评价和排行的平台。

## 快速开始

### 使用启动脚本（推荐）

**macOS/Linux:**
```bash
./start.sh
```

**Windows:**
```cmd
start.bat
```

### 手动启动

#### 1. 启动基础设施（MySQL + Redis）
```bash
docker-compose up -d mysql redis
```

#### 2. 启动后端
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### 3. 启动前端
```bash
cd frontend
npm install
npm run dev
```

## 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端页面 | http://localhost:5173 | Vue.js 前端界面 |
| 后端 API | http://localhost:8080/api | REST API |
| MySQL | localhost:3306 | 数据库 |
| Redis | localhost:6379 | 缓存服务 |

## 项目结构

```
DevToolMP/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/devtoolmp/
│   │       ├── config/      # 配置类
│   │       ├── controller/  # 控制器
│   │       ├── dto/         # 数据传输对象
│   │       ├── entity/      # 实体类
│   │       ├── mapper/      # MyBatis Mapper
│   │       ├── service/     # 业务服务
│   │       └── schedule/    # 定时任务
│   ├── src/main/resources/
│   │   ├── mapper/          # MyBatis XML
│   │   ├── schema.sql       # 数据库结构
│   │   ├── data.sql         # 初始数据
│   │   └── application*.yml # 配置文件
│   └── Dockerfile
├── frontend/                # Vue 3 前端
│   ├── src/
│   │   ├── api/            # API 封装
│   │   ├── components/     # 组件
│   │   ├── views/          # 页面视图
│   │   ├── stores/         # Pinia 状态管理
│   │   └── router/         # 路由配置
│   ├── vite.config.js
│   └── Dockerfile
├── docker-compose.yml       # Docker 编排配置
├── start.sh / start.bat     # 启动脚本
├── stop.sh                  # 停止脚本
├── ARCHITECTURE_DESIGN.md   # 架构设计文档
├── IMPLEMENTATION_DESIGN.md # 实现设计文档
└── DOCKER.md                # Docker 使用文档
```

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.1
- **Java 版本**: JDK 21
- **持久层**: MyBatis 3.0.3
- **数据库**: MySQL 8.0
- **缓存**: Redis 7
- **构建工具**: Maven

### 前端
- **框架**: Vue 3.3.8
- **构建工具**: Vite 5.0.4
- **UI 组件**: Element Plus 2.4.4
- **状态管理**: Pinia 2.1.7
- **路由**: Vue Router 4.2.5
- **HTTP 客户端**: Axios 1.6.2

## 核心功能

- 🔍 **工具搜索**: 按名称、描述、标签搜索
- 📊 **排行榜**: 日榜、周榜、总榜
- ⭐ **评价系统**: 评分、评论、回复、点赞
- ❤️ **收藏功能**: 收藏喜欢的工具
- 📈 **GitHub 集成**: 自动同步仓库数据
- 🏷️ **分类标签**: 多分类、多标签组织

## API 端点

### 工具管理
- `GET /api/tools` - 获取工具列表
- `GET /api/tools/{id}` - 获取工具详情
- `GET /api/tools/search?keyword=xxx` - 搜索工具
- `POST /api/tools/{id}/view` - 记录浏览
- `POST /api/tools/{id}/favorite` - 切换收藏

### 排行榜
- `GET /api/tools/ranking/daily` - 日榜
- `GET /api/tools/ranking/weekly` - 周榜
- `GET /api/tools/ranking/alltime` - 总榜

### 评价
- `GET /api/ratings/{toolId}` - 获取评价列表
- `POST /api/ratings` - 创建评价
- `POST /api/ratings/{id}/reply` - 回复评价
- `POST /api/ratings/{id}/like` - 点赞评价

## 测试

```bash
# 测试后端 API
curl http://localhost:8080/api/tools
curl http://localhost:8080/api/tools/1/detail
curl http://localhost:8080/api/tools/ranking/daily

# 测试前端
open http://localhost:5173
```

## 数据库

数据库在首次启动时自动初始化，包含：
- 8 张表（工具、分类、评价、收藏、浏览记录等）
- 5 个示例工具
- 示例评价和标签数据

## 环境配置

### 开发环境 (application-dev.yml)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/devtoolmp
    username: devtool
    password: devtool123
  data:
    redis:
      host: localhost
      port: 6379
```

### Docker 环境变量
```yaml
SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/devtoolmp
SPRING_DATASOURCE_USERNAME: devtool
SPRING_DATASOURCE_PASSWORD: devtool123
SPRING_DATA_REDIS_HOST: redis
```

## 停止服务

```bash
# 使用停止脚本
./stop.sh

# 或手动停止
docker-compose down
# Ctrl+C 停止本地服务
```

## 文档

- [架构设计文档](ARCHITECTURE_DESIGN.md) - 系统架构和模块设计
- [实现设计文档](IMPLEMENTATION_DESIGN.md) - 详细实现说明
- [Docker 使用文档](DOCKER.md) - Docker 配置和部署

## 开发指南

### 后端开发
```bash
cd backend
mvn spring-boot:run                    # 启动
mvn test                               # 测试
mvn clean package                      # 打包
```

### 前端开发
```bash
cd frontend
npm install                            # 安装依赖
npm run dev                            # 启动开发服务器
npm run build                          # 构建生产版本
npm run lint                           # 代码检查
```

## 故障排除

### 端口被占用
修改 `docker-compose.yml` 或 `application.yml` 中的端口配置

### 数据库连接失败
确保 MySQL 和 Redis 已启动：
```bash
docker-compose ps
```

### 后端启动失败
检查日志：
```bash
tail -f /private/tmp/claude-501/*/output
```

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！
