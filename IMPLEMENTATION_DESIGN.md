# DevToolMP 实现设计文档

## 1. 实现概述

### 1.1 项目结构
```
DevToolMP/
├── backend/                    # 后端项目 (Spring Boot)
│   ├── src/main/java/
│   │   └── com/devtoolmp/
│   │       ├── config/         # 配置类
│   │       ├── controller/     # 控制器
│   │       ├── dto/            # 数据传输对象
│   │       │   ├── request/    # 请求DTO
│   │       │   └── response/   # 响应DTO
│   │       ├── entity/         # 实体类
│   │       ├── exception/      # 异常处理
│   │       ├── mapper/         # MyBatis Mapper
│   │       ├── schedule/       # 定时任务
│   │       ├── service/        # 业务服务
│   │       └── DevToolMpApplication.java
│   ├── src/main/resources/
│   │   ├── mapper/             # MyBatis XML映射文件
│   │   └── application*.yml    # 配置文件
│   └── pom.xml                 # Maven配置
├── frontend/                   # 前端项目 (Vue 3)
│   ├── src/
│   │   ├── api/                # API调用封装
│   │   ├── components/         # 组件
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # 状态管理
│   │   ├── views/              # 页面视图
│   │   ├── App.vue             # 根组件
│   │   └── main.js             # 入口文件
│   └── package.json            # 依赖配置
├── docker-compose.yml          # Docker编排配置
└── docker-compose.prod.yml     # 生产环境配置
```

---

## 2. 后端实现详解

### 2.1 配置模块 (config)

#### 2.1.1 CorsConfig
**位置**: `com.devtoolmp.config.CorsConfig`

**实现**:
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
```

**作用**: 允许前端跨域访问后端API

#### 2.1.2 CacheConfig
**位置**: `com.devtoolmp.config.CacheConfig`

**实现**:
```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues();
        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }
}
```

**作用**: 配置Redis缓存管理器，设置默认缓存时间为1小时

#### 2.1.3 RestTemplateConfig
**位置**: `com.devtoolmp.config.RestTemplateConfig`

**实现**:
```java
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

**作用**: 配置RestTemplate Bean用于GitHub API调用

---

### 2.2 实体模块 (entity)

#### 2.2.1 Tool - 工具实体
**位置**: `com.devtoolmp.entity.Tool`

**核心字段**:
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID |
| name | String | 工具名称 |
| description | String | 工具描述 |
| categoryId | Long | 分类ID |
| githubOwner | String | GitHub仓库所有者 |
| githubRepo | String | GitHub仓库名称 |
| stars | Integer | GitHub星标数 |
| hotScoreDaily | BigDecimal | 日热度分数 |
| status | String | 状态 (active/inactive) |

**辅助方法**:
```java
// 实体持久化前回调
public void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    if (createdAt == null) createdAt = now;
    updatedAt = now;
}

// 获取GitHub URL
public String getGitHubUrl() {
    return "https://github.com/" + githubOwner + "/" + githubRepo;
}
```

#### 2.2.2 Rating - 评价实体
**位置**: `com.devtoolmp.entity.Rating`

**核心字段**:
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID |
| toolId | Long | 关联工具ID |
| clientIdentifier | String | 客户端标识 |
| score | Integer | 评分 (1-5) |
| comment | String | 评价内容 |

#### 2.2.3 其他实体
- `Category`: 分类实体
- `Favorite`: 收藏记录
- `ViewRecord`: 浏览记录
- `ToolTag`: 工具标签
- `CommentReply`: 评价回复
- `RatingLike`: 评价点赞

---

### 2.3 数据访问模块 (mapper)

#### 2.3.1 ToolMapper
**位置**: `com.devtoolmp.mapper.ToolMapper`

**接口方法**:
```java
public interface ToolMapper {
    Tool findById(Long id);
    List<Tool> findByStatus(String status);
    List<Tool> findByStatusWithPage(String status, int offset, int size);
    List<Tool> searchByKeyword(String keyword);
    int countByStatus(String status);
    void insert(Tool tool);
    void update(Tool tool);
    void deleteById(Long id);
}
```

