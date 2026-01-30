# DevToolMP - 工具市场项目规划

## 项目概述

DevToolMP 是一个仿照 skills.sh 风格的工具市场平台，提供工具展示、热度排行、评论评分等功能，支持 GitHub 深度集成。

## 技术栈

### 后端
- **框架**: Spring Boot 3.x
- **安全**: Spring Security + JWT
- **数据库**: Spring Data JPA + MySQL 8.0
- **GitHub**: GitHub API Integration
- **测试**: JUnit 5 + Mockito + Testcontainers

### 前端
- **框架**: Vue 3 (Composition API)
- **路由**: Vue Router
- **状态管理**: Pinia
- **HTTP**: Axios
- **UI**: Element Plus
- **工具库**: VueUse
- **测试**: Vitest + Vue Test Utils

## 数据库设计

```sql
-- 用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    avatar VARCHAR(500),
    bio TEXT,
    github_url VARCHAR(500),
    github_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_github_id (github_id)
);

-- 分类表
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    icon VARCHAR(50),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 工具表
CREATE TABLE tools (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category_id BIGINT,
    github_owner VARCHAR(100),
    github_repo VARCHAR(100),
    version VARCHAR(50),
    stars INT DEFAULT 0,
    forks INT DEFAULT 0,
    open_issues INT DEFAULT 0,
    watchers INT DEFAULT 0,
    view_count INT DEFAULT 0,
    favorite_count INT DEFAULT 0,
    install_count INT DEFAULT 0,
    view_count_yesterday INT DEFAULT 0,
    favorite_count_yesterday INT DEFAULT 0,
    install_count_yesterday INT DEFAULT 0,
    hot_score_daily DECIMAL(10,2) DEFAULT 0,
    hot_score_weekly DECIMAL(10,2) DEFAULT 0,
    hot_score_alltime DECIMAL(10,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_status (status),
    INDEX idx_hot_daily (hot_score_daily DESC),
    INDEX idx_hot_weekly (hot_score_weekly DESC),
    INDEX idx_hot_alltime (hot_score_alltime DESC)
);

-- 工具标签关联表
CREATE TABLE tool_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id BIGINT NOT NULL,
    tag_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE,
    INDEX idx_tool_tag (tool_id, tag_name),
    INDEX idx_tag (tag_name)
);

-- 收藏表
CREATE TABLE favorites (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    tool_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_tool (user_id, tool_id),
    INDEX idx_user (user_id),
    INDEX idx_tool (tool_id)
);

-- 浏览记录表
CREATE TABLE view_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id BIGINT NOT NULL,
    user_id BIGINT,
    ip_address VARCHAR(50),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_tool_date (tool_id, created_at)
);

-- 遥测数据表
CREATE TABLE telemetry_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id BIGINT NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    ip_address VARCHAR(50),
    user_agent TEXT,
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE,
    INDEX idx_tool_date (tool_id, created_at),
    INDEX idx_type (event_type)
);

-- 评分表
CREATE TABLE ratings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    score INT NOT NULL CHECK (score >= 1 AND score <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_tool (user_id, tool_id),
    INDEX idx_tool (tool_id),
    INDEX idx_user (user_id)
);

-- 评论回复表
CREATE TABLE comment_replies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rating_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reply_to_user_id BIGINT,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rating_id) REFERENCES ratings(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (reply_to_user_id) REFERENCES users(id),
    INDEX idx_rating (rating_id)
);

-- 评论点赞表
CREATE TABLE rating_likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rating_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rating_id) REFERENCES comment_replies(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_rating (user_id, rating_id),
    INDEX idx_rating (rating_id)
);
```

## 项目结构

