#!/bin/bash

# DevToolMP 项目停止脚本

echo "=== DevToolMP 停止脚本 ==="
echo ""

# 颜色
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 停止本地后端
stop_backend() {
    if [ -f /tmp/devtoolmp-backend.pid ]; then
        PID=$(cat /tmp/devtoolmp-backend.pid)
        if ps -p $PID > /dev/null 2>&1; then
            echo -e "${YELLOW}停止后端服务 (PID: $PID)...${NC}"
            kill $PID
            rm /tmp/devtoolmp-backend.pid
            echo -e "${GREEN}✓ 后端已停止${NC}"
        else
            echo "后端进程未运行"
            rm -f /tmp/devtoolmp-backend.pid
        fi
    else
        echo "后端 PID 文件不存在"
    fi
}

# 停止本地前端
stop_frontend() {
    if [ -f /tmp/devtoolmp-frontend.pid ]; then
        PID=$(cat /tmp/devtoolmp-frontend.pid)
        if ps -p $PID > /dev/null 2>&1; then
            echo -e "${YELLOW}停止前端服务 (PID: $PID)...${NC}"
            kill $PID
            rm /tmp/devtoolmp-frontend.pid
            echo -e "${GREEN}✓ 前端已停止${NC}"
        else
            echo "前端进程未运行"
            rm -f /tmp/devtoolmp-frontend.pid
        fi
    else
        echo "前端 PID 文件不存在"
    fi
}

# 停止 Docker 服务
stop_docker() {
    echo -e "${YELLOW}停止 Docker 服务...${NC}"
    docker-compose down
    echo -e "${GREEN}✓ Docker 服务已停止${NC}"
}

# 主流程
main() {
    echo "选择停止选项:"
    echo "  1) 全部停止 (Docker + 本地服务)"
    echo "  2) 只停止 Docker 服务"
    echo "  3) 只停止本地服务"
    echo ""
    read -p "请输入选项 [1-3]: " option

    case $option in
        1)
            stop_docker
            echo ""
            stop_backend
            stop_frontend
            ;;
        2)
            stop_docker
            ;;
        3)
            stop_backend
            echo ""
            stop_frontend
            ;;
        *)
            echo "无效选项"
            exit 1
            ;;
    esac

    echo ""
    echo -e "${GREEN}=== 停止完成 ===${NC}"
}

main