**XML映射** (`mapper/ToolMapper.xml`):
```xml
<select id="findById" resultType="Tool">
    SELECT * FROM tools WHERE id = #{id}
</select>

<select id="searchByKeyword" resultType="Tool">
    SELECT * FROM tools
    WHERE status = 'active'
    AND (name LIKE CONCAT('%', #{keyword}, '%')
         OR description LIKE CONCAT('%', #{keyword}, '%'))
</select>
```

#### 2.3.2 RatingMapper
**位置**: `com.devtoolmp.mapper.RatingMapper`

**接口方法**:
```java
public interface RatingMapper {
    Rating findById(Long id);
    List<Rating> findByToolId(Long toolId);
    int countByToolId(Long toolId);
    Double getAverageScoreByToolId(Long toolId);
    void insert(Rating rating);
    void update(Rating rating);
    void deleteById(Long id);
}
```

#### 2.3.3 其他Mapper
- `CategoryMapper`: 分类数据操作
- `FavoriteMapper`: 收藏数据操作
- `ViewRecordMapper`: 浏览记录操作
- `ToolTagMapper`: 标签数据操作
- `CommentReplyMapper`: 回复数据操作
- `RatingLikeMapper`: 点赞数据操作

---

### 2.4 业务服务模块 (service)

#### 2.4.1 ToolService
**位置**: `com.devtoolmp.service.ToolService`

**核心方法**:

**创建工具**:
```java
@Transactional
public Tool createTool(ToolCreateRequest request) {
    // 1. 创建工具实体
    Tool tool = new Tool();
    tool.setName(request.getName());
    tool.setDescription(request.getDescription());
    tool.setCategoryId(request.getCategoryId());
    tool.setGithubOwner(request.getGithubOwner());
    tool.setGithubRepo(request.getGithubRepo());
    tool.prePersist();

    // 2. 保存到数据库
    toolMapper.insert(tool);

    // 3. 保存标签
    if (request.getTags() != null) {
        for (String tagName : request.getTags()) {
            ToolTag tag = new ToolTag();
            tag.setToolId(tool.getId());
            tag.setTagName(tagName.trim());
            toolTagMapper.insert(tag);
        }
    }

    return tool;
}
```

**记录浏览**:
```java
@Transactional
public void recordView(Long toolId, String clientIdentifier, String ipAddress, String userAgent) {
    // 1. 获取工具并增加浏览计数
    Tool tool = toolMapper.findById(toolId);
    tool.setViewCount(tool.getViewCount() + 1);
    tool.preUpdate();
    toolMapper.update(tool);

    // 2. 创建浏览记录
    ViewRecord record = new ViewRecord();
    record.setToolId(toolId);
    record.setClientIdentifier(clientIdentifier);
    record.setIpAddress(ipAddress);
    record.setUserAgent(userAgent);
    record.prePersist();
    viewRecordMapper.insert(record);
}
```

**切换收藏**:
```java
@Transactional
public boolean toggleFavorite(Long toolId, String clientIdentifier) {
    // 1. 检查是否已收藏
    Favorite favorite = favoriteMapper.findByClientIdentifierAndToolId(clientIdentifier, toolId);
    Tool tool = toolMapper.findById(toolId);

    if (favorite != null) {
        // 已收藏，取消收藏
        favoriteMapper.deleteByClientIdentifierAndToolId(clientIdentifier, toolId);
        tool.setFavoriteCount(tool.getFavoriteCount() - 1);
        toolMapper.update(tool);
        return false;
    } else {
        // 未收藏，添加收藏
        favorite = new Favorite();
        favorite.setClientIdentifier(clientIdentifier);
        favorite.setToolId(toolId);
        favorite.prePersist();
        favoriteMapper.insert(favorite);
        tool.setFavoriteCount(tool.getFavoriteCount() + 1);
        toolMapper.update(tool);
        return true;
    }
}
```

#### 2.4.2 GitHubService
**位置**: `com.devtoolmp.service.GitHubService`

**获取仓库信息**:
```java
@Cacheable(value = "githubRepoInfo", key = "#owner + '/' + #repo")
public Map<String, Object> fetchRepositoryInfo(String owner, String repo) {
    String url = GITHUB_API_BASE + "/repos/" + owner + "/" + repo;

    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", "application/vnd.github.v3+json");

    if (githubApiToken != null && !githubApiToken.isEmpty()) {
        headers.set("Authorization", "Bearer " + githubApiToken);
    }

    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        return response.getBody();
    }

    return null;
}
```

