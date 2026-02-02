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
echo -e "${BLUE}       DevToolMP 开发模式启动${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""
echo -e "${YELLOW}说明: 此脚本仅启动基础服务(MySQL, Redis)${NC}"
echo -e "${YELLOW}      前后端需要在独立终端手动启动${NC}"
echo ""

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}错误: Docker未运行,请先启动Docker${NC}"
    exit 1
fi

# 进入项目根目录
cd "$PROJECT_ROOT"

# 启动Docker服务
echo -e "${YELLOW}[1/2] 启动Docker服务 (MySQL, Redis)...${NC}"
docker-compose up -d

# 等待MySQL就绪
echo -e "${YELLOW}[2/2] 等待服务就绪...${NC}"
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

echo ""
echo -e "${GREEN}================================================${NC}"
echo -e "${GREEN}       基础服务启动完成!${NC}"
echo -e "${GREEN}================================================${NC}"
echo ""
echo -e "${BLUE}请在独立终端中运行以下命令启动前后端:${NC}"
echo ""
echo -e "${YELLOW}启动后端:${NC}"
echo -e "  cd backend"
echo -e "  mvn spring-boot:run"
echo ""
echo -e "${YELLOW}启动前端:${NC}"
echo -e "  cd frontend"
echo -e "  npm run dev"
echo ""
echo -e "${GREEN}MySQL:   ${BLUE}localhost:3306${NC}"
echo -e "${GREEN}Redis:   ${BLUE}localhost:6379${NC}"
echo -e "${GREEN}================================================${NC}"
echo ""
