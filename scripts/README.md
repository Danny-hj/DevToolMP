# DevToolMP 脚本使用说明

本目录包含了 DevToolMP 项目的实用脚本，用于快速启动、停止、管理和维护开发环境。

## 📋 脚本列表

### 启动脚本

#### `start.sh` - 启动开发环境
一键启动 DevToolMP 开发环境，支持多种启动模式。

**使用方法:**
```bash
./scripts/start.sh
```

**启动模式:**
1. **混合模式** (推荐) - Docker MySQL/Redis + 本地后端/前端
2. **完全 Docker 模式** - 所有服务在 Docker 容器中运行
3. **仅基础设施** - 只启动 MySQL 和 Redis
4. **快速启动** - 跳过检查，使用默认配置

**功能特性:**
- ✅ 自动检查 Docker 运行状态
- ✅ 检查端口占用情况
- ✅ 等待服务健康检查完成
- ✅ 检测并提示已运行的服务
- ✅ 彩色输出，清晰直观

---

### 停止脚本

#### `stop.sh` - 停止服务
优雅地停止 DevToolMP 服务。

**使用方法:**
```bash
./scripts/stop.sh
```

**停止选项:**
1. **全部停止** - 停止 Docker + 本地服务
2. **只停止 Docker 服务**
3. **只停止本地服务**
4. **全部停止并清理数据卷** - 会删除数据库数据

**功能特性:**
- ✅ 优雅停止，等待进程结束
- ✅ 支持强制终止无响应进程
- ✅ 自动清理临时文件
- ✅ 多种方式查找进程

---

### 状态查看

#### `status.sh` - 查看服务状态
查看所有服务的运行状态。

**使用方法:**
```bash
./scripts/status.sh
```

**显示内容:**
- Docker 服务状态（MySQL、Redis、Backend、Frontend）
- 本地后端状态
- 本地前端状态
- 端口占用情况
- 快速API测试

---

### 重启服务

#### `restart.sh` - 重启服务
快速重启指定的服务。

**使用方法:**
```bash
./scripts/restart.sh
```

**重启选项:**
1. 重启所有服务
2. 只重启后端
3. 只重启前端
4. 重启 Docker 服务

---

### 日志查看

#### `logs.sh` - 查看日志
实时查看服务日志。

**交互式使用:**
```bash
./scripts/logs.sh
```

**命令行使用:**
```bash
./scripts/logs.sh backend          # Docker Backend 日志
./scripts/logs.sh frontend         # Docker Frontend 日志
./scripts/logs.sh mysql            # Docker MySQL 日志
./scripts/logs.sh redis            # Docker Redis 日志
./scripts/logs.sh local-backend    # 本地后端日志
./scripts/logs.sh local-frontend   # 本地前端日志
./scripts/logs.sh error            # 查看错误日志
./scripts/logs.sh all              # 所有服务日志

# 简写形式
./scripts/logs.sh b     # Backend
./scripts/logs.sh f     # Frontend
./scripts/logs.sh lb    # Local Backend
./scripts/logs.sh lf    # Local Frontend
./scripts/logs.sh e     # Error
./scripts/logs.sh a     # All
```

---

### 清理脚本

#### `clean.sh` - 清理项目
清理构建产物、临时文件和 Docker 资源。

**交互式使用:**
```bash
./scripts/clean.sh
```

**清理选项:**
1. **轻量清理** - 日志、PID文件
2. **中等清理** - 包括 Docker 容器
3. **完全清理** - 包括数据卷（会删除数据库数据）
4. **只清理前端构建产物**
5. **只清理后端构建产物**
6. **清理所有** - 不删除数据卷

**命令行使用:**
```bash
./scripts/clean.sh logs       # 只清理日志和PID文件
./scripts/clean.sh docker     # 清理 Docker 容器
./scripts/clean.sh volumes    # 清理数据卷（会删除数据库数据）
./scripts/clean.sh frontend   # 清理前端构建产物
./scripts/clean.sh backend    # 清理后端构建产物
./scripts/clean.sh all        # 清理所有（不删除数据卷）
```

---

## 🔧 快速命令参考

### 日常开发

```bash
# 启动开发环境（混合模式）
./scripts/start.sh

# 查看服务状态
./scripts/status.sh

# 查看日志
./scripts/logs.sh

# 停止服务
./scripts/stop.sh
```

### 调试和测试

```bash
# 只重启后端
./scripts/restart.sh
# 选择 2

# 查看后端日志
./scripts/logs.sh backend

# 查看错误日志
./scripts/logs.sh error
```

### 清理和重置

```bash
# 轻量清理（日志、临时文件）
./scripts/clean.sh
# 选择 1

# 完全重置（包括数据库）
./scripts/clean.sh volumes
```

---

## 📝 注意事项

1. **权限**: 确保所有脚本具有可执行权限：
   ```bash
   chmod +x scripts/*.sh
   ```

2. **Docker**: 大部分脚本需要 Docker 运行中

3. **端口冲突**: 脚本会自动检测端口占用并提示

4. **数据安全**: 使用清理脚本时注意选项 3/volumes 会删除数据库数据

5. **日志位置**:
   - 本地后端日志: `/tmp/devtoolmp-backend.log`
   - 本地前端日志: `/tmp/devtoolmp-frontend.log`

6. **PID 文件位置**:
   - 后端: `/tmp/devtoolmp-backend.pid`
   - 前端: `/tmp/devtoolmp-frontend.pid`

---

## 🐛 故障排除

### 脚本无法执行
```bash
chmod +x scripts/*.sh
```

### 端口被占用
```bash
# 查看占用情况
./scripts/status.sh

# 停止占用端口的服务
./scripts/stop.sh

# 或者使用清理脚本
./scripts/clean.sh
```

### 服务启动失败
```bash
# 查看错误日志
./scripts/logs.sh error

# 查看详细状态
./scripts/status.sh
```

### 数据库问题
```bash
# 完全重置数据库（会删除数据）
./scripts/clean.sh volumes
./scripts/start.sh
```

---

## 📚 相关文档

- [项目 README](../README.md)
- [架构设计](../ARCHITECTURE_DESIGN.md)
- [实现设计](../IMPLEMENTATION_DESIGN.md)
- [Docker 使用](../DOCKER.md)