**同步GitHub数据**:
```java
@Transactional
public Tool syncGitHubData(Long toolId) {
    Tool tool = toolMapper.findById(toolId);

    Map<String, Object> repoInfo = fetchRepositoryInfo(tool.getGithubOwner(), tool.getGithubRepo());

    // 更新GitHub统计数据
    tool.setStars(getIntegerValue(repoInfo, "stargazers_count"));
    tool.setForks(getIntegerValue(repoInfo, "forks_count"));
    tool.setOpenIssues(getIntegerValue(repoInfo, "open_issues_count"));
    tool.setWatchers(getIntegerValue(repoInfo, "watchers_count"));

    tool.preUpdate();
    toolMapper.update(tool);

    return tool;
}
```

#### 2.4.3 RatingService
**位置**: `com.devtoolmp.service.RatingService`

**创建评价**:
```java
@Transactional
public Rating createRating(RatingCreateRequest request, String clientIdentifier) {
    // 1. 验证工具存在
    Tool tool = toolMapper.findById(request.getToolId());
    if (tool == null) {
        throw new RuntimeException("Tool not found");
    }

    // 2. 检查是否已评价
    Rating existing = ratingMapper.findByClientIdentifierAndToolId(clientIdentifier, request.getToolId());
    if (existing != null) {
        throw new RuntimeException("Already rated");
    }

    // 3. 创建评价
    Rating rating = new Rating();
    rating.setToolId(request.getToolId());
    rating.setClientIdentifier(clientIdentifier);
    rating.setScore(request.getScore());
    rating.setComment(request.getComment());
    rating.prePersist();
    ratingMapper.insert(rating);

    return rating;
}
```

#### 2.4.4 RankingService
**位置**: `com.devtoolmp.service.RankingService`

**获取排行榜**:
```java
public List<ToolRankingDTO> getRanking(String type, int limit) {
    List<Tool> tools;

    switch (type.toLowerCase()) {
        case "daily":
            tools = toolMapper.findTopByHotScoreDaily(limit);
            break;
        case "weekly":
            tools = toolMapper.findTopByHotScoreWeekly(limit);
            break;
        case "alltime":
            tools = toolMapper.findTopByHotScoreAlltime(limit);
            break;
        default:
            throw new IllegalArgumentException("Invalid ranking type");
    }

    return tools.stream()
        .map(tool -> ToolRankingDTO.fromEntity(tool))
        .collect(Collectors.toList());
}
```

---

### 2.5 控制器模块 (controller)

#### 2.5.1 ToolController
**位置**: `com.devtoolmp.controller.ToolController`

**API端点**:

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/tools` | 获取工具列表 (分页) |
| GET | `/tools/{id}` | 获取工具详情 |
| GET | `/tools/{id}/detail` | 获取工具完整详情 (含用户状态) |
| GET | `/tools/search` | 搜索工具 |
| POST | `/tools` | 创建工具 |
| PUT | `/tools/{id}` | 更新工具 |
| DELETE | `/tools/{id}` | 删除工具 |
| POST | `/tools/{id}/view` | 记录浏览 |
| POST | `/tools/{id}/favorite` | 切换收藏 |
| POST | `/tools/{id}/install` | 记录安装 |
| PUT | `/tools/{id}/publish` | 发布工具 |
| PUT | `/tools/{id}/unpublish` | 下架工具 |

**实现示例**:
```java
@RestController
@RequestMapping("/tools")
public class ToolController {