```
DevToolMP/
├── backend/                                    # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/devtoolmp/
│   │   │   │       ├── config/
│   │   │   │       │   ├── SecurityConfig.java
│   │   │   │       │   ├── GitHubOAuthConfig.java
│   │   │   │       │   ├── CorsConfig.java
│   │   │   │       │   └── MySQLConfig.java
│   │   │   │       ├── controller/
│   │   │   │       │   ├── AuthController.java
│   │   │   │       │   ├── ToolController.java
│   │   │   │       │   ├── SearchController.java
│   │   │   │       │   ├── RankingController.java
│   │   │   │       │   ├── UserController.java
│   │   │   │       │   └── RatingController.java
│   │   │   │       ├── service/
│   │   │   │       │   ├── AuthService.java
│   │   │   │       │   ├── ToolService.java
│   │   │   │       │   ├── RankingService.java
│   │   │   │       │   ├── SearchService.java
│   │   │   │       │   ├── GitHubService.java
│   │   │   │       │   ├── TelemetryService.java
│   │   │   │       │   └── RatingService.java
│   │   │   │       ├── repository/
│   │   │   │       │   ├── UserRepository.java
│   │   │   │       │   ├── ToolRepository.java
│   │   │   │       │   ├── CategoryRepository.java
│   │   │   │       │   ├── FavoriteRepository.java
│   │   │   │       │   ├── ViewRecordRepository.java
│   │   │   │       │   ├── TelemetryRepository.java
│   │   │   │       │   ├── RatingRepository.java
│   │   │   │       │   └── CommentReplyRepository.java
│   │   │   │       ├── entity/
│   │   │   │       │   ├── User.java
│   │   │   │       │   ├── Tool.java
│   │   │   │       │   ├── Category.java
│   │   │   │       │   ├── ToolTag.java
│   │   │   │       │   ├── Favorite.java
│   │   │   │       │   ├── ViewRecord.java
│   │   │   │       │   ├── TelemetryData.java
│   │   │   │       │   ├── Rating.java
│   │   │   │       │   ├── CommentReply.java
│   │   │   │       │   └── RatingLike.java
│   │   │   │       ├── dto/
│   │   │   │       │   ├── request/
│   │   │   │       │   │   ├── ToolCreateRequest.java
│   │   │   │       │   │   ├── ToolUpdateRequest.java
│   │   │   │       │   │   ├── RatingCreateRequest.java
│   │   │   │       │   │   └── CommentReplyRequest.java
│   │   │   │       │   ├── response/
│   │   │   │       │   │   ├── ToolDTO.java
│   │   │   │       │   │   ├── ToolRankingDTO.java
│   │   │   │       │   │   ├── ToolDetailDTO.java
│   │   │   │       │   │   ├── RatingDTO.java
│   │   │   │       │   │   └── CommentReplyDTO.java
│   │   │   │       │   └── GitHubRepoDTO.java
│   │   │   │       ├── github/
│   │   │   │       │   ├── GitHubApiClient.java
│   │   │   │       │   ├── GitHubWebhookController.java
│   │   │   │       │   └── ReadmeParser.java
│   │   │   │       ├── scheduler/
│   │   │   │       │   ├── HotScoreScheduler.java
│   │   │   │       │   └── RatingStatisticsScheduler.java
│   │   │   │       ├── util/
│   │   │   │       │   ├── JwtUtil.java
│   │   │   │       │   ├── HotScoreCalculator.java
│   │   │   │       │   └── MarkdownUtil.java
│   │   │   │       └── DevToolMpApplication.java
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       ├── application-test.yml
│   │   │       ├── schema.sql
│   │   │       └── data.sql
│   │   └── test/
│   │       └── java/
│   │           └── com/devtoolmp/
│   │               ├── service/
│   │               │   ├── AuthServiceTest.java
│   │               │   ├── ToolServiceTest.java
│   │               │   ├── RankingServiceTest.java
│   │               │   ├── SearchServiceTest.java
│   │               │   └── RatingServiceTest.java
│   │               ├── repository/
│   │               │   ├── ToolRepositoryTest.java
│   │               │   └── RatingRepositoryTest.java
│   │               ├── controller/
│   │               │   ├── ToolControllerTest.java
│   │               │   └── RatingControllerTest.java
│   │               ├── integration/
│   │               │   ├── GitHubServiceIntegrationTest.java
│   │               │   └── EndToEndTest.java
│   │               └── util/
│   │                   └── HotScoreCalculatorTest.java
│   ├── pom.xml
│   ├── docker-compose.yml
│   └── Dockerfile
│
├── frontend/                                   # 前端项目
│   ├── public/
│   ├── src/
│   │   ├── assets/
│   │   │   ├── styles/
│   │   │   │   ├── variables.scss
│   │   │   │   └── dark-theme.scss
│   │   │   └── icons/
│   │   ├── components/
│   │   │   ├── layout/
│   │   │   │   ├── AppHeader.vue
│   │   │   │   ├── AppFooter.vue
│   │   │   │   └── AppSidebar.vue
│   │   │   ├── tool/
│   │   │   │   ├── ToolCard.vue
│   │   │   │   ├── ToolList.vue
│   │   │   │   ├── ToolDetail.vue
│   │   │   │   ├── ToolDocs.vue
│   │   │   │   └── InstallCommand.vue
│   │   │   ├── ranking/
│   │   │   │   ├── HotRanking.vue
│   │   │   │   ├── DailyRanking.vue
│   │   │   │   └── WeeklyRanking.vue
│   │   │   ├── rating/
│   │   │   │   ├── RatingDisplay.vue
│   │   │   │   ├── RatingForm.vue
│   │   │   │   ├── RatingList.vue
│   │   │   │   ├── RatingItem.vue
│   │   │   │   └── CommentReply.vue
│   │   │   ├── auth/
│   │   │   │   ├── Login.vue
│   │   │   │   ├── Register.vue
│   │   │   │   └── GitHubAuth.vue
│   │   │   └── common/
│   │   │       ├── SearchBar.vue
│   │   │       ├── CopyButton.vue
│   │   │       └── MarkdownRenderer.vue
│   │   ├── views/
│   │   │   ├── Home.vue
│   │   │   ├── Tools.vue
│   │   │   ├── ToolDetail.vue
│   │   │   ├── ToolDocs.vue
│   │   │   ├── Search.vue
│   │   │   ├── Profile.vue
│   │   │   └── SubmitTool.vue
│   │   ├── stores/
│   │   │   ├── user.js
│   │   │   ├── tools.js
│   │   │   ├── search.js
│   │   │   └── rating.js
│   │   ├── api/
│   │   │   ├── index.js
│   │   │   ├── github.js
│   │   │   ├── telemetry.js
│   │   │   └── rating.js
│   │   ├── router/
│   │   │   └── index.js
│   │   ├── utils/
│   │   │   ├── hotScore.js
│   │   │   └── markdown.js
│   │   ├── App.vue
│   │   └── main.js
│   ├── tests/
│   │   ├── unit/
│   │   │   ├── components/
│   │   │   │   ├── ToolCard.spec.js
│   │   │   │   ├── RatingDisplay.spec.js
│   │   │   │   ├── RatingForm.spec.js
│   │   │   │   └── RatingList.spec.js
│   │   │   ├── stores/
│   │   │   │   ├── tools.spec.js
│   │   │   │   └── rating.spec.js
│   │   │   └── utils/
│   │   │       └── hotScore.spec.js
│   │   └── e2e/
│   │       ├── auth.spec.js
│   │       ├── tools.spec.js
│   │       └── ratings.spec.js
│   ├── package.json
│   ├── vite.config.js
│   ├── vitest.config.js
│   └── docker-compose.yml
│
├── docs/                                       # 项目文档
│   ├── README.md
│   ├── API.md
│   ├── DEPLOY.md
│   ├── TEST.md
│   └── LOCAL_SETUP.md
│
└── scripts/                                    # 脚本工具
    ├── start-dev.sh
    ├── test-all.sh
    └── init-db.sh
```

