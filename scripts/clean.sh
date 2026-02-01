#!/bin/bash

# DevToolMP 清理脚本

echo "=== DevToolMP 清理脚本 ==="
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

# 停止所有服务
stop_all() {
    echo -e "${BLUE}[停止]${NC} 停止所有服务..."
    ./scripts/stop.sh > /dev/null 2>&1 <<EOF
1
EOF
    echo -e "${GREEN}✓ 服务已停止${NC}"
}

# 清理日志文件
clean_logs() {
    echo -e "${BLUE}[清理]${NC} 日志文件..."
    rm -f /tmp/devtoolmp-backend.log
    rm -f /tmp/devtoolmp-frontend.log
    echo -e "${GREEN}✓ 日志已清理${NC}"
}

# 清理PID文件
clean_pids() {
    echo -e "${BLUE}[清理]${NC} PID 文件..."
    rm -f /tmp/devtoolmp-backend.pid
    rm -f /tmp/devtoolmp-frontend.pid
    echo -e "${GREEN}✓ PID 文件已清理${NC}"
}

# 清理Docker容器和镜像
clean_docker() {
    echo -e "${BLUE}[清理]${NC} Docker 容器..."
    docker-compose down --rmi local 2>/dev/null || true
    echo -e "${GREEN}✓ Docker 容器已清理${NC}"
}

# 清理Docker数据卷
clean_volumes() {
    echo -e "${YELLOW}[警告]${NC} 即将删除所有数据卷，包括数据库数据!"
    read -p "确认继续? [y/N]: " confirm

    if [[ "$confirm" =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}[清理]${NC} Docker 数据卷..."
        docker-compose down -v 2>/dev/null || true
        echo -e "${GREEN}✓ 数据卷已清理${NC}"
    else
        echo "跳过数据卷清理"
    fi
}

# 清理前端构建产物
clean_frontend() {
    echo -e "${BLUE}[清理]${NC} 前端构建产物..."

    if [ -d "frontend/dist" ]; then
        rm -rf frontend/dist
        echo -e "${GREEN}✓ frontend/dist 已清理${NC}"
    fi

    if [ -d "frontend/node_modules/.vite" ]; then
        rm -rf frontend/node_modules/.vite
        echo -e "${GREEN}✓ Vite 缓存已清理${NC}"
    fi
}

# 清理后端构建产物
clean_backend() {
    echo -e "${BLUE}[清理]${NC} 后端构建产物..."

    if [ -d "backend/target" ]; then
        rm -rf backend/target
        echo -e "${GREEN}✓ backend/target 已清理${NC}"
    fi
}

# 清理所有
clean_all() {
    stop_all
    clean_logs
    clean_pids
    clean_docker
    clean_frontend
    clean_backend

    echo ""
    echo -e "${YELLOW}[提示]${NC} 如需清理数据库数据，请运行: $0 volumes"
}

# 显示菜单
show_menu() {
    echo "请选择清理选项:"
    echo "  1) 轻量清理 (日志、PID文件)"
    echo "  2) 中等清理 (包括 Docker 容器)"
    echo "  3) 完全清理 (包括数据卷 - 会删除数据库数据)"
    echo "  4) 只清理前端构建产物"
    echo "  5) 只清理后端构建产物"
    echo "  6) 清理所有 (不删除数据卷)"
    echo ""
}

# 主流程
main() {
    if [ $# -gt 0 ]; then
        case $1 in
            logs)
                clean_logs
                clean_pids
                ;;
            docker)
                stop_all
                clean_docker
                ;;
            volumes)
                stop_all
                clean_volumes
                ;;
            frontend)
                clean_frontend
                ;;
            backend)
                clean_backend
                ;;
            all)
                clean_all
                ;;
            *)
                echo "用法: $0 [logs|docker|volumes|frontend|backend|all]"
                exit 1
                ;;
        esac
    else
        show_menu
        read -p "请输入选项 [1-6]: " option

        case $option in
            1)
                clean_logs
                clean_pids
                ;;
            2)
                stop_all
                clean_logs
                clean_pids
                clean_docker
                ;;
            3)
                stop_all
                clean_logs
                clean_pids
                clean_volumes
                ;;
            4)
                clean_frontend
                ;;
            5)
                clean_backend
                ;;
            6)
                clean_all
                ;;
            *)
                echo -e "${RED}✗ 无效选项${NC}"
                exit 1
                ;;
        esac
    fi

    echo ""
    echo "================================"
    echo -e "${GREEN}✓ 清理完成${NC}"
    echo "================================"
}

main "$@"