    @GetMapping
    public ResponseEntity<PageResponse<ToolDTO>> getTools(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ToolDTO> tools = toolService.getTools(page, size);
        return ResponseEntity.ok(tools);
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<Boolean> toggleFavorite(
            @PathVariable Long id,
            HttpServletRequest request) {
        String clientIdentifier = getClientIdentifier(request);
        boolean isFavorited = toolService.toggleFavorite(id, clientIdentifier);
        return ResponseEntity.ok(isFavorited);
    }

    private String getClientIdentifier(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        return ipAddress + ":" + (userAgent != null ? userAgent : "");
    }
}
```

#### 2.5.2 RatingController
**位置**: `com.devtoolmp.controller.RatingController`

**API端点**:

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/ratings/{toolId}` | 获取工具评价列表 |
| POST | `/ratings` | 创建评价 |
| POST | `/ratings/{id}/reply` | 回复评价 |
| POST | `/ratings/{id}/like` | 点赞评价 |

#### 2.5.3 RankingController
**位置**: `com.devtoolmp.controller.RankingController`

**API端点**:

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/ranking/{type}` | 获取排行榜 (daily/weekly/alltime) |

#### 2.5.4 SearchController
**位置**: `com.devtoolmp.controller.SearchController`

**API端点**:

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/search` | 搜索工具 |

---

### 2.6 定时任务模块 (schedule)

#### 2.6.1 GitHubSyncScheduler
**位置**: `com.devtoolmp.schedule.GitHubSyncScheduler`

**实现**:
```java
@Component
public class GitHubSyncScheduler {

    @Scheduled(cron = "0 0 2 * * ?")
    public void syncGitHubDataDaily() {
        log.info("Starting daily GitHub data sync at {}", LocalDateTime.now());
        try {
            var result = gitHubService.syncAllToolsGitHubData();
            log.info("GitHub data sync completed: {}", result);
        } catch (Exception e) {
            log.error("Error during daily GitHub data sync", e);
        }
    }
}
```

**执行时间**: 每天凌晨2点

---

### 2.7 异常处理模块 (exception)

#### 2.7.1 GlobalExceptionHandler
**位置**: `com.devtoolmp.exception.GlobalExceptionHandler`

**实现**:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse> handleBusinessException(BusinessException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error"));
    }
}
```

---

### 2.8 DTO模块 (dto)

#### 2.8.1 请求DTO

**ToolCreateRequest**:
```java
public class ToolCreateRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private String githubOwner;
    private String githubRepo;
    private String version;
    private String status;
    private List<String> tags;
}
```

**RatingCreateRequest**:
```java
public class RatingCreateRequest {
    @NotNull(message = "Tool ID is required")
    private Long toolId;

    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 5, message = "Score must be at most 5")
    private Integer score;

    private String comment;
}
```

#### 2.8.2 响应DTO

**ApiResponse** - 统一响应格式:
```java
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Success", data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null);
    }
}
```

**PageResponse** - 分页响应:
```java
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long total;

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long total) {
        return new PageResponse<>(content, page, size, total);
    }
}
```

---

## 3. 前端实现详解

### 3.1 项目结构
```
frontend/src/
├── api/                    # API调用
│   └── index.js           # Axios配置与API方法
├── components/            # 组件
│   ├── layout/           # 布局组件
│   │   ├── AppHeader.vue
│   │   └── AppFooter.vue
│   ├── tool/             # 工具组件
│   │   ├── ToolCard.vue
│   │   └── ToolFormDialog.vue
│   ├── rating/           # 评价组件
│   │   ├── RatingDisplay.vue
│   │   └── RatingForm.vue
│   └── home/             # 首页组件
│       ├── StatsCard.vue
│       └── AnimatedNumber.vue
├── views/                # 页面视图
│   ├── Home.vue          # 首页
│   ├── Tools.vue         # 工具列表
│   ├── ToolDetail.vue    # 工具详情
│   ├── Search.vue        # 搜索页面
│   └── Ranking.vue       # 排行榜
├── stores/               # Pinia状态管理
│   ├── tools.js
│   ├── rating.js
│   ├── ranking.js
│   └── user.js
├── router/               # 路由配置
│   └── index.js
├── App.vue               # 根组件
└── main.js               # 入口文件
```

---

### 3.2 API层 (api/index.js)

**Axios配置**:
```javascript
import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export default api
```

**API方法**:
```javascript
// 工具相关API
export const toolApi = {
  // 获取工具列表
  getTools: (page = 0, size = 20) => {
    return api.get('/tools', { params: { page, size } })
  },

  // 获取工具详情
  getToolDetail: (id) => {
    return api.get(`/tools/${id}/detail`)
  },

  // 搜索工具
  searchTools: (keyword, page = 0, size = 20) => {
    return api.get('/tools/search', { params: { keyword, page, size } })
  },

  // 切换收藏
  toggleFavorite: (id) => {
    return api.post(`/tools/${id}/favorite`)
  },

  // 记录浏览
  recordView: (id) => {
    return api.post(`/tools/${id}/view`)
  }
}

// 评价相关API
export const ratingApi = {
  // 获取工具评价
  getRatings: (toolId) => {
    return api.get(`/ratings/${toolId}`)
  },

  // 创建评价
  createRating: (data) => {
    return api.post('/ratings', data)
  }
}

