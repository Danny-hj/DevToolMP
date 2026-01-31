# DevToolMP 实现设计文档

## 文档版本

- **版本**: 1.0.0
- **更新日期**: 2026-01-31
- **作者**: DevToolMP Team
- **状态**: 生产就绪

## 目录

1. [API 接口设计](#1-api-接口设计)
2. [数据库实现设计](#2-数据库实现设计)
3. [核心业务流程](#3-核心业务流程)
4. [关键算法实现](#4-关键算法实现)
5. [前端组件设计](#5-前端组件设计)
6. [状态管理设计](#6-状态管理设计)
7. [路由与导航设计](#7-路由与导航设计)
8. [缓存实现细节](#8-缓存实现细节)
9. [异常处理机制](#9-异常处理机制)
10. [性能优化方案](#10-性能优化方案)

---

## 1. API 接口设计

### 1.1 API 设计原则

**RESTful 规范**:
- 使用 HTTP 方法语义（GET/POST/PUT/DELETE）
- 资源导向的 URL 设计
- 统一的响应格式
- 标准 HTTP 状态码

**URL 命名规范**:
- 使用复数名词: `/api/tools`
- 层级结构: `/api/tools/{id}/ratings`
- 小写字母，连字符分隔

**版本控制**:
- 当前版本: v1（隐式）
- 未来通过 URL 路径版本: `/api/v2/tools`

### 1.2 统一响应格式

**成功响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

**错误响应**:
```json
{
  "code": 400,
  "message": "参数验证失败",
  "data": null
}
```

**分页响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [...],
    "currentPage": 0,
    "pageSize": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

### 1.3 核心 API 详细设计

#### 1.3.1 工具管理 API

**获取工具列表**
```
GET /api/tools
Query Parameters:
  - page: int (default: 0) - 页码
  - size: int (default: 20) - 每页大小
  - sort: string (default: latest) - 排序方式 (latest/hot)
  - categoryId: long (optional) - 分类筛选

Response: 200 OK
{
  "content": [
    {
      "id": 1,
      "name": "Vue.js",
      "description": "渐进式JavaScript框架",
      "categoryId": 1,
      "categoryName": "开发工具",
      "githubOwner": "vuejs",
      "githubRepo": "vue",
      "githubUrl": "https://github.com/vuejs/vue",
      "version": "3.4.0",
      "stars": 0,
      "forks": 0,
      "openIssues": 0,
      "watchers": 0,
      "viewCount": 8,
      "favoriteCount": 2,
      "installCount": 2,
      "status": "active",
      "tags": ["JavaScript", "Vue", "前端框架"],
      "createdAt": "2026-01-31T09:49:55",
      "updatedAt": "2026-01-31T10:32:27"
    }
  ],
  "currentPage": 0,
  "pageSize": 20,
  "totalElements": 3,
  "totalPages": 1
}
```

**获取工具详情**
```
GET /api/tools/{id}

Response: 200 OK
{
  "id": 10,
  "name": "Vue.js",
  "description": "渐进式JavaScript框架",
  "categoryName": "开发工具",
  "githubUrl": "https://github.com/vuejs/vue",
  "version": "3.4.0",
  "stars": 0,
  "forks": 0,
  "viewCount": 8,
  "favoriteCount": 2,
  "installCount": 2,
  "averageRating": 4.5,
  "totalRatings": 10,
  "isFavorited": true,
  "tags": ["JavaScript", "Vue"],
  "createdAt": "2026-01-31T09:49:55",
  "updatedAt": "2026-01-31T10:32:27"
}
```

**创建工具**
```
POST /api/tools
Content-Type: application/json

Request:
{
  "name": "Tool Name",
  "description": "Tool description",
  "categoryId": 1,
  "githubOwner": "owner",
  "githubRepo": "repo",
  "tags": ["tag1", "tag2"],
  "status": "active"
}

Response: 201 Created
{
  "id": 12,
  "name": "Tool Name",
  ...
}
```

**记录浏览**
```
POST /api/tools/{id}/view
Headers:
  X-Client-Identifier: auto-generated from IP+UA

Response: 200 OK
{
  "code": 200,
  "message": "浏览记录已保存"
}
```

**切换收藏**
```
POST /api/tools/{id}/favorite
Headers:
  X-Client-Identifier: auto-generated from IP+UA

Response: 200 OK
{
  "code": 200,
  "message": "success",
  "data": true  // true: 已收藏, false: 已取消
}
```

#### 1.3.2 排行榜 API

**日榜**
```
GET /api/tools/ranking/daily

Response: 200 OK
[
  {
    "id": 10,
    "name": "Vue.js",
    "description": "渐进式JavaScript框架",
    "categoryName": "开发工具",
    "hotScore": 12.40,
    "installCount": 2,
    "favoriteCount": 2,
    "viewCount": 8,
    "changePercentage": 25.5,  // 较昨日变化百分比
    "tags": ["JavaScript", "Vue"]
  },
  ...
]
```

**趋势榜**
```
GET /api/tools/ranking/trending

Response: 200 OK
[
  {
    "id": 10,
    "name": "Vue.js",
    "hotScore": 12.40,
    "changePercentage": 150.5,  // 增长最快
    ...
  }
]
```

#### 1.3.3 评分 API

**创建评分**
```
POST /api/ratings
Headers:
  X-Client-Identifier: auto-generated from IP+UA

Request:
{
  "toolId": 10,
  "score": 5,
  "comment": "非常好用的框架",
  "username": "用户昵称（可选）"
}

Response: 201 Created
{
  "id": 5,
  "toolId": 10,
  "score": 5,
  "comment": "非常好用的框架",
  "createdAt": "2026-01-31T10:35:00"
}
```

**获取评分统计**
```
GET /api/ratings/tool/{toolId}/statistics

Response: 200 OK
{
  "averageRating": 4.5,
  "totalRatings": 10,
  "scoreDistribution": {
    "5": 6,
    "4": 2,
    "3": 1,
    "2": 1,
    "1": 0
  }
}
```

#### 1.3.4 GitHub API

**同步工具数据**
```
POST /api/github/sync/{toolId}

Response: 200 OK
{
  "id": 10,
  "stars": 150000,
  "forks": 25000,
  "openIssues": 150,
  "watchers": 5000,
  "syncedAt": "2026-01-31T10:35:00"
}
```

---

## 2. 数据库实现设计

### 2.1 表结构设计

#### 2.1.1 tools 表（工具主表）

```sql
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
  hot_score_daily DECIMAL(10,2) DEFAULT 0.00,
  hot_score_weekly DECIMAL(10,2) DEFAULT 0.00,
  hot_score_alltime DECIMAL(10,2) DEFAULT 0.00,
  status VARCHAR(20) DEFAULT 'active',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
  INDEX idx_status (status),
  INDEX idx_category_id (category_id),
  INDEX idx_hot_score_daily (hot_score_daily DESC),
  INDEX idx_hot_score_weekly (hot_score_weekly DESC),
  INDEX idx_hot_score_alltime (hot_score_alltime DESC)
);
```

**字段说明**:
- `view_count_yesterday`: 昨日浏览数，用于计算变化百分比
- `hot_score_daily/weekly/alltime`: 三个维度的热度分数
- `status`: 工具状态（active=上架, inactive=下架）

#### 2.1.2 favorites 表（收藏表）

```sql
CREATE TABLE favorites (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  client_identifier VARCHAR(500) NOT NULL,
  tool_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE,
  UNIQUE KEY uk_client_tool (client_identifier, tool_id),
  INDEX idx_client_identifier (client_identifier),
  INDEX idx_tool_id (tool_id)
);
```

**设计要点**:
- 使用 `client_identifier` 替代 `user_id`
- 复合唯一索引确保同一客户端不能重复收藏
- 级联删除：工具删除时自动删除收藏记录

#### 2.1.3 ratings 表（评分表）

```sql
CREATE TABLE ratings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tool_id BIGINT NOT NULL,
  client_identifier VARCHAR(500) NOT NULL,
  score INT NOT NULL CHECK (score >= 1 AND score <= 5),
  comment TEXT,
  username VARCHAR(100),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE,
  UNIQUE KEY uk_client_tool (client_identifier, tool_id),
  INDEX idx_tool_id (tool_id),
  INDEX idx_score (score)
);
```

**约束说明**:
- `score` 字段约束：1-5 星
- 同一客户端对同一工具只能评分一次

#### 2.1.4 view_records 表（浏览记录表）

```sql
CREATE TABLE view_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tool_id BIGINT NOT NULL,
  client_identifier VARCHAR(500) NOT NULL,
  ip_address VARCHAR(50),
  user_agent VARCHAR(500),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE,
  INDEX idx_tool_id (tool_id),
  INDEX idx_created_at (created_at)
);
```

### 2.2 MyBatis Mapper 设计

#### 2.2.1 ToolMapper.xml

**查询配置**:
```xml
<mapper namespace="com.devtoolmp.mapper.ToolMapper">
  <resultMap id="BaseResultMap" type="com.devtoolmp.entity.Tool">
    <id column="id" property="id"/>
    <result column="name" property="name"/>
    <result column="description" property="description"/>
    <!-- ... 其他字段映射 -->
  </resultMap>

  <!-- 查找活跃工具（分页） -->
  <select id="findByStatus" resultMap="BaseResultMap">
    SELECT * FROM tools
    WHERE status = 'active'
    ORDER BY created_at DESC
    LIMIT #{offset}, #{limit}
  </select>

  <!-- 查找日榜前20 -->
  <select id="findTop20ByStatusOrderByHotScoreDailyDesc" resultMap="BaseResultMap">
    SELECT * FROM tools
    WHERE status = 'active'
    ORDER BY hot_score_daily DESC
    LIMIT 20
  </select>

  <!-- 更新热度分数 -->
  <update id="updateHotScores">
    UPDATE tools SET
      hot_score_daily = #{daily},
      hot_score_weekly = #{weekly},
      hot_score_alltime = #{alltime},
      updated_at = NOW()
    WHERE id = #{id}
  </update>
</mapper>
```

**驼峰命名转换**:
```yaml
mybatis:
  configuration:
    map-underscore-to-camel-case: true
```

---

## 3. 核心业务流程

### 3.1 工具浏览记录流程

```
┌─────────────┐
│  用户访问   │
│  工具详情页  │
└──────┬──────┘
       │
       ↓
┌─────────────────────────────────────────────────────────────┐
│  ToolController.recordView(id)                             │
│  1. 获取 clientIdentifier (IP + User-Agent)               │
│  2. 创建 ViewRecord 实体                                    │
│  3. 保存到数据库                                           │
│  4. 异步更新工具的 view_count                              │
└─────────────────────────────────────────────────────────────┘
       │
       ↓
┌─────────────────────────────────────────────────────────────┐
│  定时任务 (每小时)                                          │
│  1. 统计昨日的 view_count, favorite_count, install_count   │
│  2. 更新 view_count_yesterday 等字段                        │
│  3. 重新计算热度分数                                        │
└─────────────────────────────────────────────────────────────┘
```

**实现代码** (ToolController.java):
```java
@PostMapping("/{id}/view")
public ResponseEntity<?> recordView(@PathVariable Long id) {
    String clientIdentifier = getClientIdentifierFromRequest(request);

    ViewRecord viewRecord = new ViewRecord();
    viewRecord.setToolId(id);
    viewRecord.setClientIdentifier(clientIdentifier);
    viewRecord.setIpAddress(request.getRemoteAddr());
    viewRecord.setUserAgent(request.getHeader("User-Agent"));

    viewRecordMapper.insert(viewRecord);

    // 异步更新浏览数
    toolService.incrementViewCount(id);

    return ApiResponse.success();
}
```

### 3.2 收藏功能流程

```
┌─────────────┐
│ 用户点击     │
│ 收藏按钮     │
└──────┬──────┘
       │
       ↓
┌─────────────────────────────────────────────────────────────┐
│  ToolController.toggleFavorite(id)                         │
│  1. 获取 clientIdentifier                                   │
│  2. 查询是否已收藏                                          │
│  3. 如果已收藏 → 删除                                       │
│  4. 如果未收藏 → 创建                                       │
│  5. 更新工具的 favoriteCount                                │
└─────────────────────────────────────────────────────────────┘
       │
       ↓
┌─────────────────────────────────────────────────────────────┐
│  返回结果                                                   │
│  - true: 收藏成功                                           │
│  - false: 取消收藏                                          │
└─────────────────────────────────────────────────────────────┘
```

**实现代码** (ToolService.java):
```java
public boolean toggleFavorite(Long toolId) {
    String clientIdentifier = getClientIdentifier();

    Favorite favorite = favoriteMapper
        .findByClientIdentifierAndToolId(clientIdentifier, toolId);

    if (favorite != null) {
        // 取消收藏
        favoriteMapper.deleteByClientIdentifierAndToolId(clientIdentifier, toolId);
        decrementFavoriteCount(toolId);
        return false;
    } else {
        // 添加收藏
        favorite = new Favorite();
        favorite.setToolId(toolId);
        favorite.setClientIdentifier(clientIdentifier);
        favoriteMapper.insert(favorite);
        incrementFavoriteCount(toolId);
        return true;
    }
}
```

### 3.3 排行榜更新流程

```
┌─────────────────────────────────────────────────────────────┐
│  Scheduled Tasks (每10分钟执行)                             │
│  1. 获取所有活跃工具                                        │
│  2. 对每个工具:                                            │
│     - 统计昨日数据（浏览、收藏、安装）                        │
│     - 计算三个维度的热度分数                                │
│     - 更新数据库                                            │
│  3. 清除 Redis 缓存                                         │
└─────────────────────────────────────────────────────────────┘
```

**实现代码** (RankingService.java):
```java
@Scheduled(fixedRate = 600000) // 每10分钟
public void updateHotScores() {
    List<Tool> tools = toolMapper.findAll();

    for (Tool tool : tools) {
        // 获取昨日数据
        int yesterdayViews = getViewCountSince(tool.getId(), LocalDateTime.now().minusDays(1));
        int yesterdayFavorites = getFavoriteCountSince(tool.getId(), LocalDateTime.now().minusDays(1));
        int yesterdayInstalls = getInstallCountSince(tool.getId(), LocalDateTime.now().minusDays(1));

        // 更新昨日统计
        tool.setViewCountYesterday(yesterdayViews);
        tool.setFavoriteCountYesterday(yesterdayFavorites);
        tool.setInstallCountYesterday(yesterdayInstalls);

        // 计算热度分数
        BigDecimal dailyScore = calculateHotScore(
            tool.getViewCount(), tool.getFavoriteCount(), tool.getInstallCount(),
            dailyRank.getWeight() // 0.2, 0.3, 0.5
        );

        // 更新数据库
        toolMapper.updateHotScores(
            tool.getId(),
            dailyScore,
            weeklyScore,
            alltimeScore
        );
    }

    // 清除缓存
    evictRankingCaches();
}
```

---

## 4. 关键算法实现

### 4.1 热度分数计算算法

**公式**:
```
hotScore = (viewCount × w1) + (favoriteCount × 10 × w2) + (installCount × 5 × w3)

其中:
- viewCount: 浏览数
- favoriteCount: 收藏数（权重 × 10）
- installCount: 安装数（权重 × 5）
- w1, w2, w3: 不同维度的权重系数
```

**权重配置**:
- **日榜**: { view: 0.2, favorite: 0.3, install: 0.5 }
  - 最重视安装，其次是收藏

- **周榜**: { view: 0.3, favorite: 0.3, install: 0.4 }
  - 平衡考虑，安装权重略高

- **总榜**: { view: 0.4, favorite: 0.3, install: 0.3 }
  - 重视长期价值，浏览和收藏权重提升

**实现代码**:
```java
private BigDecimal calculateHotScore(int views, int favorites, int installs,
                                       double viewWeight, double favoriteWeight,
                                       double installWeight) {
    double score = (views * viewWeight)
                  + (favorites * 10 * favoriteWeight)
                  + (installs * 5 * installWeight);

    return BigDecimal.valueOf(score)
        .setScale(2, RoundingMode.HALF_UP);
}

// 使用示例
BigDecimal dailyScore = calculateHotScore(
    tool.getViewCount(),
    tool.getFavoriteCount(),
    tool.getInstallCount(),
    0.2, 0.3, 0.5  // 日榜权重
);
```

**计算示例**:
```
工具 A 数据:
- 浏览数: 1000
- 收藏数: 50
- 安装数: 20

日榜计算:
= (1000 × 0.2) + (50 × 10 × 0.3) + (20 × 5 × 0.5)
= 200 + 150 + 50
= 400

周榜计算:
= (1000 × 0.3) + (50 × 10 × 0.3) + (20 × 5 × 0.4)
= 300 + 150 + 40
= 490

总榜计算:
= (1000 × 0.4) + (50 × 10 × 0.3) + (20 × 5 × 0.3)
= 400 + 150 + 30
= 580
```

### 4.2 变化百分比计算

**公式**:
```
changePercentage = ((current - previous) / previous) × 100

如果 previous = 0，则:
- 如果 current > 0: changePercentage = 100
- 否则: changePercentage = 0
```

**实现代码**:
```java
private Double calculateChangePercentage(int currentViews, int previousViews,
                                         int currentFavorites, int previousFavorites,
                                         int currentInstalls, int previousInstalls) {
    double currentTotal = currentViews + currentFavorites * 10 + currentInstalls * 5;
    double previousTotal = previousViews + previousFavorites * 10 + previousInstalls * 5;

    if (previousTotal == 0) {
        return currentTotal > 0 ? 100.0 : 0.0;
    }

    double change = ((currentTotal - previousTotal) / previousTotal) * 100;
    return Math.round(change * 10.0) / 10.0; // 保留一位小数
}
```

### 4.3 客户端标识符生成

**算法**:
```
clientIdentifier = MD5(IP + ":" + User-Agent)

或简化版本:
clientIdentifier = IP + ":" + User-Agent
```

**实现代码**:
```java
private String getClientIdentifierFromRequest(HttpServletRequest request) {
    String ip = request.getRemoteAddr();
    String userAgent = request.getHeader("User-Agent");
    return ip + ":" + userAgent;
}
```

**安全性考虑**:
- IP 可以被伪造
- User-Agent 可以被修改
- 不涉及敏感操作，安全性可接受

---

## 5. 前端组件设计

### 5.1 组件层次结构

```
App.vue
├── AppHeader.vue (导航栏)
│   ├── Logo
│   ├── SearchBox
│   ├── NavLinks
│   └── UserActions
├── RouterView
│   ├── Home.vue
│   │   ├── Hero Section
│   │   ├── StatsCard.vue (统计卡片)
│   │   │   └── AnimatedNumber.vue (数字动画)
│   │   ├── QuickRankingCards
│   │   └── HotToolsGrid
│   │       └── ToolCard.vue (工具卡片)
│   ├── Tools.vue (工具列表页)
│   │   ├── ToolFilters
│   │   ├── ToolGrid
│   │   │   └── ToolCard.vue
│   │   └── Pagination
│   ├── ToolDetail.vue (工具详情页)
│   │   ├── ToolHeader
│   │   ├── ToolStats
│   │   ├── RatingDisplay.vue (评分展示)
│   │   └── RatingForm.vue (评分表单)
│   ├── Search.vue (搜索页)
│   └── Ranking.vue (排行榜页)
│       ├── RankingTabs
│       └── RankingTable
└── AppFooter.vue
```

### 5.2 核心组件设计

#### 5.2.1 ToolCard.vue（工具卡片）

**Props**:
```javascript
{
  tool: {
    type: Object,
    required: true
  },
  showPublishButton: {
    type: Boolean,
    default: false
  },
  showSyncButton: {
    type: Boolean,
    default: false
  }
}
```

**Emits**:
```javascript
[
  'click',        // 点击卡片
  'favorite',     // 收藏/取消
  'publish',      // 上架
  'unpublish',    // 下架
  'synced'        // 同步完成
]
```

**Features**:
- 工具信息展示（名称、描述、标签）
- GitHub 信息（stars, forks）
- 统计数据（浏览、收藏、安装）
- 热度分数徽章
- 一键复制安装命令
- 收藏按钮

#### 5.2.2 RatingDisplay.vue（评分展示）

**Props**:
```javascript
{
  statistics: {
    type: Object,
    required: true
  }
}
```

**Features**:
- 平均分展示（大号显示）
- 总评分人数
- 五星进度条
- 评分分布直方图

**实现逻辑**:
```javascript
const ratingDistribution = computed(() => {
  if (!props.statistics?.scoreDistribution) return []

  return [5, 4, 3, 2, 1].map(score => ({
    score,
    count: props.statistics.scoreDistribution[score] || 0,
    percentage: calculatePercentage(score)
  }))
})
```

#### 5.2.3 RatingForm.vue（评分表单）

**Props**:
```javascript
{
  toolId: {
    type: Number,
    required: true
  }
}
```

**Emits**:
```javascript
[
  'success'  // 评分成功
]
```

**Features**:
- 星级评分选择
- 评论输入（可选）
- 昵称输入（可选）
- 表单验证

**表单规则**:
```javascript
const rules = {
  score: [
    { required: true, message: '请选择评分', trigger: 'change' }
  ],
  comment: [
    { max: 500, message: '评论不能超过500字', trigger: 'blur' }
  ],
  username: [
    { max: 50, message: '昵称不能超过50字', trigger: 'blur' }
  ]
}
```

---

## 6. 状态管理设计

### 6.1 Pinia Store 架构

**Store 列表**:
- `tools.js` - 工具状态管理
- `rating.js` - 评分状态管理
- `ranking.js` - 排行榜状态管理
- `user.js` - 用户状态管理（简化版）

### 6.2 tools.js Store 设计

**状态**:
```javascript
{
  tools: [],           // 工具列表
  currentTool: null,   // 当前工具详情
  loading: false,      // 加载状态
  error: null,         // 错误信息
  total: 0             // 总数
}
```

**Actions**:
```javascript
{
  fetchTools(page, size),        // 获取工具列表
  fetchToolDetail(id),            // 获取工具详情
  searchTools(keyword),           // 搜索工具
  toggleFavorite(id),             // 切换收藏
  recordView(id),                 // 记录浏览
  recordInstall(id)               // 记录安装
}
```

**实现示例**:
```javascript
export const useToolsStore = defineStore('tools', () => {
  const tools = ref([])
  const currentTool = ref(null)
  const loading = ref(false)
  const error = ref(null)
  const total = ref(0)

  async function fetchTools(page = 0, size = 20) {
    loading.value = true
    error.value = null

    try {
      const response = await request.get('/tools', {
        params: { page, size }
      })
      tools.value = response.content
      total.value = response.totalElements
    } catch (err) {
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  async function toggleFavorite(id) {
    try {
      const isFavorited = await request.post(`/tools/${id}/favorite`)

      // 更新本地状态
      if (currentTool.value && currentTool.value.id === id) {
        currentTool.value.isFavorited = isFavorited
        if (isFavorited) {
          currentTool.value.favoriteCount++
        } else {
          currentTool.value.favoriteCount--
        }
      }

      return isFavorited
    } catch (error) {
      throw error
    }
  }

  return {
    tools,
    currentTool,
    loading,
    error,
    total,
    fetchTools,
    toggleFavorite
    // ... 其他方法
  }
})
```

### 6.3 ranking.js Store 设计

**状态**:
```javascript
{
  rankings: [],      // 排行榜数据
  loading: false,    // 加载状态
  error: null,       // 错误信息
  activeTab: 'alltime'  // 当前标签
}
```

**Actions**:
```javascript
{
  fetchRankings(type),    // 获取排行榜 (daily/weekly/alltime/trending)
  switchTab(tab),         // 切换标签
  refresh()               // 刷新当前榜单
}
```

**路由监听**:
```javascript
// 监听路由变化，确保每次进入排行榜都刷新数据
watch(() => route.path, (newPath) => {
  if (newPath === '/ranking') {
    fetchRankings(activeTab.value)
  }
})
```

---

## 7. 路由与导航设计

### 7.1 路由配置

**路由表**:
```javascript
const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/tools',
    name: 'Tools',
    component: Tools
  },
  {
    path: '/tools/:id',
    name: 'ToolDetail',
    component: ToolDetail,
    props: true  // 路由参数作为 props 传递
  },
  {
    path: '/search',
    name: 'Search',
    component: Search
  },
  {
    path: '/ranking',
    name: 'Ranking',
    component: Ranking
  }
]
```

### 7.2 导航守卫设计

**beforeEach 守卫**:
```javascript
router.beforeEach((to, from, next) => {
  console.log('[Router] Navigating from', from.path, 'to', to.path)

  // 关闭所有 Element Plus 对话框和遮罩层
  const overlays = document.querySelectorAll('.el-overlay')
  overlays.forEach(overlay => overlay.remove())

  // 移除对话框 body 类
  document.body.classList.remove('el-popup-parent--hidden')

  // 关闭所有对话框
  const dialogs = document.querySelectorAll('.el-dialog')
  dialogs.forEach(dialog => dialog.remove())

  next()
})
```

**问题背景**:
- Element Plus 对话框的遮罩层 (`.el-overlay`) z-index 很高（2000+）
- 路由切换时遮罩层没有被正确清理
- 导致新页面的所有点击事件被遮罩层拦截

**解决方案**:
1. 路由守卫强制清理所有遮罩层和对话框
2. 全局点击监听器作为后备方案
3. 导航链接的点击处理器

### 7.3 编程式导航

**使用示例**:
```javascript
import { useRouter } from 'vue-router'

const router = useRouter()

// 导航到工具详情
router.push(`/tools/${toolId}`)

// 带查询参数
router.push({
  name: 'Search',
  query: { q: 'Vue' }
})

// 返回上一页
router.back()

// 替换当前路由
router.replace('/ranking')
```

---

## 8. 缓存实现细节

### 8.1 Redis 配置

**application-dev.yml**:
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0
      timeout: 5000ms
      jedis:
        pool:
          max-active: 8
          max-wait: -1ms
          max-idle: 8
          min-idle: 0
```

**Redis 连接池**:
- 最大连接数: 8
- 最大等待时间: -1ms（无限等待）
- 最大空闲连接: 8
- 最小空闲连接: 0

### 8.2 缓存注解使用

**@Cacheable 注解**:
```java
@Cacheable(value = "dailyRanking", key = "'daily'")
public List<ToolRankingDTO> getDailyRanking() {
    // 方法实现
}
```

**缓存配置**:
- **value**: 缓存名称（命名空间）
- **key**: 缓存键（SpEL 表达式）
- **unless**: 条件排除
- **condition**: 条件缓存

**缓存键生成**:
```
dailyRanking::daily
weeklyRanking::weekly
allTimeRanking::alltime
trendingRanking::trending
```

### 8.3 缓存失效策略

**主动失效**:
```java
@CacheEvict(value = "dailyRanking", allEntries = true)
public void evictDailyRanking() {
    // 清除所有日榜缓存
}
```

**定时失效**:
```java
@Cacheable(value = "dailyRanking", key = "'daily'", unless = "#result == null")
```

**TTL 配置**（通过 Redis 配置）:
```java
@Bean
public RedisCacheConfiguration cacheConfiguration() {
    return RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10))  // 10分钟过期
        .disableCachingNullValues()
        .serializeKeysWith(RedisSerializationContext.SerializationPair
            .fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(RedisSerializationContext.SerializationPair
            .fromSerializer(new GenericJackson2JsonRedisSerializer()));
}
```

### 8.4 缓存预热

**启动时预热**:
```java
@PostConstruct
public void warmUpCache() {
    // 预加载排行榜数据
    getDailyRanking();
    getWeeklyRanking();
    getAllTimeRanking();
    getTrendingRankings();
}
```

---

## 9. 异常处理机制

### 9.1 异常类型定义

**业务异常**:
```java
public class BusinessException extends RuntimeException {
    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
}
```

**自定义异常**:
- `ToolNotFoundException` - 工具不存在
- `CategoryNotFoundException` - 分类不存在
- `InvalidRatingException` - 无效评分
- `DuplicateRatingException` - 重复评分

### 9.2 全局异常处理器

**GlobalExceptionHandler**:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(BusinessException e) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors()
            .stream()
            .map(DefaultMessage::getDefaultMessage)
            .collect(Collectors.joining(", "));

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("参数验证失败: " + message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception e) {
        log.error("未处理的异常", e);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("服务器内部错误"));
    }
}
```

### 9.3 前端错误处理

**Axios 拦截器**:
```javascript
request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response) {
      const { status, data } = error.response

      switch (status) {
        case 400:
          ElMessage.error(data.message || '请求参数错误')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(data.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误')
    }

    return Promise.reject(error)
  }
)
```

---

## 10. 性能优化方案

### 10.1 数据库优化

**索引优化**:
```sql
-- 复合索引
CREATE INDEX idx_status_category ON tools(status, category_id);
CREATE INDEX idx_hot_score_daily ON tools(hot_score_daily DESC);

-- 覆盖索引
CREATE INDEX idx_ranking_cover ON tools(status, hot_score_daily, id, name);
```

**查询优化**:
- 使用 `LIMIT` 限制结果集
- 避免 `SELECT *`，只查询需要的字段
- 使用 `JOIN` 代替多次查询
- 批量查询代替循环查询

**批量更新示例**:
```java
// 不推荐：N+1 查询
for (Tool tool : tools) {
    toolMapper.updateHotScores(tool.getId(), ...);
}

// 推荐：批量更新
List<Tool> tools = toolMapper.findAll();
for (Tool tool : tools) {
    calculateHotScores(tool);
}
toolMapper.batchUpdateHotScores(tools);
```

### 10.2 前端优化

**代码分割**:
```javascript
const Ranking = () => import('@/views/Ranking.vue')
const ToolDetail = () => import('@/views/ToolDetail.vue')
```

**图片优化**:
- 使用 WebP 格式
- 懒加载图片
- CDN 加速

**组件懒加载**:
```vue
<template>
  <div>
    <ToolCard v-for="tool in visibleTools" :key="tool.id" />
  </div>
</template>

<script setup>
import { ref } from 'vue'

const visibleTools = ref(tools.slice(0, 20))

// 滚动加载更多
function loadMore() {
  visibleTools.value.push(...tools.slice(offset, offset + 20))
}
</script>
```

### 10.3 API 优化

**减少请求次数**:
- 合并多个接口
- 使用 GraphQL（未来）
- 批量操作

**响应压缩**:
```yaml
server:
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain
```

### 10.4 缓存优化

**多级缓存**:
```
L1: 浏览器缓存 (1小时)
L2: CDN 缓存 (10分钟)
L3: Redis 缓存 (10分钟)
L4: 数据库查询缓存
```

**缓存预热**:
- 应用启动时预加载热门数据
- 定时任务刷新缓存
- 前端预加载下一页数据

---

## 附录

### A. 常见问题排查

**问题 1**: 排行榜数据不更新
- 检查定时任务是否运行
- 检查 Redis 连接是否正常
- 手动清除缓存: `redis-cli FLUSHDB`

**问题 2**: 浏览数统计不准确
- 检查 clientIdentifier 生成逻辑
- 检查 ViewRecord 是否正确保存
- 检查定时任务是否正确更新

**问题 3**: Element Plus 对话框无法关闭
- 检查路由守卫是否生效
- 检查 `.el-overlay` 元素是否存在
- 手动清理: `document.querySelectorAll('.el-overlay').forEach(el => el.remove())`

### B. 性能监控

**关键指标**:
- API 响应时间 < 200ms (P95)
- 首屏加载时间 < 2s
- 排行榜查询 < 100ms
- 工具详情查询 < 50ms

**监控工具**:
- Spring Boot Actuator
- Redis INFO 命令
- MySQL Slow Query Log
- Chrome DevTools Performance

### C. 开发最佳实践

**后端**:
1. 使用 `@Transactional` 确保数据一致性
2. 使用 `@Cacheable` 缓存热点数据
3. 使用 `@Validated` 验证参数
4. 使用日志记录关键操作
5. 编写单元测试覆盖核心逻辑

**前端**:
1. 使用 Composition API 组织代码
2. 使用 Pinia 管理全局状态
3. 使用 `<script setup>` 简化代码
4. 使用 computed 优化计算
5. 使用 watch 监听变化

---

**文档结束**

相关文档:
- [架构设计文档](./ARCHITECTURE_DESIGN.md)
- [CLAUDE.md](./CLAUDE.md)
- [API 文档](./docs/API.md)
