# DevToolMP - 开发者工具市场平台

一个一站式开发者工具发现、分享、评价和排行的平台。

## 快速开始

### 使用启动脚本（推荐）

项目提供了便捷的启动脚本，自动化启动所有服务：

**macOS/Linux:**
```bash
# 启动所有服务（MySQL + Redis + Backend + Frontend）
./scripts/start.sh

# 停止所有服务
./scripts/stop.sh
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
├── backend/                      # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/devtoolmp/
│   │       ├── config/           # 配置类（CORS、缓存、RestTemplate等）
│   │       ├── controller/       # REST控制器
│   │       │   ├── ToolController      # 工具管理
│   │       │   ├── RatingController    # 评价管理
│   │       │   ├── RankingController   # 排行榜
│   │       │   ├── SearchController    # 搜索
│   │       │   ├── GitHubController    # GitHub集成
│   │       │   └── CategoryController  # 分类管理
│   │       ├── dto/              # 数据传输对象
│   │       │   ├── request/      # 请求DTO
│   │       │   └── response/     # 响应DTO
│   │       ├── entity/           # 实体类
│   │       │   ├── Tool.java
│   │       │   ├── Category.java
│   │       │   ├── Rating.java
│   │       │   ├── CommentReply.java
│   │       │   ├── Favorite.java
│   │       │   ├── ViewRecord.java
│   │       │   ├── ToolTag.java
│   │       │   └── RatingLike.java
│   │       ├── mapper/           # MyBatis Mapper接口
│   │       ├── service/          # 业务服务
│   │       │   ├── ToolService
│   │       │   ├── RatingService
│   │       │   ├── RankingService
│   │       │   ├── GitHubService
│   │       │   └── CategoryService
│   │       ├── schedule/         # 定时任务
│   │       │   └── GitHubSyncScheduler
│   │       └── exception/        # 异常处理
│   ├── src/main/resources/
│   │   ├── mapper/              # MyBatis XML映射文件
│   │   ├── schema.sql           # 数据库表结构定义
│   │   ├── data.sql             # 初始数据
│   │   └── application*.yml     # 配置文件
│   ├── Dockerfile               # 后端Docker镜像
│   └── pom.xml                  # Maven配置
├── frontend/                     # Vue 3 前端
│   ├── src/
│   │   ├── api/                 # API调用封装
│   │   ├── components/          # 可复用组件
│   │   │   ├── layout/         # 布局组件（Header、Footer）
│   │   │   ├── tool/           # 工具相关组件
│   │   │   ├── rating/         # 评价相关组件
│   │   │   └── home/           # 首页组件
│   │   ├── views/              # 页面视图
│   │   │   ├── Home.vue
│   │   │   ├── Tools.vue
│   │   │   ├── ToolDetail.vue
│   │   │   ├── Search.vue
│   │   │   └── Ranking.vue
│   │   ├── stores/             # Pinia状态管理
│   │   ├── router/             # Vue Router配置
│   │   ├── assets/             # 静态资源（样式、图片等）
│   │   ├── App.vue             # 根组件
│   │   └── main.js             # 入口文件
│   ├── Dockerfile              # 前端Docker镜像
│   ├── vite.config.js          # Vite构建配置
│   └── package.json            # NPM依赖配置
├── scripts/                     # 脚本目录
│   ├── start.sh                # 启动脚本
│   └── stop.sh                 # 停止脚本
├── docker-compose.yml           # Docker Compose开发环境配置
├── docker-compose.prod.yml      # Docker Compose生产环境配置
├── README.md                    # 项目说明文档
├── ARCHITECTURE_DESIGN.md       # 架构设计文档
├── IMPLEMENTATION_DESIGN.md     # 实现设计文档
└── DOCKER.md                    # Docker使用文档
```

## 技术栈

### 后端技术栈
- **框架**: Spring Boot 3.2.1
- **Java 版本**: JDK 21
- **持久层**: MyBatis 3.0.3（SQL映射和ORM）
- **数据库**: MySQL 8.0（关系型数据库）
- **缓存**: Redis 7（缓存和会话存储，Jedis客户端）
- **构建工具**: Maven 3.9
- **HTTP客户端**: RestTemplate（GitHub API集成）
- **缓存框架**: Spring Cache with Redis
- **定时任务**: Spring @Scheduled
- **数据验证**: Spring Boot Validation

### 前端技术栈
- **框架**: Vue 3.3.8（组合式API + `<script setup>`）
- **构建工具**: Vite 5.0.4
- **UI 组件库**: Element Plus 2.4.4
- **图标库**: @element-plus/icons-vue 2.3.1
- **状态管理**: Pinia 2.1.7
- **路由**: Vue Router 4.2.5
- **HTTP 客户端**: Axios 1.6.2
- **工具库**: @vueuse/core 10.7.0
- **样式**: SCSS/SASS 1.69.5

### 测试工具
- **前端测试**: Vitest 1.0.4, Playwright 1.40.1, @vue/test-utils 2.4.3
- **代码检查**: ESLint 8.55.0, eslint-plugin-vue 9.19.2

### 容器化与部署
- **容器化**: Docker + Docker Compose
- **数据库容器**: MySQL 8.0 Docker镜像
- **缓存容器**: Redis 7 Alpine镜像
- **后端容器**: Eclipse Temurin JRE 21 Alpine
- **前端容器**: Nginx Alpine（生产环境）

## 核心功能

