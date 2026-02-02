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
echo -e "${BLUE}       DevToolMP 日志查看${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

# 进入项目根目录
cd "$PROJECT_ROOT"

# 检查参数
if [ -z "$1" ]; then
    echo -e "${YELLOW}用法: ./logs.sh [backend|frontend|docker]${NC}"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo -e "  ./logs.sh backend  - 查看后端日志"
    echo -e "  ./logs.sh frontend - 查看前端日志"
    echo -e "  ./logs.sh docker   - 查看Docker服务日志"
    echo ""
    exit 0
fi

case "$1" in
    backend)
        echo -e "${YELLOW}查看后端日志 (Ctrl+C 退出)...${NC}"
        echo ""
        if [ -f "logs/backend.log" ]; then
            tail -f logs/backend.log
        else
            echo -e "${RED}错误: 后端日志文件不存在${NC}"
            exit 1
        fi
        ;;
    frontend)
        echo -e "${YELLOW}查看前端日志 (Ctrl+C 退出)...${NC}"
        echo ""
        if [ -f "logs/frontend.log" ]; then
            tail -f logs/frontend.log
        else
            echo -e "${RED}错误: 前端日志文件不存在${NC}"
            exit 1
        fi
        ;;
    docker)
        echo -e "${YELLOW}查看Docker服务日志 (Ctrl+C 退出)...${NC}"
        echo ""
        echo -e "${YELLOW}选择服务:${NC}"
        echo -e "  1) MySQL"
        echo -e "  2) Redis"
        echo ""
        read -p "请输入选项 (1 或 2): " choice

        case "$choice" in
            1)
                docker logs -f devtoolmp-mysql
                ;;
            2)
                docker logs -f devtoolmp-redis
                ;;
            *)
                echo -e "${RED}无效选项${NC}"
                exit 1
                ;;
        esac
        ;;
    *)
        echo -e "${RED}错误: 无效的参数 '$1'${NC}"
        echo -e "${YELLOW}有效参数: backend, frontend, docker${NC}"
        exit 1
        ;;
esac
