# DevToolMP - 工具市场平台

DevToolMP 是一个仿照 skills.sh 风格的工具市场平台，提供工具展示、热度排行、评论评分等功能，支持 GitHub 深度集成。

## 技术栈

### 后端
- Spring Boot 3.x
- Spring Security + JWT
- Spring Data JPA + MySQL 8.0
- JUnit 5 + Mockito + Testcontainers

### 前端
- Vue 3 (Composition API)
- Vue Router
- Pinia
- Axios
- Element Plus
- Vitest + Vue Test Utils

## 快速开始

### 前置要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+ (或使用 Docker)

### 使用 Docker 启动（推荐）

1. 启动开发环境：
```bash
chmod +x scripts/start-dev.sh
./scripts/start-dev.sh
```

2. 访问应用：
- 前端：http://localhost:5173
- 后端：http://localhost:8080/api

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

### 运行所有测试
```bash
chmod +x scripts/test-all.sh
./scripts/test-all.sh
```

### 单独运行后端测试
```bash
cd backend
./mvnw test
```

### 单独运行前端测试
```bash
cd frontend
npm run test:unit
```

## 项目结构

```
DevToolMP/
├── backend/              # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/devtoolmp/
│   │   │   │       ├── config/
│   │   │   │       ├── controller/
│   │   │   │       ├── service/
│   │   │   │       ├── repository/
│   │   │   │       ├── entity/
│   │   │   │       ├── dto/
│   │   │   │       └── util/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── frontend/             # 前端项目
│   ├── src/
│   │   ├── components/
│   │   ├── views/
│   │   ├── stores/
│   │   ├── api/
│   │   └── router/
│   └── package.json
├── docs/                 # 文档
├── scripts/              # 脚本
└── docker-compose.yml
```

## 主要功能

- 工具展示和搜索
- 热度排行（日榜、周榜、总榜）
- 评论和评分系统
- 收藏功能
- GitHub 集成

## 配置

### 后端配置
编辑 `backend/src/main/resources/application-dev.yml`

### 前端配置
编辑 `frontend/vite.config.js`

## 开发

### 添加新功能
1. 后端：在 `backend/src/main/java/com/devtoolmp/` 添加代码
2. 前端：在 `frontend/src/` 添加代码

### 代码风格
- 后端：遵循 Java 代码规范
- 前端：遵循 Vue 3 组合式 API 最佳实践

## License

MIT
