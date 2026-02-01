#!/bin/bash

# DevToolMP 项目停止脚本

echo "=== DevToolMP 停止脚本 ==="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 项目根目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_ROOT"

# 停止本地后端
stop_backend() {
    echo -e "${BLUE}[停止]${NC} 本地后端服务..."

    if [ -f /tmp/devtoolmp-backend.pid ]; then
        PID=$(cat /tmp/devtoolmp-backend.pid)
        if ps -p $PID > /dev/null 2>&1; then
            echo -e "${YELLOW}[终止]${NC} 后端进程 (PID: $PID)..."
            kill $PID 2>/dev/null || true

            # 等待进程结束
            local retries=0
            while ps -p $PID > /dev/null 2>&1 && [ $retries -lt 10 ]; do
                sleep 1
                retries=$((retries + 1))
            done

            # 如果还在运行，强制终止
            if ps -p $PID > /dev/null 2>&1; then
                echo -e "${YELLOW}[强制]${NC} 强制终止后端..."
                kill -9 $PID 2>/dev/null || true
                sleep 1
            fi

            rm -f /tmp/devtoolmp-backend.pid
            echo -e "${GREEN}✓ 后端已停止${NC}"
        else
            echo -e "${YELLOW}⚠ 后端进程未运行${NC}"
            rm -f /tmp/devtoolmp-backend.pid
        fi
    else
        echo -e "${YELLOW}⚠ 后端 PID 文件不存在${NC}"
    fi
}

# 停止本地前端
stop_frontend() {
    echo -e "${BLUE}[停止]${NC} 本地前端服务..."

    # 尝试多种方式查找并停止前端进程
    local stopped=false

    # 方式1: 使用PID文件
    if [ -f /tmp/devtoolmp-frontend.pid ]; then
        PID=$(cat /tmp/devtoolmp-frontend.pid)
        if ps -p $PID > /dev/null 2>&1; then
            echo -e "${YELLOW}[终止]${NC} 前端进程 (PID: $PID)..."
            kill $PID 2>/dev/null || true
            sleep 2

            # 强制终止如果还在运行
            if ps -p $PID > /dev/null 2>&1; then
                kill -9 $PID 2>/dev/null || true
            fi

            rm -f /tmp/devtoolmp-frontend.pid
            stopped=true
        else
            rm -f /tmp/devtoolmp-frontend.pid
        fi
    fi

    # 方式2: 查找端口5173的进程
    if ! $stopped; then
        FRONTEND_PIDS=$(lsof -ti:5173 2>/dev/null || true)
        if [ -n "$FRONTEND_PIDS" ]; then
            echo -e "${YELLOW}[终止]${NC} 前端进程 (端口 5173)..."
            echo $FRONTEND_PIDS | xargs kill 2>/dev/null || true
            sleep 2
            echo $FRONTEND_PIDS | xargs kill -9 2>/dev/null || true
            stopped=true
        fi
    fi

    # 方式3: 查找npm/vite进程
    if ! $stopped; then
        VITE_PIDS=$(pgrep -f "vite.*5173" 2>/dev/null || true)
        if [ -n "$VITE_PIDS" ]; then
            echo -e "${YELLOW}[终止]${NC} Vite 进程..."
            echo $VITE_PIDS | xargs kill 2>/dev/null || true
            sleep 2
            stopped=true
        fi
    fi

    if $stopped || [ -f /tmp/devtoolmp-frontend.pid ]; then
        echo -e "${GREEN}✓ 前端已停止${NC}"
    else
        echo -e "${YELLOW}⚠ 未找到运行中的前端进程${NC}"
    fi

    rm -f /tmp/devtoolmp-frontend.pid
}

# 停止 Docker 服务
stop_docker() {
    echo -e "${BLUE}[停止]${NC} Docker 服务..."

    if docker-compose ps -q 2>/dev/null | grep -q .; then
        echo -e "${YELLOW}[停止]${NC} Docker 容器..."
        docker-compose down
        echo -e "${GREEN}✓ Docker 服务已停止${NC}"
    else
        echo -e "${YELLOW}⚠ 没有 Docker 服务在运行${NC}"
    fi
}

# 清理临时文件
cleanup() {
    echo -e "${BLUE}[清理]${NC} 临时文件..."
    rm -f /tmp/devtoolmp-backend.pid
    rm -f /tmp/devtoolmp-frontend.pid
    echo -e "${GREEN}✓ 临时文件已清理${NC}"
}

# 主流程
main() {
    echo "请选择停止选项:"
    echo "  1) 全部停止 (Docker + 本地服务)"
    echo "  2) 只停止 Docker 服务"
    echo "  3) 只停止本地服务"
    echo "  4) 全部停止并清理数据卷"
    echo ""
    read -p "请输入选项 [1-4，默认1]: " option
    option=${option:-1}

    case $option in
        1)
            echo -e "${BLUE}[模式]${NC} 停止所有服务"
            echo ""
            stop_docker
            echo ""
            stop_backend
            stop_frontend
            cleanup
            ;;
        2)
            echo -e "${BLUE}[模式]${NC} 停止 Docker 服务"
            echo ""
            stop_docker
            ;;
        3)
            echo -e "${BLUE}[模式]${NC} 停止本地服务"
            echo ""
            stop_backend
            echo ""
            stop_frontend
            cleanup
            ;;
        4)
            echo -e "${BLUE}[模式]${NC} 停止并清理"
            echo ""
            read -p "确认删除所有数据卷? 这将删除数据库数据! [y/N]: " confirm
            if [[ "$confirm" =~ ^[Yy]$ ]]; then
                stop_docker
                docker-compose down -v
                stop_backend
                stop_frontend
                cleanup
                echo -e "${GREEN}✓ 数据卷已清理${NC}"
            else
                echo "已取消"
                exit 0
            fi
            ;;
        *)
            echo -e "${RED}✗ 无效选项${NC}"
            exit 1
            ;;
    esac

    echo ""
    echo "================================"
    echo -e "${GREEN}✓ 停止完成${NC}"
    echo "================================"
}

main
