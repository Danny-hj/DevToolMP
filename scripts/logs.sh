#!/bin/bash

# DevToolMP 日志查看脚本

echo "=== DevToolMP 日志查看 ==="
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

# 显示菜单
show_menu() {
    echo "请选择要查看的日志:"
    echo "  1) Docker 所有服务日志 (实时)"
    echo "  2) Docker Backend 日志 (实时)"
    echo "  3) Docker Frontend 日志 (实时)"
    echo "  4) Docker MySQL 日志 (实时)"
    echo "  5) Docker Redis 日志 (实时)"
    echo "  6) 本地后端日志 (实时)"
    echo "  7) 本地前端日志 (实时)"
    echo "  8) 查看最近的错误日志"
    echo ""
    echo "提示: 使用 Ctrl+C 退出日志查看"
    echo ""
}

# 查看日志
view_logs() {
    show_menu
    read -p "请输入选项 [1-8]: " option

    case $option in
        1)
            echo -e "${BLUE}[日志]${NC} Docker 所有服务 (实时)..."
            echo ""
            docker-compose logs -f
            ;;
        2)
            echo -e "${BLUE}[日志]${NC} Docker Backend (实时)..."
            echo ""
            docker-compose logs -f backend
            ;;
        3)
            echo -e "${BLUE}[日志]${NC} Docker Frontend (实时)..."
            echo ""
            docker-compose logs -f frontend
            ;;
        4)
            echo -e "${BLUE}[日志]${NC} Docker MySQL (实时)..."
            echo ""
            docker-compose logs -f mysql
            ;;
        5)
            echo -e "${BLUE}[日志]${NC} Docker Redis (实时)..."
            echo ""
            docker-compose logs -f redis
            ;;
        6)
            if [ -f /tmp/devtoolmp-backend.log ]; then
                echo -e "${BLUE}[日志]${NC} 本地后端 (实时)..."
                echo ""
                tail -f /tmp/devtoolmp-backend.log
            else
                echo -e "${RED}✗ 日志文件不存在: /tmp/devtoolmp-backend.log${NC}"
                echo -e "${YELLOW}提示: 后端可能未启动或使用 Docker 运行${NC}"
            fi
            ;;
        7)
            if [ -f /tmp/devtoolmp-frontend.log ]; then
                echo -e "${BLUE}[日志]${NC} 本地前端 (实时)..."
                echo ""
                tail -f /tmp/devtoolmp-frontend.log
            else
                echo -e "${RED}✗ 日志文件不存在: /tmp/devtoolmp-frontend.log${NC}"
                echo -e "${YELLOW}提示: 前端可能未启动或使用 Docker 运行${NC}"
            fi
            ;;
        8)
            echo -e "${BLUE}[日志]${NC} 最近的错误日志..."
            echo ""

            echo -e "${YELLOW}--- Docker Backend 错误 ---${NC}"
            docker-compose logs --tail=50 backend 2>&1 | grep -i error || echo "无错误"

            echo ""
            echo -e "${YELLOW}--- Docker Frontend 错误 ---${NC}"
            docker-compose logs --tail=50 frontend 2>&1 | grep -i error || echo "无错误"

            echo ""
            echo -e "${YELLOW}--- 本地后端错误 ---${NC}"
            if [ -f /tmp/devtoolmp-backend.log ]; then
                tail -100 /tmp/devtoolmp-backend.log | grep -i error || echo "无错误"
            else
                echo "日志文件不存在"
            fi

            echo ""
            echo -e "${YELLOW}--- 本地前端错误 ---${NC}"
            if [ -f /tmp/devtoolmp-frontend.log ]; then
                tail -100 /tmp/devtoolmp-frontend.log | grep -i error || echo "无错误"
            else
                echo "日志文件不存在"
            fi
            ;;
        *)
            echo -e "${RED}✗ 无效选项${NC}"
            exit 1
            ;;
    esac
}

# 支持命令行参数
if [ $# -gt 0 ]; then
    case $1 in
        backend|b)
            echo -e "${BLUE}[日志]${NC} Docker Backend..."
            docker-compose logs -f backend
            ;;
        frontend|f)
            echo -e "${BLUE}[日志]${NC} Docker Frontend..."
            docker-compose logs -f frontend
            ;;
        mysql)
            echo -e "${BLUE}[日志]${NC} Docker MySQL..."
            docker-compose logs -f mysql
            ;;
        redis)
            echo -e "${BLUE}[日志]${NC} Docker Redis..."
            docker-compose logs -f redis
            ;;
        local-backend|lb)
            if [ -f /tmp/devtoolmp-backend.log ]; then
                echo -e "${BLUE}[日志]${NC} 本地后端..."
                tail -f /tmp/devtoolmp-backend.log
            else
                echo -e "${RED}✗ 日志文件不存在${NC}"
                exit 1
            fi
            ;;
        local-frontend|lf)
            if [ -f /tmp/devtoolmp-frontend.log ]; then
                echo -e "${BLUE}[日志]${NC} 本地前端..."
                tail -f /tmp/devtoolmp-frontend.log
            else
                echo -e "${RED}✗ 日志文件不存在${NC}"
                exit 1
            fi
            ;;
        error|e)
            echo -e "${BLUE}[日志]${NC} 错误日志..."
            docker-compose logs --tail=100 | grep -i error || echo "无错误"
            ;;
        all|a)
            echo -e "${BLUE}[日志]${NC} 所有服务..."
            docker-compose logs -f
            ;;
        *)
            echo "用法: $0 [backend|frontend|mysql|redis|local-backend|local-frontend|error|all]"
            exit 1
            ;;
    esac
else
    view_logs
fi
