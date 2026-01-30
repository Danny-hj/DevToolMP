# DevToolMP 项目总结

## 项目概述

DevToolMP 是一个基于 Spring Boot 3.x + Vue 3 的工具市场平台，仿照 skills.sh 的设计风格，提供工具展示、热度排行、评论评分等功能。

## 已完成的功能

### 后端（Spring Boot 3.x）

✅ 项目结构和配置
- Maven 项目配置（pom.xml）
- 应用配置文件（application.yml, application-dev.yml, application-test.yml）
- 跨域配置（CorsConfig）
- JWT 工具类（JwtUtil）

✅ 数据库设计
- 完整的数据库表结构（schema.sql）
- 测试数据初始化（data.sql）
- 10 个核心表：users, categories, tools, tool_tags, favorites, view_records, telemetry_data, ratings, comment_replies, rating_likes

✅ 实体类（Entity）
- User.java
- Category.java
- Tool.java
- ToolTag.java
- Favorite.java
- ViewRecord.java
- TelemetryData.java
- Rating.java
- CommentReply.java
- RatingLike.java

✅ 数据访问层（Repository）
- UserRepository
- CategoryRepository
- ToolRepository
- ToolTagRepository
- FavoriteRepository
- ViewRecordRepository
- TelemetryRepository
- RatingRepository
- CommentReplyRepository

✅ 数据传输对象（DTO）
- 请求 DTO：ToolCreateRequest, ToolUpdateRequest, RatingCreateRequest, CommentReplyRequest
- 响应 DTO：ToolDTO, ToolDetailDTO, ToolRankingDTO, RatingDTO, CommentReplyDTO, RatingStatisticsDTO

✅ 服务层（Service）
- ToolService：工具 CRUD、搜索、浏览记录、收藏、安装记录
- RatingService：评论评分 CRUD、回复、统计
- RankingService：热度计算和排行榜

✅ 控制器层（Controller）
- ToolController：工具相关 API
- RatingController：评论评分相关 API
- RankingController：排行榜相关 API
- SearchController：搜索相关 API

### 前端（Vue 3）

✅ 项目结构和配置
- Vite 配置（vite.config.js）
- Vitest 配置（vitest.config.js）
- 依赖配置（package.json）
- 环境变量配置
- 路由配置（router/index.js）
- API 请求封装（api/index.js）
- SCSS 变量（assets/styles/variables.scss）
- 暗色主题（assets/styles/dark-theme.scss）

✅ 状态管理（Pinia Store）
- tools.js：工具状态管理
- rating.js：评论评分状态管理
- user.js：用户状态管理

✅ 布局组件
- AppHeader.vue：顶部导航栏（Logo、搜索、导航、用户操作）
- AppFooter.vue：页脚

✅ 工具相关组件
- ToolCard.vue：工具卡片组件（展示工具信息、标签、统计数据、收藏）

✅ 评论评分组件
- RatingDisplay.vue：评分展示组件（平均分、评分分布）
- RatingForm.vue：评分表单组件（提交评价）

✅ 页面组件
- Home.vue：首页（热门工具、最新工具）
- Tools.vue：工具列表页（分页、排序、筛选）
- ToolDetail.vue：工具详情页（工具信息、统计数据、用户评价）
- Search.vue：搜索结果页（搜索、展示结果）

### 配置和文档

✅ Docker 配置
- docker-compose.yml：本地开发环境配置
- backend/Dockerfile：后端 Docker 配置
- frontend/Dockerfile：前端 Docker 配置

✅ 脚本
- scripts/start-dev.sh：一键启动开发环境
- scripts/test-all.sh：运行所有测试
- scripts/init-db.sh：初始化数据库

✅ 文档
- plan.md：完整的项目规划
- docs/README.md：项目说明文档
- docs/API.md：API 接口文档
- .gitignore：Git 忽略配置

## 技术栈

### 后端
- Spring Boot 3.2.0
- Spring Security + JWT
- Spring Data JPA
- MySQL 8.0
- JUnit 5 + Mockito + Testcontainers

