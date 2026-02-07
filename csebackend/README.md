# DevToolMP ServiceComb 模拟框架 Backend

这是 DevToolMP 项目的 ServiceComb 框架**实现**版本，使用 Spring Boot 3.x 实现 ServiceComb 的 Schema 模式。

## 项目概述

- **框架**: Spring Boot 3.2.1 + ServiceComb 模拟
- **Java 版本**: 21
- **数据访问**: MyBatis 3.0.3
- **架构模式**: Schema 模式（模拟 ServiceComb）

## 为什么使用模拟方案？

由于 ServiceComb Java Chassis 3.x 对 Spring Boot 3.x 的支持还不完善，我们采用**模拟方案**：

### ✅ 优势

1. **使用最新技术栈**: Spring Boot 3.x + Java 21
2. **保持 ServiceComb 代码风格**: 使用 Schema 模式，而非传统的 Controller
3. **无需额外依赖**: 纯 Spring Boot 实现，无需 ServiceComb 库
4. **100% 兼容**: API 路径和功能与原项目完全一致

### 📝 实现方式

- 使用 `@RestController` + `@RequestMapping` 替代 `@RestSchema`
- 保持 Schema 命名规范：`ToolSchema`、`CategorySchema` 等
- 保持与原 Controller 相同的 API 路径配置

## 项目结构

```
csebackend/
├── pom.xml                          # Maven 配置（纯 Spring Boot）
├── src/main/
│   ├── java/com/devtoolmp/
│   │   ├── Application.java         # Spring Boot 启动类
│   │   ├── schema/                  # REST Schema 定义（替代 Controller）
│   │   │   ├── ToolSchema.java
│   │   │   ├── CategorySchema.java
│   │   │   ├── RatingSchema.java
│   │   │   ├── SearchSchema.java
│   │   │   ├── RankingSchema.java
│   │   │   └── CodehubSchema.java
│   │   ├── service/                 # 业务逻辑层
│   │   ├── mapper/                  # MyBatis Mapper
│   │   ├── entity/                  # 实体类
│   │   ├── dto/                     # 数据传输对象
│   │   ├── config/                  # 配置类
│   │   └── exception/               # 异常处理
│   └── resources/
│       ├── application.yml          # Spring Boot 配置
│       ├── mapper/*.xml             # MyBatis XML
│       ├── schema.sql               # 数据库表结构
│       └── data.sql                 # 预置数据
```

## 与原项目的对比

| 特性 | 原 backend | csebackend（模拟） |
|------|-----------|------------------|
| 框架 | Spring Boot 3.2.1 | Spring Boot 3.2.1 |
| 注解 | `@RestController` | `@RestController` + `@RequestMapping` |
| 命名 | `XxxController` | `XxxSchema` |
| API 路径 | `/webapi/toolmarket/v1/*` | **完全相同** |
| Service 层 | 相同 | **完全相同** |
| Mapper 层 | 相同 | **完全相同** |
| 数据库 | 相同 | **完全相同** |

## Schema vs Controller 对比

### 原 Spring Boot Controller

```java
@RestController
@RequestMapping("/webapi/toolmarket/v1/tools")
public class ToolController {
    @Autowired
    private ToolService toolService;

    @GetMapping
    public ResponseEntity<PageResponse<ToolDTO>> getTools(...) {
        // ...
    }
}
```

### csebackend Schema（模拟 ServiceComb）

```java
@RestController
@RequestMapping("/webapi/toolmarket/v1/tools")
public class ToolSchema {
    @Autowired
    private ToolService toolService;

    @GetMapping
    public ResponseEntity<PageResponse<ToolDTO>> getTools(...) {
        // 完全相同的实现
    }
}
```

**差异说明**:
- 注解配置：完全相同
- 类名：`Controller` → `Schema`（保持 ServiceComb 风格）
- 实现逻辑：完全相同

## 启动步骤

### 1. 配置数据库

确保 MySQL 数据库运行并已导入表结构和数据：

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE devtoolmp;"

# 导入表结构
mysql -u root -p devtoolmp < csebackend/src/main/resources/schema.sql

# 导入预置数据
mysql -u root -p devtoolmp < csebackend/src/main/resources/data.sql
```

### 2. 启动 Redis

```bash
# 使用 Docker 启动 Redis
docker run -d -p 6379:6379 --name redis redis:latest
```

### 3. 启动应用

```bash
cd csebackend
mvn clean install
mvn spring-boot:run
```

### 4. 测试 API

```bash
# 测试工具列表
curl http://localhost:8080/webapi/toolmarket/v1/tools