// 排行榜API
export const rankingApi = {
  // 获取排行榜
  getRanking: (type = 'daily', limit = 20) => {
    return api.get(`/ranking/${type}`, { params: { limit } })
  }
}
```

---

### 3.3 状态管理 (stores/)

#### 3.3.1 tools.js
```javascript
import { defineStore } from 'pinia'
import { toolApi } from '@/api'

export const useToolsStore = defineStore('tools', {
  state: () => ({
    tools: [],
    currentTool: null,
    loading: false,
    total: 0
  }),

  actions: {
    async fetchTools(page = 0, size = 20) {
      this.loading = true
      try {
        const response = await toolApi.getTools(page, size)
        this.tools = response.content
        this.total = response.total
      } catch (error) {
        console.error('Failed to fetch tools:', error)
      } finally {
        this.loading = false
      }
    },

    async fetchToolDetail(id) {
      this.loading = true
      try {
        const response = await toolApi.getToolDetail(id)
        this.currentTool = response
        return response
      } catch (error) {
        console.error('Failed to fetch tool detail:', error)
      } finally {
        this.loading = false
      }
    }
  }
})
```

#### 3.3.2 user.js
```javascript
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    clientIdentifier: null
  }),

  actions: {
    initClientIdentifier() {
      // 生成客户端标识
      const fingerprint = this.generateFingerprint()
      this.clientIdentifier = fingerprint
    },

    generateFingerprint() {
      // 简单的指纹生成
      return 'client_' + Math.random().toString(36).substr(2, 9)
    }
  }
})
```

---

### 3.4 核心组件

#### 3.4.1 ToolCard.vue - 工具卡片组件
```vue
<template>
  <el-card class="tool-card" @click="goToDetail">
    <div class="tool-header">
      <h3>{{ tool.name }}</h3>
      <el-tag>{{ tool.categoryName }}</el-tag>
    </div>
    <p class="description">{{ tool.description }}</p>
    <div class="stats">
      <span><i class="el-icon-star-on"></i> {{ tool.stars }}</span>
      <span><i class="el-icon-view"></i> {{ tool.viewCount }}</span>
      <span><i class="el-icon-collection"></i> {{ tool.favoriteCount }}</span>
    </div>
    <div class="tags">
      <el-tag v-for="tag in tool.tags" :key="tag" size="small">
        {{ tag }}
      </el-tag>
    </div>
  </el-card>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  tool: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const goToDetail = () => {
  router.push(`/tools/${props.tool.id}`)
}
</script>
```

#### 3.4.2 RatingForm.vue - 评价表单组件
```vue
<template>
  <el-dialog v-model="visible" title="提交评价" width="500px">
    <el-form :model="form" :rules="rules" ref="formRef">
      <el-form-item label="评分" prop="score">
        <el-rate v-model="form.score" :max="5" />
      </el-form-item>
      <el-form-item label="评论" prop="comment">
        <el-input
          v-model="form.comment"
          type="textarea"
          :rows="4"
          placeholder="分享你的使用体验..."
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit">提交</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ratingApi } from '@/api'

const visible = ref(false)
const formRef = ref()
const form = reactive({
  toolId: null,
  score: 5,
  comment: ''
})

const rules = {
  score: [{ required: true, message: '请选择评分', trigger: 'change' }],
  comment: [{ required: true, message: '请输入评论内容', trigger: 'blur' }]
}

const emit = defineEmits('submitted')

const submit = async () => {
  await formRef.value.validate()
  await ratingApi.createRating(form)
  visible.value = false
  emit('submitted')
}

const open = (toolId) => {
  form.toolId = toolId
  visible.value = true
}

defineExpose({ open })
</script>
```

---

### 3.5 页面视图

#### 3.5.1 Home.vue - 首页
```vue
<template>
  <div class="home">
    <AppHeader />

    <section class="hero">
      <h1>发现优秀的开发者工具</h1>
      <p>一站式工具探索、评价与排行平台</p>
      <el-input
        v-model="searchKeyword"
        placeholder="搜索工具..."
        @keyup.enter="search"
      >
        <template #append>
          <el-button @click="search">搜索</el-button>
        </template>
      </el-input>
    </section>

    <section class="stats">
      <StatsCard
        title="收录工具"
        :value="totalTools"
        icon="el-icon-box"
      />
      <StatsCard
        title="总浏览量"
        :value="totalViews"
        icon="el-icon-view"
      />
      <StatsCard
        title="总收藏量"
        :value="totalFavorites"
        icon="el-icon-star-on"
      />
    </section>

    <section class="trending">
      <h2>热门工具</h2>
      <div class="tool-list">
        <ToolCard
          v-for="tool in trendingTools"
          :key="tool.id"
          :tool="tool"
        />
      </div>
    </section>

    <AppFooter />
  </div>
