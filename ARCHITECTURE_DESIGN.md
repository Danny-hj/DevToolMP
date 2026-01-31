# DevToolMP 架构设计文档

## 1. 项目概述

### 1.1 项目简介
DevToolMP（DevTool Market Platform）是一个开发者工具市场平台，旨在为开发者提供工具发现、分享、评价和排行的一站式解决方案。

### 1.2 技术栈

#### 后端技术栈
- **框架**: Spring Boot 3.2.1
- **Java版本**: JDK 21
- **持久层**: MyBatis 3.0.3
- **数据库**: MySQL 8.0
- **缓存**: Redis 7 (Jedis客户端)
- **构建工具**: Maven
- **测试**: JUnit 5, Testcontainers 1.20.4

#### 前端技术栈
- **框架**: Vue 3.3.8
- **构建工具**: Vite 5.0.4
- **UI组件**: Element Plus 2.4.4
- **状态管理**: Pinia 2.1.7
- **路由**: Vue Router 4.2.5
- **HTTP客户端**: Axios 1.6.2
- **工具库**: @vueuse/core 10.7.0
- **测试**: Vitest 1.0.4, Playwright 1.40.1

#### 容器化
- **容器编排**: Docker Compose
- **生产环境**: Docker Compose Production

---

## 2. 系统架构

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         客户端层 (Frontend)                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐   │
│  │   Vue 3 SPA  │  │  Vite Build  │  │  Element Plus UI     │   │
│  └──────────────┘  └──────────────┘  └──────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       网关层 (API Gateway)                       │
│              ┌──────────────────────────────────────┐           │
│              │  Spring Boot Controller Layer        │           │
│              │  - REST API Endpoints                │           │
│              │  - CORS Configuration                │           │
│              └──────────────────────────────────────┘           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        服务层 (Services)                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │  ToolService │  │  GitHubService│  │  RatingService      │  │
│  │              │  │              │  │  RankingService     │  │
│  └──────────────┘  └──────────────┘  └──────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      数据访问层 (Mapper/DAO)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │ MyBatis Mappers (XML) │          │  Entity Classes       │  │
│  └──────────────┘  └──────────────┘  └──────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       数据存储层 (Storage)                        │
│  ┌──────────────────┐           ┌──────────────────┐           │
│  │   MySQL 8.0      │           │   Redis 7        │           │
│  │  - 业务数据       │           │  - 缓存层         │           │
│  │  - 关系数据       │           │  - 会话存储       │           │
│  └──────────────────┘           └──────────────────┘           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      外部服务集成                                │
│  ┌──────────────────┐           ┌──────────────────┐           │
│  │   GitHub API     │           │  定时任务调度     │           │
│  │  - 仓库信息同步   │           │  - 数据同步       │           │
│  │  - 统计数据更新   │           │  - 热度计算       │           │
│  └──────────────────┘           └──────────────────┘           │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 分层架构详解

#### 2.2.1 表现层 (Controller)
- **职责**: 接收HTTP请求，参数验证，调用服务层，返回响应
- **主要组件**:
  - `ToolController`: 工具管理相关接口
  - `RatingController`: 评价管理相关接口
  - `RankingController`: 排行榜相关接口
  - `SearchController`: 搜索功能接口
  - `GitHubController`: GitHub集成接口

#### 2.2.2 业务逻辑层 (Service)
- **职责**: 实现核心业务逻辑，事务管理，缓存管理
- **主要组件**:
  - `ToolService`: 工具CRUD、浏览记录、收藏管理
  - `RatingService`: 评价创建、回复、点赞
  - `RankingService`: 排行榜计算、热度分数更新
  - `GitHubService`: GitHub数据同步、API调用

#### 2.2.3 数据访问层 (Mapper)
- **职责**: 数据库操作，SQL执行，结果映射
- **主要组件**:
  - `ToolMapper`: 工具数据操作
  - `RatingMapper`: 评价数据操作
  - `CategoryMapper`: 分类数据操作
  - `FavoriteMapper`, `ViewRecordMapper`, `ToolTagMapper` 等

#### 2.2.4 实体层 (Entity/DTO)
- **Entity**: 数据库实体映射
  - `Tool`, `Rating`, `Category`, `Favorite`, `ViewRecord`, `ToolTag`, `CommentReply`, `RatingLike`
- **DTO**: 数据传输对象
  - 请求DTO: `ToolCreateRequest`, `ToolUpdateRequest`, `RatingCreateRequest`, `CommentReplyRequest`
  - 响应DTO: `ToolDTO`, `ToolDetailDTO`, `RatingDTO`, `PageResponse`, `ApiResponse`

