#!/bin/bash

# DevToolMP 重启脚本

echo "=== DevToolMP 重启脚本 ==="
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

# 主流程
main() {
    echo "请选择重启选项:"
    echo "  1) 重启所有服务"
    echo "  2) 只重启后端"
    echo "  3) 只重启前端"
    echo "  4) 重启 Docker 服务"
    echo ""
    read -p "请输入选项 [1-4]: " option

    case $option in
        1)
            echo -e "${BLUE}[重启]${NC} 所有服务"
            echo ""
            ./scripts/stop.sh <<EOF
1
EOF
            echo ""
            echo -e "${YELLOW}[等待]${NC} 等待服务完全停止..."
            sleep 3
            echo ""
            ./scripts/start.sh
            ;;
        2)
            echo -e "${BLUE}[重启]${NC} 后端服务"

            # 停止后端
            if [ -f /tmp/devtoolmp-backend.pid ]; then
                PID=$(cat /tmp/devtoolmp-backend.pid)
                kill $PID 2>/dev/null || true
                rm -f /tmp/devtoolmp-backend.pid
                echo -e "${YELLOW}[等待]${NC} 等待后端停止..."
                sleep 3
            fi

            # 启动后端
            echo ""
            ./scripts/start.sh <<EOF
3
EOF
            ;;
        3)
            echo -e "${BLUE}[重启]${NC} 前端服务"

            # 停止前端
            if [ -f /tmp/devtoolmp-frontend.pid ]; then
                PID=$(cat /tmp/devtoolmp-frontend.pid)
                kill $PID 2>/dev/null || true
                rm -f /tmp/devtoolmp-frontend.pid
                echo -e "${YELLOW}[等待]${NC} 等待前端停止..."
                sleep 2
            fi

            # 启动前端
            echo ""
            ./scripts/start.sh <<EOF
3
EOF
            ;;
        4)
            echo -e "${BLUE}[重启]${NC} Docker 服务"
            docker-compose restart
            echo -e "${GREEN}✓ Docker 服务已重启${NC}"
            ;;
        *)
            echo -e "${RED}✗ 无效选项${NC}"
            exit 1
            ;;
    esac
}

main
