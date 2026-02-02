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
echo -e "${BLUE}       DevToolMP 服务状态${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

# 进入项目根目录
cd "$PROJECT_ROOT"

# 检查Docker服务
echo -e "${YELLOW}[Docker服务]${NC}"
if docker ps | grep -q "devtoolmp-mysql"; then
    echo -e "${GREEN}✓ MySQL${NC} - 运行中"
else
    echo -e "${RED}✗ MySQL${NC} - 未运行"
fi

if docker ps | grep -q "devtoolmp-redis"; then
    echo -e "${GREEN}✓ Redis${NC} - 运行中"
else
    echo -e "${RED}✗ Redis${NC} - 未运行"
fi
echo ""

# 检查后端服务
echo -e "${YELLOW}[后端服务]${NC}"
if pgrep -f "devtoolmp-backend" > /dev/null; then
    BACKEND_PID=$(pgrep -f "devtoolmp-backend" | head -1)
    echo -e "${GREEN}✓ 后端服务${NC} - 运行中 (PID: $BACKEND_PID)"
    # 检查端口
    if lsof -i :8080 > /dev/null 2>&1; then
        echo -e "  端口: ${GREEN}8080${NC} 已监听"
    else
        echo -e "  端口: ${RED}8080${NC} 未监听"
    fi
else
    echo -e "${RED}✗ 后端服务${NC} - 未运行"
fi
echo ""

# 检查前端服务
echo -e "${YELLOW}[前端服务]${NC}"
if pgrep -f "vite.*frontend" > /dev/null; then
    FRONTEND_PID=$(pgrep -f "vite.*frontend" | head -1)
    echo -e "${GREEN}✓ 前端服务${NC} - 运行中 (PID: $FRONTEND_PID)"
    # 检查端口
    if lsof -i :5173 > /dev/null 2>&1; then
        echo -e "  端口: ${GREEN}5173${NC} 已监听"
    else
        echo -e "  端口: ${RED}5173${NC} 未监听"
    fi
else
    echo -e "${RED}✗ 前端服务${NC} - 未运行"
fi
echo ""

echo -e "${BLUE}================================================${NC}"