---

## 3. 数据模型设计

### 3.1 核心实体关系

```
┌─────────────┐       ┌──────────────┐       ┌─────────────┐
│   Category  │───1:N─│     Tool     │───1:N─│   ToolTag   │
│  (分类)      │       │   (工具)      │       │  (标签)      │
└─────────────┘       └──────────────┘       └─────────────┘
                             │
                             │ 1:N
                             ▼
                      ┌──────────────┐
                      │  Rating      │
                      │  (评价)       │
                      └──────────────┘
                             │
                             │ 1:N
                             ▼
                      ┌──────────────┐
                      │ CommentReply │
                      │ (评价回复)    │
                      └──────────────┘

┌──────────────┐       ┌──────────────┐
│   Favorite   │───N:1─│     Tool     │
│  (收藏)       │       │              │
└──────────────┘       │              │
                      │              │
┌──────────────┐       │              │
│  ViewRecord  │───N:1─│              │
│  (浏览记录)   │       │              │
└──────────────┘       └──────────────┘
```

### 3.2 数据库表设计

#### tools (工具表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(255) | 工具名称 |
| description | TEXT | 描述 |
| category_id | BIGINT | 分类ID |
| github_owner | VARCHAR(255) | GitHub所有者 |
| github_repo | VARCHAR(255) | GitHub仓库 |
| version | VARCHAR(50) | 版本号 |
| stars | INT | GitHub星标数 |
| forks | INT | GitHub分支数 |
| open_issues | INT | GitHub问题数 |
| watchers | INT | GitHub关注者 |
| view_count | INT | 浏览次数 |
| favorite_count | INT | 收藏次数 |
| install_count | INT | 安装次数 |
| hot_score_daily | DECIMAL | 日热度分数 |
| hot_score_weekly | DECIMAL | 周热度分数 |
| hot_score_alltime | DECIMAL | 总热度分数 |
| status | VARCHAR(20) | 状态 (active/inactive) |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

#### ratings (评价表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| tool_id | BIGINT | 工具ID |
| client_identifier | VARCHAR(255) | 客户端标识 |
| score | INT | 评分 (1-5) |
| comment | TEXT | 评价内容 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

#### categories (分类表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(100) | 分类名称 |
| description | TEXT | 描述 |
| created_at | DATETIME | 创建时间 |

---

## 4. 核心模块设计

### 4.1 工具管理模块

**功能**:
- 工具的创建、读取、更新、删除 (CRUD)
- 工具列表分页查询
- 工具详情展示
- 工具发布/下架状态管理

**关键流程**:
```
创建工具 → 验证数据 → 保存基本信息 → 保存标签 → 返回工具DTO
查询工具 → 数据库查询 → 关联查询分类和标签 → 组装DTO → 返回
```

### 4.2 GitHub集成模块

**功能**:
- 同步GitHub仓库统计数据 (stars, forks, issues, watchers)
- 定时自动同步所有工具的GitHub数据
- 获取GitHub仓库README
- 获取最新发布版本信息

**同步策略**:
- 定时任务: 每天凌晨2点执行全量同步
- 手动触发: 支持单个工具的即时同步
- API限流处理: 请求间隔1秒，避免超过GitHub API限制

### 4.3 评价系统模块

**功能**:
- 用户提交评价 (1-5星评分 + 评论)
- 评价回复功能
- 评价点赞功能
- 评价统计 (平均分、评分分布)

**数据流**:
```
创建评价 → 验证工具存在 → 保存评价 → 更新工具统计
回复评价 → 验证评价存在 → 保存回复 → 关联评价
点赞评价 → 验证评价存在 → 切换点赞状态 → 更新点赞数
```

### 4.4 排行榜模块

**功能**:
- 日榜、周榜、总榜
- 基于热度分数排序
- 热度分数算法考虑: 浏览量、收藏量、安装量的增长

**热度算法**:
```
日热度 = (当日浏览量 × 1 + 当日收藏量 × 5 + 当日安装量 × 10) / 衰减因子
周热度 = 近7天热度累加
总热度 = 历史数据综合计算
```

### 4.5 搜索模块

**功能**:
- 工具名称/描述关键词搜索
- 分页返回结果
- 支持模糊匹配

---

## 5. 缓存策略

### 5.1 Redis缓存设计

**缓存类型**:
1. **GitHub API缓存**: `githubRepoInfo`, `githubReadme`, `githubLatestRelease`
   - 缓存GitHub API响应，减少API调用
   - TTL: 1小时

2. **热点数据缓存**:
   - 热门工具详情
   - 排行榜数据
   - TTL: 5-15分钟

