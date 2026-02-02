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
echo -e "${BLUE}       DevToolMP 清理脚本${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

# 进入项目根目录
cd "$PROJECT_ROOT"

# 确认操作
echo -e "${RED}警告: 此操作将:${NC}"
echo -e "${YELLOW}  1. 停止所有服务${NC}"
echo -e "${YELLOW}  2. 删除Docker数据卷 (数据库数据)${NC}"
echo -e "${YELLOW}  3. 清理日志文件${NC}"
echo -e "${YELLOW}  4. 清理Maven构建缓存${NC}"
echo ""
read -p "确认继续? (y/n): " confirm

if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
    echo -e "${YELLOW}操作已取消${NC}"
    exit 0
fi

echo ""
echo -e "${YELLOW}[1/5] 停止所有服务...${NC}"
bash "$SCRIPT_DIR/stop.sh"

echo -e "${YELLOW}[2/5] 删除Docker数据卷...${NC}"
docker-compose down -v
echo -e "${GREEN}✓ Docker数据卷已删除${NC}"

echo -e "${YELLOW}[3/5] 清理日志文件...${NC}"
if [ -d "logs" ]; then
    rm -f logs/*.log
    rm -f logs/*.pid
    echo -e "${GREEN}✓ 日志文件已清理${NC}"
else
    echo -e "${YELLOW}日志目录不存在${NC}"
fi

echo -e "${YELLOW}[4/5] 清理Maven构建缓存...${NC}"
if [ -d "backend/target" ]; then
    rm -rf backend/target
    echo -e "${GREEN}✓ Maven构建缓存已清理${NC}"
else
    echo -e "${YELLOW}Maven构建缓存不存在${NC}"
fi

echo -e "${YELLOW}[5/5] 清理前端构建缓存...${NC}"
if [ -d "frontend/dist" ]; then
    rm -rf frontend/dist
    echo -e "${GREEN}✓ 前端构建缓存已清理${NC}"
else
    echo -e "${YELLOW}前端构建缓存不存在${NC}"
fi

echo ""
echo -e "${GREEN}================================================${NC}"
echo -e "${GREEN}       清理完成!${NC}"
echo -e "${GREEN}================================================${NC}"
echo ""
echo -e "${YELLOW}提示: 使用 './start.sh' 重新启动所有服务${NC}"
echo ""