### 前端
- Vue 3.3.8
- Vue Router 4.2.5
- Pinia 2.1.7
- Axios 1.6.2
- Element Plus 2.4.4
- VueUse 10.7.0
- Vite 5.0.4
- Vitest 1.0.4

## 核心功能

### 1. 工具管理
- 工具列表展示（分页、排序）
- 工具详情展示
- 工具搜索（关键词搜索）
- 工具创建、更新、删除

### 2. 统计数据
- 浏览量统计
- 收藏数统计
- 安装数统计
- GitHub 仓库信息（Stars, Forks, Issues）

### 3. 热度排行
- 日榜（基于最近 24 小时的热度）
- 周榜（基于最近 7 天的热度）
- 总榜（基于所有历史数据）
- 热度变化百分比展示

### 4. 评论评分
- 五星评分系统
- 评论功能
- 评分统计（平均分、分布）
- 评论回复
- 点赞功能

### 5. 收藏功能
- 工具收藏/取消收藏
- 收藏列表展示
- 收藏状态检查

### 6. 搜索功能
- 关键词搜索
- 搜索建议
- 搜索结果分页

## 热度计算算法

```javascript
// 热度计算公式
hotScore = (viewCount * weight) + (favoriteCount * 10 * weight) + (installCount * 5 * weight)

// 权重分配
dailyRank = { view: 0.2, favorite: 0.3, install: 0.5 }
weeklyRank = { view: 0.3, favorite: 0.3, install: 0.4 }
alltimeRank = { view: 0.4, favorite: 0.3, install: 0.3 }
```

## 项目结构

```
DevToolMP/
├── backend/                    # 后端项目
│   ├── src/main/java/com/devtoolmp/
│   │   ├── config/            # 配置类
│   │   ├── controller/         # 控制器
│   │   ├── service/           # 服务层
│   │   ├── repository/         # 数据访问层
│   │   ├── entity/            # 实体类
│   │   ├── dto/               # 数据传输对象
│   │   └── util/              # 工具类
│   ├── src/main/resources/
│   │   ├── schema.sql         # 数据库表结构
│   │   ├── data.sql           # 测试数据
│   │   └── application*.yml    # 配置文件
│   └── pom.xml                # Maven 配置
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── components/        # 组件
│   │   ├── views/             # 页面
│   │   ├── stores/            # 状态管理
│   │   ├── api/               # API 封装
│   │   ├── router/            # 路由配置
│   │   └── assets/            # 资源文件
│   ├── package.json           # 依赖配置
│   └── vite.config.js         # Vite 配置
├── docs/                       # 文档
├── scripts/                    # 脚本
├── docker-compose.yml         # Docker 配置
└── plan.md                    # 项目规划
```

## 快速开始

### 使用 Docker 启动（推荐）

```bash
# 启动开发环境
./scripts/start-dev.sh

# 访问应用
# 前端：http://localhost:5173
# 后端：http://localhost:8080/api
```

### 本地启动

#### 后端
```bash
cd backend
./mvnw spring-boot:run
```

#### 前端
```bash
cd frontend
npm install
npm run dev
```

## 测试

```bash
# 运行所有测试
./scripts/test-all.sh

# 后端测试
cd backend
./mvnw test

# 前端测试
cd frontend
npm run test:unit
```

## 待完善功能

1. 用户认证系统（登录、注册、GitHub OAuth）
2. 用户个人中心
3. 工具提交页面
4. GitHub API 集成（自动同步仓库信息）
5. 工具文档页面（渲染 README）
6. 更多单元测试
7. E2E 测试
8. 响应式设计优化
9. 国际化支持
10. 部署配置

## 注意事项

1. 后端需要 Java 17+
2. 前端需要 Node.js 18+
3. 数据库使用 MySQL 8.0+
4. 需要配置 GitHub OAuth 凭证（可选）
5. 生产环境需要修改 JWT 密钥

## License

MIT