### 5.2 缓存注解使用
```java
@Cacheable(value = "githubRepoInfo", key = "#owner + '/' + #repo")
public Map<String, Object> fetchRepositoryInfo(String owner, String repo)
```

---

## 6. 外部API集成

### 6.1 GitHub API

**集成方式**:
- 使用Spring RestTemplate进行HTTP调用
- 支持Personal Access Token认证
- 请求头: `Accept: application/vnd.github.v3+json`

**API端点**:
- 仓库信息: `GET /repos/{owner}/{repo}`
- README: `GET /repos/{owner}/{repo}/readme`
- 最新发布: `GET /repos/{owner}/{repo}/releases/latest`

**速率限制处理**:
- 未认证: 60次/小时
- 已认证: 5000次/小时
- 同步任务添加1秒延迟避免超限

---

## 7. 定时任务设计

### 7.1 GitHub数据同步
- **执行时间**: 每天凌晨2点
- **Cron表达式**: `0 0 2 * * ?`
- **功能**: 同步所有活跃工具的GitHub统计数据

### 7.2 可扩展任务
- 小时同步: `0 0 * * * ?` (已注释，按需启用)
- 热度分数计算: 可按小时/每日执行

---

## 8. 安全与认证

### 8.1 客户端标识
- 基于IP地址 + User-Agent生成唯一标识
- 用于追踪用户浏览、收藏、评价行为
- 无需传统用户登录系统

### 8.2 CORS配置
- 允许跨域请求
- 支持开发环境的前后端分离部署

### 8.3 数据验证
- 使用`@Valid`注解进行请求参数验证
- 自定义`BusinessException`处理业务异常

---

## 9. 前端架构

### 9.1 目录结构
```
frontend/src/
├── api/              # API接口封装
├── components/       # 可复用组件
│   ├── layout/      # 布局组件
│   ├── tool/        # 工具相关组件
│   ├── rating/      # 评价相关组件
│   └── home/        # 首页组件
├── views/           # 页面视图
├── stores/          # Pinia状态管理
├── router/          # 路由配置
└── main.js          # 应用入口
```

### 9.2 状态管理 (Pinia Stores)
- `tools`: 工具列表和详情状态
- `rating`: 评价相关状态
- `ranking`: 排行榜状态
- `user`: 用户标识状态

### 9.3 路由设计
- `/`: 首页
- `/tools`: 工具列表
- `/tools/:id`: 工具详情
- `/search`: 搜索页面
- `/ranking`: 排行榜

---

## 10. 部署架构

### 10.1 开发环境
- 前端: Vite开发服务器 (端口5173)
- 后端: Spring Boot (端口8080)
- 数据库: MySQL容器 (端口3306)
- 缓存: Redis容器 (端口6379)

### 10.2 生产环境
- Docker Compose多容器部署
- 服务健康检查 (healthcheck)
- 数据持久化卷 (volumes)
- 网络隔离 (networks)

### 10.3 服务依赖
```
frontend → backend → mysql
                     → redis
```

---

## 11. 性能优化

### 11.1 后端优化
- MyBatis二级缓存
- Redis缓存热点数据
- GitHub API响应缓存
- 数据库索引优化 (分类ID、状态字段)

### 11.2 前端优化
- Vite快速冷启动
- 组件按需加载
- 静态资源压缩
- Vue响应式优化

---

## 12. 监控与日志

### 12.1 日志配置
- 日志级别: DEBUG (开发), INFO (生产)
- 日志输出: 控制台 (可扩展至文件)
- SQL日志: MyBatis日志输出

### 12.2 异常处理
- 全局异常处理器: `GlobalExceptionHandler`
- 统一响应格式: `ApiResponse`
- 业务异常: `BusinessException`

---

## 13. 扩展性设计

### 13.1 水平扩展
- 后端无状态设计，支持多实例部署
- Redis可切换为集群模式
- MySQL可切换为主从复制

### 13.2 功能扩展点
- 用户认证系统 (OAuth2)
- 工具提交审核流程
- 工具安装统计与分析
- 通知系统 (邮件/推送)
- 工具比较功能

---

## 14. 技术债务与改进建议

### 14.1 当前技术债务
1. 缺少完整的单元测试覆盖
2. 搜索功能可升级为Elasticsearch
3. 客户端标识机制过于简单

### 14.2 改进建议
1. 引入消息队列处理异步任务 (如数据同步)
2. 添加API文档 (Swagger/OpenAPI)
3. 实现完整的用户认证与授权
4. 添加工具分类和标签的自动推荐
5. 实现工具安装监控和使用统计

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2025-01-31 | 初始架构设计文档 |