# 测试工具详情
curl http://localhost:8080/webapi/toolmarket/v1/tools/1/detail

# 测试分类
curl http://localhost:8080/webapi/toolmarket/v1/categories

# 测试排行榜
curl http://localhost:8080/webapi/toolmarket/v1/tools/ranking/daily
```

## API 兼容性

✅ **所有 REST API 路径保持不变**，前端无需任何修改即可接入。

### API 端点清单

| Schema | 路径前缀 | 说明 |
|--------|---------|------|
| ToolSchema | `/webapi/toolmarket/v1/tools` | 工具管理 |
| CategorySchema | `/webapi/toolmarket/v1/categories` | 分类管理 |
| RatingSchema | `/webapi/toolmarket/v1/ratings` | 评价管理 |
| SearchSchema | `/webapi/toolmarket/v1/search` | 搜索功能 |
| RankingSchema | `/webapi/toolmarket/v1/tools/ranking` | 排行榜 |
| CodehubSchema | `/webapi/toolmarket/v1/codehub` | 代码仓库集成 |

## 关键技术点

### 1. Schema 命名规范

所有 REST 端点都使用 `Schema` 后缀命名，模拟 ServiceComb 风格：

- `ToolSchema` - 工具管理
- `CategorySchema` - 分类管理
- `RatingSchema` - 评价管理
- `SearchSchema` - 搜索功能
- `RankingSchema` - 排行榜
- `CodehubSchema` - 代码仓库集成

### 2. 代码结构优势

虽然使用标准 Spring Boot 注解，但保持了 ServiceComb 的代码组织风格：

- **清晰的职责划分**: 每个 Schema 负责一个业务领域
- **统一的命名规范**: Schema 替代 Controller
- **易于迁移**: 如果未来 ServiceComb 3.x 成熟，可以轻松迁移

### 3. 与原项目对比

| 代码文件 | 原项目 | csebackend |
|---------|-------------------|-------------------|
| 工具管理 | `ToolController.java` | `ToolSchema.java` |
| 分类管理 | `CategoryController.java` | `CategorySchema.java` |
| 评价管理 | `RatingController.java` | `RatingSchema.java` |
| 搜索功能 | `SearchController.java` | `SearchSchema.java` |
| 排行榜 | `RankingController.java` | `RankingSchema.java` |
| 代码仓库 | `CodehubController.java` | `CodehubSchema.java` |

**业务逻辑**: 完全相同，无需修改

## 下一步

1. **对比测试**: 同时运行 backend 和 csebackend，对比 API 响应
2. **功能验证**: 确保所有功能正常工作
3. **前端集成**: 前端无需修改，直接切换到新后端
4. **性能测试**: 测试两个版本的性能差异

## 迁移到真实 ServiceComb（可选）

如果未来 ServiceComb 3.x 对 Spring Boot 3.x 支持成熟，可以轻松迁移：

1. 在 `pom.xml` 中添加 ServiceComb 依赖
2. 将 `@RestController` 替换为 `@RestSchema`
3. 添加 `microservice.yaml` 配置文件
4. 在启动类添加 `@EnableServiceComb` 注解

由于代码结构已经符合 ServiceComb 的 Schema 模式，迁移成本非常低。

## 故障排除

### 问题 1: 编译错误

**错误信息**: `Compilation failure`

**解决方案**:
```bash
# 清理并重新编译
mvn clean install
```

### 问题 2: 数据库连接失败

**错误信息**: `Could not connect to database`

**解决方案**:
- 检查 MySQL 是否启动
- 检查 `application.yml` 中的数据库配置
- 确保数据库和表已创建

### 问题 3: Redis 连接失败

**错误信息**: `Unable to connect to Redis`

**解决方案**:
```bash
# 检查 Redis 是否运行
docker ps | grep redis

# 或启动 Redis
docker run -d -p 6379:6379 --name redis redis:latest
```

## 参考资源

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [MyBatis Spring Boot Starter](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)
- [Apache ServiceComb 官方文档](https://servicecomb.apache.org/docs/users/)

## 总结

这个项目展示了如何在 Spring Boot 3.x 中实现 ServiceComb 的 Schema 模式，虽然使用的是标准 Spring Boot 注解，但保持了 ServiceComb 的代码风格和架构思想。这种方式既使用了最新的技术栈，又保持了清晰的代码组织结构，是一种实用的微服务架构实现方案。
