# DevToolMP 项目设计文档

**版本**: v1.0
**更新日期**: 2026-02-02
**项目名称**: 开发工具市场平台 (Developer Tool Marketplace)

---

## 目录

- [1. 项目概述](#1-项目概述)
- [2. 技术栈](#2-技术栈)
- [3. 系统架构](#3-系统架构)
- [4. API接口文档](#4-api接口文档)
- [5. 数据库设计](#5-数据库设计)
- [6. 数据模型](#6-数据模型)
- [7. 业务流程](#7-业务流程)
- [8. 部署说明](#8-部署说明)

---

## 1. 项目概述

### 1.1 项目简介

DevToolMP 是一个面向开发者的工具市场平台，提供：

- **工具管理**: 统一管理和展示各类开发工具
- **代码仓库集成**: 与 Codehub 集成，自动同步项目信息
- **评价系统**: 用户可以对工具进行评分和评论
- **排行榜**: 实时更新工具热度排行
- **分类浏览**: 按分类组织和展示工具

### 1.2 核心功能

| 功能模块 | 描述 |
|---------|------|
| 工具市场 | 浏览、搜索、筛选开发工具 |
| 工具详情 | 查看工具详细信息、评价、使用统计 |
| 用户交互 | 收藏、评分、评论、安装记录 |
| 数据同步 | 自动从 Codehub 同步项目信息 |
| 排行榜 | 日榜、周榜、总榜、趋势榜 |

---

## 2. 技术栈

### 2.1 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | 编程语言 |
| Spring Boot | 3.2.1 | 应用框架 |
| MyBatis | 3.5.14 | ORM框架 |
| MySQL | 8.0+ | 关系型数据库 |
| HikariCP | - | 数据库连接池 |
| Maven | - | 项目构建工具 |
| Lombok | - | 简化Java代码 |

### 2.2 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue.js | 3.x | 前端框架 |
| Vite | 5.x | 构建工具 |
| Element Plus | - | UI组件库 |
| Pinia | - | 状态管理 |
| Axios | - | HTTP客户端 |

### 2.3 开发规范

- **字符集**: UTF-8 (数据库) / utf8mb4_general_ci (排序规则)
- **API前缀**: `/webapi/toolmarket/v1`
- **端口配置**: 后端 8080 / 前端 5173

---

## 3. 系统架构

### 3.1 整体架构

```
┌─────────────┐
│   Browser   │
└──────┬──────┘
       │
       ▼
┌──────────────────────┐
│   Frontend (Vue 3)     │
│   - Port: 5173        │
│   - Vite Dev Server  │
└──────┬───────────────┘
       │ API Call
       ▼
┌──────────────────────────┐
│  Backend (Spring Boot)     │
│  - Port: 8080             │
│  - Context: /webapi/       │
│     toolmarket/v1         │
└──────┬─────────────────────┘
       │
       ▼
┌──────────────────────────┐
│   MySQL Database          │
│   - devtoolmp            │
└──────────────────────────┘
```

### 3.2 分层架构

```
┌─────────────────────────────────────┐
│          Controller 层               │
│  - 接收HTTP请求                       │
│  - 参数验证                          │
│  - 统一错误处理                      │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│           Service 层                  │
│  - 业务逻辑处理                       │
│  - 事务管理                          │
│  - 数据校验                           │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│          Mapper 层                   │
│  - 数据访问对象 (DAO)                │
│  - SQL映射                           │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│         Database Layer                │
│  - MySQL 表                          │
└─────────────────────────────────────┘
```

---

## 4. API接口文档

### 4.1 API 基础信息

- **Base URL**: `http://localhost:8080/webapi/toolmarket/v1`
- **Content-Type**: `application/json;charset=UTF-8`
- **响应格式**: JSON

### 4.2 工具管理 API

#### 4.2.1 获取工具列表

```
GET /webapi/toolmarket/v1/tools
```

**Query Parameters**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码（从0开始） |
| size | int | 否 | 20 | 每页数量 |

**响应示例**:
```json
{
  "content": [
    {
      "id": 1,
      "name": "测试工具",
      "description": "工具描述",
      "categoryId": 1,
      "categoryName": "MCP",
      "hotScoreDaily": 0.0,
      "status": "active",
      "tags": [],
      "createTime": "2026-02-02T03:22:27"
    }
  ],
  "currentPage": 0,
  "pageSize": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

#### 4.2.2 获取工具详情

```
GET /webapi/toolmarket/v1/tools/{id}/detail
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 工具ID |

**响应示例**:
```json
{
  "id": 1,
  "name": "测试工具",
  "description": "工具描述",
  "viewCount": 10,
  "favoriteCount": 5,
  "installCount": 3,
  "hotScoreDaily": 45.0,
  "hotScoreWeekly": 120.0,
  "hotScoreAlltime": 500.0,
  "status": "active",
  "tags": ["标签1", "标签2"],
  "createTime": "2026-02-02T03:22:27",
  "updateTime": "2026-02-02T03:22:27"
}
```

#### 4.2.3 创建工具

```
POST /webapi/toolmarket/v1/tools
```

**请求体**:
```json
{
  "name": "工具名称",
  "description": "工具描述（10-500字符）",
  "categoryId": 1,
  "githubOwner": "owner",
  "githubRepo": "repo",
  "version": "1.0.0",
  "status": "active"
}
```

#### 4.2.4 更新工具

```
PUT /webapi/toolmarket/v1/tools/{id}
```

#### 4.2.5 删除工具

```
DELETE /webapi/toolmarket/v1/tools/{id}
```

#### 4.2.6 搜索工具

```
GET /webapi/toolmarket/v1/tools/search?keyword=关键词
```

### 4.3 用户交互 API

#### 4.3.1 记录浏览

```
POST /webapi/toolmarket/v1/tools/{id}/view
```

#### 4.3.2 切换收藏

```
POST /webapi/toolmarket/v1/tools/{id}/favorite
```

**响应**:
```json
true  // 收藏成功
false // 取消收藏
```

#### 4.3.3 检查收藏状态

```
GET /webapi/toolmarket/v1/tools/{id}/favorite/check
```

#### 4.3.4 记录安装

```
POST /webapi/toolmarket/v1/tools/{id}/install
```

#### 4.3.5 上架/下架工具

```
PUT /webapi/toolmarket/v1/tools/{id}/publish
PUT /webapi/toolmarket/v1/tools/{id}/unpublish
```

### 4.4 分类管理 API

#### 4.4.1 获取所有分类

```
GET /webapi/toolmarket/v1/categories
```

**响应示例**:
```json
[
  {
    "id": 1,
    "name": "MCP",
    "description": "Agent MCP工具",
    "sortOrder": 1,
    "createTime": "2026-02-02T03:22:27"
  }
]
```

### 4.5 评价系统 API

#### 4.5.1 获取工具评价

```
GET /webapi/toolmarket/v1/ratings/tool/{toolId}?page=0&size=20
```

#### 4.5.2 创建评价

```
POST /webapi/toolmarket/v1/ratings/tool/{toolId}
```

**请求体**:
```json
{
  "score": 5,
  "comment": "评价内容"
}
```

#### 4.5.3 删除评价

```
DELETE /webapi/toolmarket/v1/ratings/{ratingId}
```

### 4.6 排行榜 API

#### 4.6.1 获取日榜

```
GET /webapi/toolmarket/v1/tools/ranking/daily
```

#### 4.6.2 获取周榜

```
GET /webapi/toolmarket/v1/tools/ranking/weekly
```

#### 4.6.3 获取总榜

```
GET /webapi/toolmarket/v1/tools/ranking/alltime
```

#### 4.6.4 获取趋势榜

```
GET /webapi/toolmarket/v1/tools/ranking/trending
```

### 4.7 Codehub API

#### 4.7.1 获取仓库信息

```
GET /webapi/toolmarket/v1/codehub/repos/{owner}/{repo}
```

#### 4.7.2 同步工具数据

```
POST /webapi/toolmarket/v1/codehub/sync/{toolId}
```

#### 4.7.3 批量同步 Agent Skills

```
POST /webapi/toolmarket/v1/codehub/agent-skills/auto-discover
```

---

## 5. 数据库设计

### 5.1 数据库规范

- **字符集**: `utf8mb4`
- **排序规则**: `utf8mb4_general_ci`
- **引擎**: `InnoDB`
- **命名规范**: 小写蛇形命名法 (snake_case)

### 5.2 数据表清单

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| categories | 工具分类 | id, name, description, sort_order |
| codehub | 代码仓库 | id, owner, repo, version, stars, forks |
| tools | 工具信息 | id, name, description, category_id, codehub_id |
| tool_tags | 工具标签 | id, tool_id, tag_name |
| favorites | 收藏记录 | id, tool_id, user_id |
| view_records | 浏览记录 | id, tool_id, user_id, create_time |
| install_records | 安装记录 | id, tool_id, user_id, create_time |
| ratings | 用户评价 | id, tool_id, user_id, score, comment |
| comment_replies | 评价回复 | id, rating_id, user_id, content |
| rating_likes | 评价点赞 | id, rating_id, user_id |

### 5.3 表结构详情

#### 5.3.1 分类表 (categories)

```sql
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    sort_order INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键ID | PK |
| name | VARCHAR(100) | 分类名称 | NOT NULL |
| description | TEXT | 分类描述 | - |
| sort_order | INT | 排序值 | DEFAULT 0 |
| create_time | TIMESTAMP | 创建时间 | DEFAULT CURRENT_TIMESTAMP |

#### 5.3.2 代码仓库表 (codehub)

```sql
CREATE TABLE codehub (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner VARCHAR(255) NOT NULL,
    repo VARCHAR(255) NOT NULL,
    version VARCHAR(50),
    stars INT DEFAULT 0,
    forks INT DEFAULT 0,
    open_issues INT DEFAULT 0,
    watchers INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_owner_repo (owner, repo),
    INDEX idx_stars (stars DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

**字段说明**:
- `owner`: 仓库所有者
- `repo`: 仓库名称
- `version`: 最新版本
- `stars`: Star 数量
- `forks`: Fork 数量
- `open_issues`: 开放Issue数量
- `watchers`: 观察者数量

#### 5.3.3 工具表 (tools)

```sql
CREATE TABLE tools (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category_id BIGINT,
    codehub_id BIGINT,
    status VARCHAR(20) DEFAULT 'active',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (codehub_id) REFERENCES codehub(id) ON DELETE SET NULL,
    INDEX idx_status (status),
    INDEX idx_category (category_id),
    INDEX idx_codehub (codehub_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

**状态枚举**:
- `active`: 上架
- `inactive`: 下架

#### 5.3.4 浏览记录表 (view_records)

```sql
CREATE TABLE view_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id BIGINT NOT NULL,
    user_id VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE,
    INDEX idx_tool_time (tool_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

#### 5.3.5 收藏表 (favorites)

```sql
CREATE TABLE favorites (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE,
    UNIQUE KEY uk_tool_user (tool_id, user_id),
    INDEX idx_tool (tool_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

#### 5.3.6 安装记录表 (install_records)

```sql
CREATE TABLE install_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id BIGINT NOT NULL,
    user_id VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE,
    INDEX idx_tool_time (tool_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8_general_ci;
```

#### 5.3.7 评价表 (ratings)

```sql
CREATE TABLE ratings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    username VARCHAR(255) DEFAULT NULL,
    score INT NOT NULL CHECK (score >= 1 AND score <= 5),
    comment TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE,
    UNIQUE KEY uk_tool_user (tool_id, user_id),
    INDEX idx_tool (tool_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

#### 5.3.8 评价回复表 (comment_replies)

```sql
CREATE TABLE comment_replies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rating_id BIGINT NOT NULL,
    user_id VARCHAR(500) NOT NULL,
    username VARCHAR(255) DEFAULT NULL,
    reply_to_user_id BIGINT DEFAULT NULL,
    reply_to_username VARCHAR(255) DEFAULT NULL,
    content TEXT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rating_id) REFERENCES ratings(id) ON DELETE CASCADE,
    INDEX idx_rating_id (rating_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

#### 5.3.9 评价点赞表 (rating_likes)

```sql
CREATE TABLE rating_likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rating_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rating_id) REFERENCES ratings(id) ON DELETE CASCADE,
    UNIQUE KEY uk_rating_user (rating_id, user_id),
    INDEX idx_rating (rating_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

---

## 6. 数据模型

### 6.1 实体关系图

```
Category (1) ────── (*) Tool (1)
                             │
                             │
                             ├────── (*) ToolTag (n)
                             │
                             ├────── (n) Codehub (1)
                             │
                             ├────── (n) Favorite (n)
                             │
                             ├────── (n) ViewRecord (n)
                             │
                             ├────── (n) InstallRecord (n)
                             │
                             └────── (n) Rating (n)
                                      │
                                      └── (n) CommentReply (n)
                                      │
                                      └── (n) RatingLike (n)
```

### 6.2 核心业务对象

#### Tool (工具)
- 基础信息：名称、描述
- 所属分类
- 关联的 Codehub 仓库
- 标签
- 状态（上架/下架）

#### Statistics (统计信息)
- 动态计算，不存储在数据库
- view_count: 浏览次数
- favorite_count: 收藏次数
- install_count: 安装次数
- hot_score: 热度分数

#### Ranking (排行榜)
- 按时间段分类：
  - 日榜（昨天数据）
  - 周榜（最近7天）
  - 总榜（全部时间）
  - 趋势榜（增长最快）

---

## 7. 业务流程

### 7.1 工具浏览流程

```
1. 用户访问工具列表
2. 系统自动记录浏览记录
3. 更新浏览统计
4. 按热度分数排序展示
```

### 7.2 工具收藏流程

```
1. 用户点击收藏按钮
2. 系统检查是否已收藏
3. 未收藏 → 添加收藏记录
4. 已收藏 → 删除收藏记录
5. 更新收藏统计
```

### 7.3 工具评分流程

```
1. 用户给工具打分（1-5星）
2. 添加评论内容
3. 其他用户可以点赞评论
4. 计算平均分数和统计信息
```

### 7.4 Codehub 同步流程

```
1. 定时或手动触发同步
2. 从 Codehub API 获取仓库信息
3. 更新 codehub 表数据
4. 自动发现 Agent Skills 项目
5. 批量创建或更新工具
```

---

## 8. 部署说明

### 8.1 环境要求

- **JDK**: 21+
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **Node.js**: 16+

### 8.2 配置说明

#### 后端配置 (application.yml)

```yaml
spring:
  profiles:
    active: dev

  datasource:
    url: jdbc:mysql://localhost:3306/devtoolmp
    username: devtool
    password: devtool123

server:
  port: 8080
```

#### 前端配置 (vite.config.js)

```javascript
export default defineConfig({
  server: {
    port: 5173,
    proxy: {
      '/webapi': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

### 8.3 启动命令

#### 后端启动

```bash
cd backend
mvn spring-boot:run
```

#### 前端启动

```bash
cd frontend
npm install
npm run dev
```

### 8.4 访问地址

- **前端**: http://localhost:5173
- **后端 API**: http://localhost:8080/webapi/toolmarket/v1

---

## 9. 附录

### 9.1 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 9.2 数据库编码标准

详见: [DATABASE_ENCODING_STANDARD.md](./DATABASE_ENCODING_STANDARD.md)

- **Engine**: InnoDB
- **Charset**: utf8mb4
- **Collate**: utf8mb4_general_ci
- **强制执行**: 所有表必须遵守此标准

### 9.3 API版本控制

当前版本: `v1`

API 路径格式: `/webapi/toolmarket/v1/{resource}`

未来版本扩展: `/webapi/toolmarket/v2/{resource}`

---

**文档维护**: 本文档应随着代码的更新而同步更新。

**变更记录**:
- 2026-02-02: 初始版本，记录 v1.0 架构和 API
