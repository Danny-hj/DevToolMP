# DevToolMP 架构设计文档

## 文档版本

- **版本**: 1.0.0
- **更新日期**: 2026-01-31
- **作者**: DevToolMP Team
- **状态**: 生产就绪

## 目录

1. [系统概述](#1-系统概述)
2. [架构原则](#2-架构原则)
3. [技术架构](#3-技术架构)
4. [系统分层](#4-系统分层)
5. [核心模块设计](#5-核心模块设计)
6. [数据架构](#6-数据架构)
7. [缓存架构](#7-缓存架构)
8. [安全架构](#8-安全架构)
9. [部署架构](#9-部署架构)
10. [扩展性设计](#10-扩展性设计)

---

## 1. 系统概述

### 1.1 项目简介

DevToolMP 是一个现代化的工具市场平台，采用前后端分离架构，提供工具发现、热度排行、评分评论、GitHub集成等功能。系统设计参考 skills.sh 的极简风格和用户体验。

### 1.2 核心功能

- **工具管理**: 工具发布、编辑、搜索、分类管理
- **热度排行**: 多维度排行榜（日榜、周榜、总榜、趋势榜）
- **评价系统**: 五星评分、评论回复、点赞功能
- **GitHub集成**: 自动同步仓库信息、实时数据更新
- **用户交互**: 收藏、浏览记录、安装统计
- **数据分析**: 热度算法、趋势分析、统计仪表板

### 1.3 设计目标

- **高性能**: 支持10万+工具数据，响应时间 < 200ms
- **高可用**: 99.9% 可用性，支持水平扩展
- **可扩展**: 模块化设计，便于功能扩展
- **易维护**: 代码规范统一，架构清晰
- **用户体验**: 极简设计，快速响应

---

## 2. 架构原则

### 2.1 前后端分离

- 前端负责 UI 渲染和用户交互
- 后端提供 RESTful API
- 通过 Axios 进行 HTTP 通信
- 完全解耦，独立开发部署

### 2.2 RESTful API 设计

- 统一的 API 响应格式
- 标准 HTTP 状态码
- 资源导向的 URL 设计
- 无状态通信

### 2.3 领域驱动设计 (DDD)

- 清晰的领域模型
- 业务逻辑封装在 Service 层
- 数据访问通过 Mapper 层
- DTO 用于层数据传输

### 2.4 缓存优先策略

- 热点数据缓存（排行榜）
- 查询结果缓存
- 定时刷新机制
- 缓存失效策略

---

## 3. 技术架构

### 3.1 技术栈全景图

```
┌─────────────────────────────────────────────────────────────┐
│                        用户层                                │
├─────────────────────────────────────────────────────────────┤
│  浏览器 (Chrome/Firefox/Safari)                              │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      前端应用层                              │
├─────────────────────────────────────────────────────────────┤
│  Vue 3.3.8  │  Vue Router 4.2.5  │  Pinia 2.1.7             │
│  Element Plus 2.4.4  │  Axios 1.6.2  │  Vite 5.0.4           │
└─────────────────────────────────────────────────────────────┘
                              ↓ REST API
┌─────────────────────────────────────────────────────────────┐
│                       API 网关层                              │
├─────────────────────────────────────────────────────────────┤
│  Spring Boot 3.2.1  │  Context Path: /api                   │
│  Port: 8080  │  CORS Configured                              │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      应用服务层                              │
├─────────────────────────────────────────────────────────────┤
│  Controller Layer  │  Service Layer  │  Mapper Layer        │
│  (REST Endpoints)  │  (Business Logic) │  (Data Access)      │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      数据存储层                              │
├─────────────────────────────────────────────────────────────┤
│  MySQL 8.0       │  Redis (Cache)      │  GitHub API        │
│  (持久化存储)     │  (分布式缓存)       │  (外部集成)         │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 后端技术栈

**核心框架**:
- Spring Boot 3.2.1 (Java 21)
- Spring Web MVC
- Spring Validation
- Spring Cache Abstraction

**数据访问**:
- MyBatis 3.0.3 (持久化框架)
- MySQL Connector 8.1.0
- HikariCP (数据库连接池)

**缓存**:
- Spring Data Redis
- Jedis 5.0.2 (Redis 客户端)

**工具库**:
- Lombok (代码简化)
- Jackson (JSON 处理)
- RestTemplate (HTTP 客户端)

**测试**:
- JUnit 5
- Mockito
- H2 Database (测试数据库)

### 3.3 前端技术栈

**核心框架**:
- Vue 3.3.8 (Composition API)
- Vue Router 4.2.5
- Pinia 2.1.7 (状态管理)

**UI 框架**:
- Element Plus 2.4.4
- Element Plus Icons 2.3.1

**HTTP 客户端**:
- Axios 1.6.2

**构建工具**:
- Vite 5.0.4
- Sass 1.69.5

**测试**:
- Vitest 1.0.4
- Vue Test Utils 2.4.3
- Playwright 1.40.1 (E2E)

---

## 4. 系统分层

### 4.1 后端分层架构

```
┌─────────────────────────────────────────────────────────────┐
│  Controller Layer (控制器层)                                │
│  - 接收 HTTP 请求                                           │
│  - 参数验证                                                 │
│  - 调用 Service 层                                          │
│  - 返回响应                                                 │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  Service Layer (服务层)                                     │
│  - 业务逻辑处理                                             │
│  - 事务管理                                                 │
│  - 调用 Mapper 层                                           │
│  - 缓存管理                                                 │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  Mapper Layer (数据访问层)                                  │
│  - MyBatis Mapper 接口                                      │
│  - SQL 执行                                                 │
│  - 结果映射                                                 │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  Database (数据库)                                          │
│  - MySQL 数据库                                             │
│  - Redis 缓存                                              │
└─────────────────────────────────────────────────────────────┘
```

### 4.2 前端分层架构

```
┌─────────────────────────────────────────────────────────────┐
│  View Layer (视图层)                                        │
│  - Page Components (Home, Tools, ToolDetail, etc.)          │
│  - UI Components (Cards, Forms, Displays)                   │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  State Management Layer (状态管理层)                        │
│  - Pinia Stores (tools, rating, ranking, user)             │
│  - Reactive State                                           │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  API Layer (API 层)                                         │
│  - Axios Instance                                           │
│  - Request/Response Interceptors                            │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  Router Layer (路由层)                                      │
│  - Vue Router                                               │
│  - Navigation Guards                                       │
└─────────────────────────────────────────────────────────────┘
```

---

## 5. 核心模块设计

### 5.1 工具管理模块 (Tool Management)

**职责**:
- 工具的 CRUD 操作
- 工具搜索和过滤
- 工具分类管理
- 标签管理
- GitHub 仓库同步

**关键类**:
- `ToolController` - REST API 端点
- `ToolService` - 业务逻辑
- `ToolMapper` - 数据访问
- `GitHubService` - GitHub API 集成

**API 端点**:
- `GET /api/tools` - 获取工具列表（分页）
- `GET /api/tools/{id}` - 获取工具详情
- `POST /api/tools` - 创建工具
- `PUT /api/tools/{id}` - 更新工具
- `DELETE /api/tools/{id}` - 删除工具
- `GET /api/tools/search` - 搜索工具
- `POST /api/tools/{id}/view` - 记录浏览
- `POST /api/tools/{id}/favorite` - 切换收藏
- `POST /api/tools/{id}/install` - 记录安装

### 5.2 排行榜模块 (Ranking)

**职责**:
- 多维度排行榜计算
- 热度分数计算
- 趋势分析
- 缓存管理

**关键类**:
- `RankingController` - REST API 端点
- `RankingService` - 排行榜计算逻辑
- `ScheduledTasks` - 定时任务

**排行榜类型**:
1. **日榜** - 基于最近24小时热度
2. **周榜** - 基于最近7天热度
3. **总榜** - 基于历史总热度
4. **趋势榜** - 24小时内增长最快

**API 端点**:
- `GET /api/tools/ranking/daily` - 日榜
- `GET /api/tools/ranking/weekly` - 周榜
- `GET /api/tools/ranking/alltime` - 总榜
- `GET /api/tools/ranking/trending` - 趋势榜

### 5.3 评价模块 (Rating)

**职责**:
- 用户评分管理
- 评论管理
- 评论回复
- 点赞功能
- 评分统计

**关键类**:
- `RatingController` - REST API 端点
- `RatingService` - 业务逻辑
- `RatingMapper` - 数据访问
- `CommentReplyMapper` - 回复数据访问
- `RatingLikeMapper` - 点赞数据访问

**API 端点**:
- `GET /api/ratings/tool/{toolId}` - 获取工具评分列表
- `POST /api/ratings` - 创建评分
- `GET /api/ratings/{id}/replies` - 获取评论回复
- `POST /api/ratings/{id}/reply` - 回复评论
- `POST /api/ratings/{id}/like` - 点赞评论
- `GET /api/ratings/tool/{toolId}/statistics` - 获取评分统计

### 5.4 GitHub 集成模块

**职责**:
- GitHub API 调用
- 仓库信息同步
- README 获取
- Release 信息获取

**关键类**:
- `GitHubController` - REST API 端点
- `GitHubService` - GitHub API 调用逻辑
- `RestTemplateConfig` - HTTP 客户端配置

**API 端点**:
- `GET /api/github/repos/{owner}/{repo}` - 获取仓库信息
- `GET /api/github/repos/{owner}/{repo}/validate` - 验证仓库
- `GET /api/github/repos/{owner}/{repo}/readme` - 获取 README
- `GET /api/github/repos/{owner}/{repo}/releases/latest` - 获取最新版本
- `POST /api/github/sync/{toolId}` - 同步工具数据
- `POST /api/github/sync/all` - 批量同步所有工具

---

## 6. 数据架构

### 6.1 数据库设计原则

- **范式化**: 第三范式 (3NF)
- **索引优化**: 常用查询字段添加索引
- **分区策略**: 按时间分区（未来扩展）
- **读写分离**: 主从复制（未来扩展）

### 6.2 核心数据表

**工具相关**:
- `tools` - 工具主表
- `categories` - 分类表
- `tool_tags` - 标签关联表

**用户交互**:
- `favorites` - 收藏表
- `ratings` - 评分表
- `rating_likes` - 评分点赞表
- `comment_replies` - 评论回复表
- `view_records` - 浏览记录表

**统计分析**:
- `telemetry_data` - 遥测数据表（已废弃）

### 6.3 ER 关系图

```
┌──────────────┐         ┌──────────────┐
│   categories │         │     tools    │
│              │1       N│              │
│ - id         │────────│ - id         │
│ - name       │         │ - category_id│
│ - description│         │ - name       │
└──────────────┘         │ - description│
                         │ - github_owner│
                         │ - github_repo │
                         └──────┬───────┘
                                │
                ┌───────────────┼───────────────┐
                │               │               │
         ┌──────┴──────┐  ┌──────┴──────┐  ┌───┴────┐
         │  tool_tags  │  │  favorites  │  │ ratings │
         │             │  │             │  │         │
         │ - tool_id   │  │ - tool_id   │  │-tool_id │
         │ - tag_name  │  │ - client_id │  │-score   │
         └─────────────┘  └─────────────┘  └─────────┘
                                                │
                                         ┌──────┴───────┐
                                         │rating_likes  │
                                         │comment_replies│
```

### 6.4 数据流

**写入流程**:
```
用户操作 → Controller → Service → Mapper → MyBatis → MySQL
                                      ↓
                                  Redis Cache Update
```

**读取流程**:
```
用户请求 → Controller → Service → Redis Cache (Hit?)
                              ↓         ↓
                         Mapper    Return
                              ↓
                          MyBatis → MySQL
                              ↓
                          Redis Cache Set
                              ↓
                          Return
```

---

## 7. 缓存架构

### 7.1 缓存策略

**缓存类型**:
- **本地缓存**: Caffeine（已废弃，改用 Redis）
- **分布式缓存**: Redis

**缓存内容**:
- 排行榜数据（10分钟 TTL）
- 工具详情（5分钟 TTL）
- 热门工具列表（5分钟 TTL）

### 7.2 缓存层次

```
┌─────────────────────────────────────────────────────────────┐
│  L1: Application Cache (Spring Cache Abstraction)           │
│  - @Cacheable 注解                                          │
│  - Cache Manager: Redis                                     │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  L2: Redis Server (分布式缓存)                              │
│  - String 数据结构                                          │
│  - TTL 自动过期                                             │
└─────────────────────────────────────────────────────────────┘
```

### 7.3 缓存更新策略

**主动更新**:
- 定时任务每10分钟更新排行榜缓存
- 工具数据变更时清除相关缓存

**被动更新**:
- TTL 过期自动失效
- LRU 淘汰策略

### 7.4 缓存 Key 设计

```
ranking:daily     -> 日榜数据
ranking:weekly    -> 周榜数据
ranking:alltime   -> 总榜数据
ranking:trending  -> 趋势榜数据
tool:{id}         -> 工具详情
tools:hot         -> 热门工具列表
```

---

## 8. 安全架构

### 8.1 无认证设计

**特点**:
- 无登录系统
- 无 JWT Token
- 无 Session 管理

**用户识别**:
- 基于客户端标识符 (clientIdentifier)
- 生成规则: IP地址 + User-Agent
- 存储在数据库的 user_identifier 字段

**优势**:
- 简化系统复杂度
- 降低开发成本
- 提升访问速度
- 无需管理用户凭证

**局限**:
- 无法关联多设备
- 无法个性化推荐
- 依赖IP和UA稳定性

### 8.2 数据安全

**输入验证**:
- `@Valid` 注解参数校验
- 自定义验证器
- SQL 参数化查询（防注入）

**输出安全**:
- JSON 序列化
- XSS 防护（前端转义）

**通信安全**:
- HTTPS（生产环境）
- CORS 配置
- CSRF 防护（无状态架构无需）

### 8.3 API 安全

**限流**（未来扩展）:
- IP 限流
- 用户限流
- 接口限流

**日志审计**:
- 访问日志
- 错误日志
- 操作日志

---

## 9. 部署架构

### 9.1 容器化部署

**Docker Compose 架构**:

```yaml
Services:
  mysql:
    Image: mysql:8.0
    Port: 3306
    Volume: mysql-data

  redis:
    Image: redis:7-alpine
    Port: 6379

  backend:
    Build: ./backend
    Port: 8080
    Depends On: mysql, redis

  frontend:
    Build: ./frontend
    Port: 5173
    Depends On: backend
```

### 9.2 网络架构

```
┌─────────────┐
│   用户浏览器  │
└──────┬──────┘
       │
       ↓
┌──────────────────────────────────────────┐
│          Nginx (反向代理)                 │
│          SSL Termination                 │
│          Load Balancing                  │
└──────────────────────────────────────────┘
       │                │
       ↓                ↓
┌─────────────┐  ┌─────────────┐
│   Frontend  │  │   Backend   │
│   (Vite)    │  │  (Spring)   │
│   Port:5173 │  │  Port:8080  │
└─────────────┘  └──────┬──────┘
                       │
        ┌──────────────┼──────────────┐
        ↓              ↓              ↓
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│   MySQL     │ │   Redis     │ │  GitHub API │
│   Port:3306 │ │   Port:6379 │ │  (External) │
└─────────────┘ └─────────────┘ └─────────────┘
```

### 9.3 环境配置

**开发环境 (dev)**:
- 本地 MySQL
- 本地 Redis
- 热重载

**生产环境 (prod)**:
- Docker MySQL
- Docker Redis
- 构建优化
- 日志聚合

---

## 10. 扩展性设计

### 10.1 水平扩展

**后端扩展**:
- 无状态设计
- 支持多实例部署
- 负载均衡

**前端扩展**:
- 静态资源 CDN
- 服务端渲染（SSR）可选

**数据库扩展**:
- 读写分离
- 分库分表
- 索引优化

### 10.2 功能扩展

**插件化设计**（未来）:
- 数据源插件
- 排行榜算法插件
- 通知插件

**微服务化**（未来）:
- 工具服务
- 用户服务
- 排行服务
- 通知服务

### 10.3 技术演进

**短期**:
- ✅ Redis 缓存
- ✅ GitHub API 集成
- ✅ 定时任务调度

**中期**:
- ⏳ Elasticsearch 搜索
- ⏳ 消息队列（异步任务）
- ⏳ 数据分析平台

**长期**:
- ⏳ 机器学习推荐
- ⏳ 实时数据流处理
- ⏳ 多语言 SDK

---

## 附录

### A. 端口清单

| 服务 | 端口 | 协议 | 说明 |
|-----|------|------|------|
| Frontend | 5173 | HTTP | Vite 开发服务器 |
| Backend | 8080 | HTTP | Spring Boot 应用 |
| MySQL | 3306 | TCP | MySQL 数据库 |
| Redis | 6379 | TCP | Redis 缓存 |

### B. 环境变量

| 变量名 | 说明 | 默认值 |
|-------|------|--------|
| SPRING_PROFILES_ACTIVE | Spring 配置文件 | dev |
| SERVER_PORT | 后端端口 | 8080 |
| MYSQL_HOST | MySQL 主机 | localhost |
| MYSQL_PORT | MySQL 端口 | 3306 |
| MYSQL_DATABASE | 数据库名 | devtoolmp |
| MYSQL_USER | 数据库用户 | devtool |
| MYSQL_PASSWORD | 数据库密码 | devtool123 |
| REDIS_HOST | Redis 主机 | localhost |
| REDIS_PORT | Redis 端口 | 6379 |

### C. 依赖版本

**后端主要依赖**:
- Spring Boot: 3.2.1
- MyBatis: 3.0.3
- MySQL Connector: 8.1.0
- Jedis: 5.0.2
- Lombok: 最新

**前端主要依赖**:
- Vue: 3.3.8
- Vue Router: 4.2.5
- Pinia: 2.1.7
- Element Plus: 2.4.4
- Axios: 1.6.2
- Vite: 5.0.4

---

**文档结束**

更多信息请参考:
- [实现设计文档](./IMPLEMENTATION_DESIGN.md)
- [API 文档](./docs/API.md)
- [项目总结](./PROJECT_SUMMARY.md)