## API 设计

### 认证相关
```
POST   /api/auth/register       # 用户注册
POST   /api/auth/login          # 用户登录
POST   /api/auth/github/callback # GitHub OAuth 回调
POST   /api/auth/refresh        # 刷新 Token
```

### 工具相关
```
GET    /api/tools                       # 获取工具列表
GET    /api/tools/{id}                  # 获取工具详情
GET    /api/tools/{id}/docs             # 获取工具文档
GET    /api/tools/hot/daily             # 日榜 Top 20
GET    /api/tools/hot/weekly            # 周榜 Top 20
GET    /api/tools/hot/alltime           # 总榜 Top 20
GET    /api/tools/trending              # 趋势上升
POST   /api/tools                       # 创建工具
PUT    /api/tools/{id}                  # 更新工具
DELETE /api/tools/{id}                  # 删除工具
POST   /api/tools/{id}/view             # 记录浏览
POST   /api/tools/{id}/favorite         # 收藏/取消收藏
POST   /api/tools/{id}/install          # 记录安装
```

### 搜索相关
```
GET    /api/search?q={query}            # 全文搜索
GET    /api/search/suggest?q={query}    # 搜索建议
```

### 评分评论相关
```
GET    /api/tools/{toolId}/ratings              # 获取评分评论
GET    /api/tools/{toolId}/ratings/statistics   # 获取评分统计
GET    /api/ratings/{ratingId}                  # 获取单条评分
GET    /api/ratings/{ratingId}/replies          # 获取回复列表
POST   /api/tools/{toolId}/ratings              # 创建评分评论
PUT    /api/ratings/{ratingId}                  # 更新评分
DELETE /api/ratings/{ratingId}                  # 删除评分
POST   /api/ratings/{ratingId}/replies          # 回复评论
DELETE /api/ratings/replies/{replyId}           # 删除回复
POST   /api/ratings/replies/{replyId}/likes     # 点赞/取消点赞
```

