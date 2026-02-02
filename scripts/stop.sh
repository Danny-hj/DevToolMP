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
echo -e "${BLUE}       DevToolMP 服务停止脚本${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

# 进入项目根目录
cd "$PROJECT_ROOT"

# 停止前端
echo -e "${YELLOW}[1/4] 停止前端服务...${NC}"
if [ -f "logs/frontend.pid" ]; then
    FRONTEND_PID=$(cat logs/frontend.pid)
    if ps -p $FRONTEND_PID > /dev/null 2>&1; then
        kill $FRONTEND_PID
        echo -e "${GREEN}✓ 前端服务已停止 (PID: $FRONTEND_PID)${NC}"
    else
        echo -e "${YELLOW}前端服务未运行${NC}"
    fi
    rm -f logs/frontend.pid
else
    # 尝试通过进程名查找并停止
    FRONTEND_PIDS=$(pgrep -f "vite.*frontend")
    if [ -n "$FRONTEND_PIDS" ]; then
        echo $FRONTEND_PIDS | xargs kill
        echo -e "${GREEN}✓ 前端服务已停止${NC}"
    else
        echo -e "${YELLOW}前端服务未运行${NC}"
    fi
fi

# 停止后端
echo -e "${YELLOW}[2/4] 停止后端服务...${NC}"
if [ -f "logs/backend.pid" ]; then
    BACKEND_PID=$(cat logs/backend.pid)
    if ps -p $BACKEND_PID > /dev/null 2>&1; then
        kill $BACKEND_PID
        echo -e "${GREEN}✓ 后端服务已停止 (PID: $BACKEND_PID)${NC}"
    else
        echo -e "${YELLOW}后端服务未运行${NC}"
    fi
    rm -f logs/backend.pid
else
    # 尝试通过进程名查找并停止
    BACKEND_PIDS=$(pgrep -f "devtoolmp-backend")
    if [ -n "$BACKEND_PIDS" ]; then
        echo $BACKEND_PIDS | xargs kill
        echo -e "${GREEN}✓ 后端服务已停止${NC}"
    else
        echo -e "${YELLOW}后端服务未运行${NC}"
    fi
fi

# 等待进程完全退出
sleep 2

# 停止Docker服务
echo -e "${YELLOW}[3/4] 停止Docker服务 (MySQL, Redis)...${NC}"
docker-compose down

echo -e "${GREEN}✓ Docker服务已停止${NC}"

echo ""
echo -e "${GREEN}================================================${NC}"
echo -e "${GREEN}       所有服务已停止${NC}"
echo -e "${GREEN}================================================${NC}"
echo ""
