# API 文档

## 基础信息

- Base URL: `http://localhost:8080/api`
- 认证方式: Bearer Token

## 认证相关

### 用户登录
```
POST /auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string"
}
```

### 用户注册
```
POST /auth/register
Content-Type: application/json

{
  "username": "string",
  "email": "string",
  "password": "string"
}
```

## 工具相关

### 获取工具列表
```
GET /tools?page=0&size=20
```

### 获取工具详情
```
GET /tools/{id}
```

### 搜索工具
```
GET /tools/search?keyword=react&page=0&size=20
```

### 记录浏览
```
POST /tools/{id}/view
Authorization: Bearer {token}
```

### 切换收藏
```
POST /tools/{id}/favorite
Authorization: Bearer {token}
```

### 检查收藏状态
```
GET /tools/{id}/favorite/check
Authorization: Bearer {token}
```

## 排行榜相关

### 获取日榜
```
GET /tools/ranking/daily
```

### 获取周榜
```
GET /tools/ranking/weekly
```

### 获取总榜
```
GET /tools/ranking/alltime
```

## 评论评分相关

### 获取工具评论列表
```
GET /ratings/tool/{toolId}?page=0&size=20
```

### 获取评分统计
```
GET /ratings/tool/{toolId}/statistics
```

### 创建评论
```
POST /ratings/tool/{toolId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "score": 5,
  "comment": "string"
}
```

### 更新评论
```
PUT /ratings/{ratingId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "score": 5,
  "comment": "string"
}
```

### 删除评论
```
DELETE /ratings/{ratingId}
Authorization: Bearer {token}
```

## 搜索相关

### 全文搜索
```
GET /search?q=react&page=0&size=20
```

## 响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 错误响应
```json
{
  "code": 400,
  "message": "error message"
}
```

## 状态码

- 200: 成功
- 400: 请求错误
- 401: 未授权
- 403: 禁止访问
- 404: 资源不存在
- 500: 服务器错误