### GitHub 集成
```
GET    /api/github/repo/{owner}/{repo}           # 获取仓库信息
GET    /api/github/readme/{owner}/{repo}         # 获取 README
POST   /api/github/sync                          # 同步数据
POST   /api/github/webhook                       # Webhook 接收
```

### 用户相关
```
GET    /api/users/profile              # 获取当前用户信息
PUT    /api/users/profile              # 更新用户信息
GET    /api/users/{id}/favorites       # 获取用户收藏
GET    /api/users/{id}/submitted       # 获取用户提交的工具
```

### 遥测 API
```
POST   /api/telemetry/install          # 上报安装数据
POST   /api/telemetry/view             # 上报浏览数据
```

## 热度计算算法

```javascript
// 热度计算公式
hotScore = (viewCount * 1) + (favoriteCount * 10) + (installs * 5)

// 变化计算
change = ((currentValue - lastValue) / lastValue) * 100

// 排行权重
dailyRank = {
  viewCount: 0.2,
  favoriteCount: 0.3,
  installs: 0.5
}

weeklyRank = {
  viewCount: 0.3,
  favoriteCount: 0.3,
  installs: 0.4
}

alltimeRank = {
  viewCount: 0.4,
  favoriteCount: 0.3,
  installs: 0.3
}
```

## 测试覆盖率目标

- **后端**: > 80%
- **前端**: > 70%

## 本地开发环境

### Docker Compose

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: devtoolmp
      MYSQL_USER: devtool
      MYSQL_PASSWORD: devtool123
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - mysql

  frontend:
    build: ./frontend
    ports:
      - "5173:5173"

volumes:
  mysql-data:
```

## 开发步骤

1. 项目初始化
2. 数据库和实体设计
3. 后端核心功能开发
4. 前端核心功能开发
5. 测试编写
6. 优化完善
7. 文档和部署准备
