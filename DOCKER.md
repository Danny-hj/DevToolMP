# DevToolMP Docker 部署指南

## 开发环境

### 启动所有服务
```bash
docker-compose up -d
```

### 停止所有服务
```bash
docker-compose down
```

### 查看日志
```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql
```

### 重启服务
```bash
docker-compose restart backend
docker-compose restart frontend
```

## 生产环境

### 启动生产环境
```bash
docker-compose -f docker-compose.prod.yml up -d
```

## 服务访问

- **前端**: http://localhost:5173
- **后端 API**: http://localhost:8080/api
- **MySQL**: localhost:3306

  - 用户: `devtool`
  - 密码: `devtool123`
  - 数据库: `devtoolmp`

## 常见问题

### 数据库连接失败
1. 等待 MySQL 完全启动（约 10-20 秒）
2. 检查健康检查状态：`docker-compose ps`

### 前端无法访问后端
1. 确保后端服务正在运行
2. 检查网络配置：`docker network ls`

### 重建容器
```bash
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

## 清理

### 停止并删除所有容器
```bash
docker-compose down -v
```

### 删除所有数据（警告：会删除数据库数据）
```bash
docker-compose down -v
docker volume rm devtoolmp_mysql-data
```
