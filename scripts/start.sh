#!/bin/bash

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 获取脚本所在目录的父目录(项目根目录)
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}       DevToolMP 服务启动脚本${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}错误: Docker未运行,请先启动Docker${NC}"
    exit 1
fi

# 进入项目根目录
cd "$PROJECT_ROOT"

echo -e "${YELLOW}[1/4] 启动Docker服务 (MySQL, Redis)...${NC}"
docker-compose up -d

# 等待MySQL就绪
echo -e "${YELLOW}[2/4] 等待MySQL服务就绪...${NC}"
echo -e "${YELLOW}提示: 首次启动可能需要1-2分钟初始化数据库${NC}"
for i in {1..30}; do
    if docker exec devtoolmp-mysql mysqladmin ping -h localhost -uroot -prootpassword > /dev/null 2>&1; then
        echo -e "${GREEN}✓ MySQL服务已就绪${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}错误: MySQL服务启动超时${NC}"
        exit 1
    fi
    echo -n "."
    sleep 2
done
echo ""

# 等待Redis就绪
echo -e "${YELLOW}[3/4] 等待Redis服务就绪...${NC}"
for i in {1..15}; do
    if docker exec devtoolmp-redis redis-cli ping > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Redis服务已就绪${NC}"
        break
    fi
    if [ $i -eq 15 ]; then
        echo -e "${RED}错误: Redis服务启动超时${NC}"
        exit 1
    fi
    echo -n "."
    sleep 1
done
echo ""

# 检查是否需要启动后端
echo -e "${YELLOW}[4/4] 检查后端服务...${NC}"
if ! pgrep -f "devtoolmp-backend" > /dev/null; then
    echo -e "${YELLOW}启动后端服务...${NC}"
    cd "$PROJECT_ROOT/backend"
    # 检查是否已经编译
    if [ ! -f "target/devtoolmp-backend-1.0.0.jar" ]; then
        echo -e "${YELLOW}首次启动,正在编译后端项目...${NC}"
        mvn clean package -DskipTests
    fi
    # 启动后端(后台运行)
    nohup mvn spring-boot:run > "$PROJECT_ROOT/logs/backend.log" 2>&1 &
    echo $! > "$PROJECT_ROOT/logs/backend.pid"
    echo -e "${GREEN}✓ 后端服务已启动${NC}"
else
    echo -e "${GREEN}✓ 后端服务已在运行${NC}"
fi

# 检查是否需要启动前端
echo -e "${YELLOW}[5/5] 检查前端服务...${NC}"
if ! pgrep -f "vite.*frontend" > /dev/null; then
    echo -e "${YELLOW}启动前端服务...${NC}"
    cd "$PROJECT_ROOT/frontend"
    # 检查node_modules是否存在
    if [ ! -d "node_modules" ]; then
        echo -e "${YELLOW}首次启动,正在安装前端依赖...${NC}"
        npm install
    fi
    # 启动前端(后台运行)
    nohup npm run dev > "$PROJECT_ROOT/logs/frontend.log" 2>&1 &
    echo $! > "$PROJECT_ROOT/logs/frontend.pid"
    echo -e "${GREEN}✓ 前端服务已启动${NC}"
else
    echo -e "${GREEN}✓ 前端服务已在运行${NC}"
fi

echo ""
echo -e "${GREEN}================================================${NC}"
echo -e "${GREEN}       所有服务启动完成!${NC}"
echo -e "${GREEN}================================================${NC}"
echo -e "${GREEN}后端地址: ${BLUE}http://localhost:8080/api${NC}"
echo -e "${GREEN}前端地址: ${BLUE}http://localhost:5173${NC}"
echo -e "${GREEN}MySQL:   ${BLUE}localhost:3306${NC}"
echo -e "${GREEN}Redis:   ${BLUE}localhost:6379${NC}"
echo -e "${GREEN}================================================${NC}"
echo ""
echo -e "${YELLOW}提示: 使用 'tail -f logs/backend.log' 查看后端日志${NC}"
echo -e "${YELLOW}提示: 使用 'tail -f logs/frontend.log' 查看前端日志${NC}"
echo ""