### 工具管理
- ✅ **工具CRUD**: 创建、读取、更新、删除工具
- 🔍 **智能搜索**: 按名称、描述关键词搜索，支持模糊匹配
- 📄 **分页查询**: 工具列表分页展示
- 🏷️ **分类管理**: 多级分类组织工具
- 🏷️ **标签系统**: 每个工具支持多个标签
- 📊 **工具详情**: 完整的工具信息展示
- 🚀 **发布管理**: 工具发布/下架状态控制

### GitHub集成
- 🔄 **自动同步**: 定时同步GitHub仓库统计数据
- 📈 **实时数据**: stars、forks、issues、watchers
- 🔗 **仓库链接**: 直接跳转到GitHub仓库
- 📖 **README展示**: 显示项目的README文档

### 评价系统
- ⭐ **评分功能**: 1-5星评分
- 💬 **评论系统**: 文字评论体验
- 💭 **评价回复**: 支持对评价进行回复
- 👍 **点赞功能**: 对评价进行点赞
- 📊 **评分统计**: 平均分、评分分布

### 交互功能
- 👁️ **浏览记录**: 记录工具浏览次数
- ❤️ **收藏功能**: 收藏喜欢的工具
- 📥 **安装统计**: 统计工具安装次数
- 👤 **用户标识**: 基于客户端标识的简单用户系统

### 排行榜
- 🏆 **日榜**: 当日热门工具排行
- 📅 **周榜**: 近7天热门工具排行
- 📊 **总榜**: 历史总热度排行
- 🔥 **热度算法**: 基于浏览、收藏、安装的综合计算

## API 端点

### 工具管理 API
- `GET /api/tools` - 获取工具列表（分页）
- `GET /api/tools/{id}` - 获取工具基本信息
- `GET /api/tools/{id}/detail` - 获取工具完整详情（含用户状态）
- `GET /api/tools/search?keyword=xxx` - 搜索工具
- `POST /api/tools` - 创建工具（管理员）
- `PUT /api/tools/{id}` - 更新工具（管理员）
- `DELETE /api/tools/{id}` - 删除工具（管理员）
- `POST /api/tools/{id}/view` - 记录浏览
- `POST /api/tools/{id}/favorite` - 切换收藏状态
- `POST /api/tools/{id}/install` - 记录安装
- `PUT /api/tools/{id}/publish` - 发布工具
- `PUT /api/tools/{id}/unpublish` - 下架工具

### 分类管理 API
- `GET /api/categories` - 获取所有分类
- `GET /api/categories/{id}` - 获取分类详情
- `POST /api/categories` - 创建分类
- `PUT /api/categories/{id}` - 更新分类
- `DELETE /api/categories/{id}` - 删除分类

### 排行榜 API
- `GET /api/ranking/daily?limit=20` - 日榜
- `GET /api/ranking/weekly?limit=20` - 周榜
- `GET /api/ranking/alltime?limit=20` - 总榜

### 评价管理 API
- `GET /api/ratings/{toolId}` - 获取工具评价列表
- `POST /api/ratings` - 创建评价
- `POST /api/ratings/{id}/reply` - 回复评价
- `POST /api/ratings/{id}/like` - 点赞/取消点赞评价

### 搜索 API
- `GET /api/search?keyword=xxx` - 搜索工具

### GitHub 集成 API
- `POST /api/github/sync/{toolId}` - 同步单个工具的GitHub数据
- `POST /api/github/sync/all` - 同步所有工具的GitHub数据

## 测试

```bash
# 测试后端 API
curl http://localhost:8080/api/tools
curl http://localhost:8080/api/tools/1/detail
curl http://localhost:8080/api/tools/ranking/daily

# 测试前端
open http://localhost:5173
```

## 数据库设计

### 数据库初始化
数据库使用SQL文件进行初始化，遵循项目的规范：
- **表结构**: 所有表定义在 `backend/src/main/resources/schema.sql`
- **初始数据**: 所有预置数据在 `backend/src/main/resources/data.sql`

### 数据表
系统包含8张核心表：
1. **categories** - 分类表（工具分类）
2. **tools** - 工具表（工具主信息）
3. **tool_tags** - 工具标签关联表
4. **ratings** - 评价表（用户评分和评论）
5. **comment_replies** - 评价回复表
6. **rating_likes** - 评价点赞表
7. **favorites** - 收藏表
8. **view_records** - 浏览记录表

### 初始数据
系统预置了以下示例数据：
- 5个工具分类（开发工具、构建工具、测试工具、文档工具、其他）
- 5个示例工具（React DevTools、Webpack、Jest、Vite、Vue DevTools）
- 示例标签数据
- 示例评价和回复数据

### 数据库特性
- ✅ 使用InnoDB引擎，支持事务
- ✅ UTF-8字符集（utf8mb4）
- ✅ 外键约束保证数据完整性
- ✅ 索引优化查询性能
- ✅ 级联删除处理关联数据
- ✅ 唯一约束防止重复数据

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

### 使用停止脚本（推荐）
```bash
./scripts/stop.sh
```

### 手动停止
```bash
# 停止并删除Docker容器
docker-compose down

# 停止并删除容器及数据卷（慎用！）
docker-compose down -v

# 停止本地运行的服务
# 在后端和前端终端按 Ctrl+C
```

## 文档

- [架构设计文档](ARCHITECTURE_DESIGN.md) - 系统架构和模块设计
- [实现设计文档](IMPLEMENTATION_DESIGN.md) - 详细实现说明
- [Docker 使用文档](DOCKER.md) - Docker 配置和部署

## 开发指南

### 后端开发
```bash
cd backend

# 启动开发服务器
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 运行测试
mvn test

# 打包
mvn clean package

# 跳过测试打包
mvn clean package -DskipTests
```

### 前端开发
```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 代码检查
npm run lint

# 运行测试
npm run test
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
