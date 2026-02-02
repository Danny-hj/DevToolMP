#!/bin/bash

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 获取脚本所在目录的父目录(项目根目录)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}       DevToolMP 服务重启脚本${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

# 停止服务
echo -e "${YELLOW}正在停止所有服务...${NC}"
bash "$SCRIPT_DIR/stop.sh"

# 等待服务完全停止
sleep 3

# 启动服务
echo -e "${YELLOW}正在启动所有服务...${NC}"
bash "$SCRIPT_DIR/start.sh"