</template>
```

#### 3.5.2 ToolDetail.vue - 工具详情页
```vue
<template>
  <div class="tool-detail">
    <AppHeader />

    <div class="container" v-if="tool">
      <div class="header">
        <h1>{{ tool.name }}</h1>
        <el-tag>{{ tool.categoryName }}</el-tag>
        <el-button
          :type="tool.favorited ? 'warning' : 'default'"
          @click="toggleFavorite"
        >
          {{ tool.favorited ? '已收藏' : '收藏' }}
        </el-button>
      </div>

      <div class="info">
        <p class="description">{{ tool.description }}</p>

        <div class="github-info" v-if="tool.githubUrl">
          <h3>GitHub 仓库</h3>
          <el-link :href="tool.githubUrl" target="_blank">
            {{ tool.githubOwner }}/{{ tool.githubRepo }}
          </el-link>
          <div class="stats">
            <span>⭐ {{ tool.stars }}</span>
            <span>🍴 {{ tool.forks }}</span>
            <span>👁️ {{ tool.watchers }}</span>
            <span>🐛 {{ tool.openIssues }}</span>
          </div>
        </div>

        <div class="tags">
          <el-tag v-for="tag in tool.tags" :key="tag">
            {{ tag }}
          </el-tag>
        </div>

        <div class="stats">
          <span>👁️ 浏览 {{ tool.viewCount }}</span>
          <span>⭐ 收藏 {{ tool.favoriteCount }}</span>
          <span>📥 安装 {{ tool.installCount }}</span>
        </div>
      </div>

      <div class="ratings">
        <div class="header">
          <h2>用户评价</h2>
          <el-button type="primary" @click="ratingFormRef.open(tool.id)">
            写评价
          </el-button>
        </div>

        <div class="average">
          <span class="score">{{ tool.averageRating?.toFixed(1) || '-' }}</span>
          <span class="count">({{ tool.totalRatings }} 条评价)</span>
        </div>

        <div class="rating-list">
          <div v-for="rating in ratings" :key="rating.id" class="rating-item">
            <el-rate v-model="rating.score" disabled />
            <p>{{ rating.comment }}</p>
            <span class="time">{{ formatTime(rating.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>

    <RatingForm ref="ratingFormRef" @submitted="loadRatings" />
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useToolsStore } from '@/stores/tools'
import { toolApi } from '@/api'
import RatingForm from '@/components/rating/RatingForm.vue'

const route = useRoute()
const toolsStore = useToolsStore()

const tool = ref(null)
const ratings = ref([])
const ratingFormRef = ref(null)

const loadTool = async () => {
  tool.value = await toolsStore.fetchToolDetail(route.params.id)
  await toolApi.recordView(route.params.id)
}

const loadRatings = async () => {
  // 加载评价列表
}

const toggleFavorite = async () => {
  const result = await toolApi.toggleFavorite(tool.value.id)
  tool.value.favorited = result
}

onMounted(() => {
  loadTool()
})
</script>
```

---

### 3.6 路由配置 (router/index.js)

```javascript
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/tools',
    name: 'Tools',
    component: () => import('@/views/Tools.vue')
  },
  {
    path: '/tools/:id',
    name: 'ToolDetail',
    component: () => import('@/views/ToolDetail.vue')
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/Search.vue')
  },
  {
    path: '/ranking',
    name: 'Ranking',
    component: () => import('@/views/Ranking.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
```

---

## 4. 数据库实现

### 4.1 初始化SQL

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS devtoolmp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE devtoolmp;

-- 分类表
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 工具表
CREATE TABLE tools (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category_id BIGINT,
    github_owner VARCHAR(255),
    github_repo VARCHAR(255),
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
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- 工具标签表
CREATE TABLE tool_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tool_id BIGINT NOT NULL,
    tag_name VARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE
);

-- 评价表
CREATE TABLE ratings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tool_id BIGINT NOT NULL,
    client_identifier VARCHAR(255) NOT NULL,
    score INT NOT NULL CHECK (score >= 1 AND score <= 5),
    comment TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE,
    UNIQUE KEY unique_tool_client (tool_id, client_identifier)
);

-- 收藏表
CREATE TABLE favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tool_id BIGINT NOT NULL,
    client_identifier VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE,
    UNIQUE KEY unique_tool_client (tool_id, client_identifier)
);

-- 浏览记录表
CREATE TABLE view_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tool_id BIGINT NOT NULL,
    client_identifier VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE
);

-- 评价回复表
CREATE TABLE comment_replies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rating_id BIGINT NOT NULL,
    client_identifier VARCHAR(255) NOT NULL,
    reply_content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rating_id) REFERENCES ratings(id) ON DELETE CASCADE
);

-- 评价点赞表
CREATE TABLE rating_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rating_id BIGINT NOT NULL,
    client_identifier VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rating_id) REFERENCES ratings(id) ON DELETE CASCADE,
    UNIQUE KEY unique_rating_client (rating_id, client_identifier)
);

-- 创建索引
CREATE INDEX idx_tools_status ON tools(status);
CREATE INDEX idx_tools_category ON tools(category_id);
CREATE INDEX idx_tools_hot_daily ON tools(hot_score_daily DESC);
CREATE INDEX idx_tools_hot_weekly ON tools(hot_score_weekly DESC);
CREATE INDEX idx_tools_hot_alltime ON tools(hot_score_alltime DESC);
CREATE INDEX idx_ratings_tool ON ratings(tool_id);
CREATE INDEX idx_view_records_tool ON view_records(tool_id);
```

---

## 5. 部署配置

### 5.1 Docker Compose配置

**开发环境** (`docker-compose.yml`):
```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: devtoolmp-mysql
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: devtoolmp
      MYSQL_USER: devtool
      MYSQL_PASSWORD: devtool123
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-prootpassword"]
      timeout: 20s
      retries: 10

  redis:
    image: redis:7-alpine
    container_name: devtoolmp-redis
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: devtoolmp-backend
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/devtoolmp?useSSL=false&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: devtool
      SPRING_DATASOURCE_PASSWORD: devtool123
      SPRING_DATA_REDIS_HOST: redis
      SPRING_DATA_REDIS_PORT: 6379
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: devtoolmp-frontend
    ports:
      - "5173:5173"
    depends_on:
      - backend

volumes:
  mysql-data:
  redis-data:
```

### 5.2 Dockerfile配置

**后端Dockerfile** (`backend/Dockerfile`):
```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**前端Dockerfile** (`frontend/Dockerfile`):
```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

---

## 6. 配置文件详解

### 6.1 后端配置

**application.yml** (主配置):
```yaml
spring:
  application:
    name: devtoolmp-backend
  profiles:
    active: dev
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
  data:
    redis:
      host: redis
      port: 6379
      database: 0
      timeout: 5000ms
      jedis:
        pool:
          max-active: 8
          max-wait: -1ms
          max-idle: 8
          min-idle: 0

mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.devtoolmp.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

server:
  port: 8080
  servlet:
    context-path: /api

github:
  api:
    base-url: https://api.github.com
    token: your-github-token-here

logging:
  level:
    com.devtoolmp: DEBUG
    org.hibernate.SQL: DEBUG
```

**application-dev.yml** (开发环境):
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/devtoolmp?useSSL=false&serverTimezone=UTC
    username: devtool
    password: devtool123
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 6.2 前端配置

**vite.config.js**:
```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

---

## 7. 开发指南

### 7.1 后端开发

**启动后端**:
```bash
cd backend
mvn spring-boot:run
```

**运行测试**:
```bash
mvn test
```

**打包**:
```bash
mvn clean package
```

### 7.2 前端开发

**安装依赖**:
```bash
cd frontend
npm install
```

**启动开发服务器**:
```bash
npm run dev
```

**构建生产版本**:
```bash
npm run build
```

### 7.3 Docker部署

**启动所有服务**:
```bash
docker-compose up -d
```

**查看日志**:
```bash
docker-compose logs -f backend
```

**停止服务**:
```bash
docker-compose down
```

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2025-01-31 | 初始实现设计文档 |
