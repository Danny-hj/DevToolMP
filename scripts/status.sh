#!/bin/bash

# DevToolMP 服务状态查看脚本

echo "=== DevToolMP 服务状态 ==="
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

# 检查Docker服务
check_docker_services() {
    echo -e "${BLUE}[Docker 服务]${NC}"
    echo ""

    if ! docker info > /dev/null 2>&1; then
        echo -e "${RED}✗ Docker 未运行${NC}"
        return
    fi

    if docker-compose ps -q 2>/dev/null | grep -q .; then
        echo "服务名称              状态          端口"
        echo "────────────────────────────────────────"

        # MySQL
        if docker-compose ps mysql | grep -q "Up"; then
            if docker-compose ps mysql | grep -q "healthy"; then
                echo -e "MySQL               ${GREEN}✓ 健康${NC}        3306"
            else
                echo -e "MySQL               ${YELLOW}⚠ 启动中${NC}        3306"
            fi
        else
            echo -e "MySQL               ${RED}✗ 停止${NC}        3306"
        fi

        # Redis
        if docker-compose ps redis | grep -q "Up"; then
            if docker-compose ps redis | grep -q "healthy"; then
                echo -e "Redis               ${GREEN}✓ 健康${NC}        6379"
            else
                echo -e "Redis               ${YELLOW}⚠ 启动中${NC}        6379"
            fi
        else
            echo -e "Redis               ${RED}✗ 停止${NC}        6379"
        fi

        # Backend
        if docker-compose ps backend 2>/dev/null | grep -q "Up"; then
            echo -e "Backend             ${GREEN}✓ 运行中${NC}        8080"
        else
            echo -e "Backend             ${RED}✗ 停止${NC}        8080"
        fi

        # Frontend
        if docker-compose ps frontend 2>/dev/null | grep -q "Up"; then
            echo -e "Frontend            ${GREEN}✓ 运行中${NC}        5173"
        else
            echo -e "Frontend            ${RED}✗ 停止${NC}        5173"
        fi
    else
        echo -e "${YELLOW}⚠ 没有 Docker 服务运行${NC}"
    fi

    echo ""
}

# 检查本地后端
check_local_backend() {
    echo -e "${BLUE}[本地后端]${NC}"
    echo ""

    if [ -f /tmp/devtoolmp-backend.pid ]; then
        PID=$(cat /tmp/devtoolmp-backend.pid)
        if ps -p $PID > /dev/null 2>&1; then
            # 测试API
            if curl -s http://localhost:8080/api/tools > /dev/null 2>&1; then
                echo -e "状态: ${GREEN}✓ 运行中${NC}"
                echo "PID:  $PID"
                echo "端口: 8080"
                echo "API:  响应正常"
            else
                echo -e "状态: ${YELLOW}⚠ 启动中${NC}"
                echo "PID:  $PID"
                echo "端口: 8080"
                echo "API:  未响应"
            fi
        else
            echo -e "状态: ${RED}✗ 已停止${NC} (PID 文件存在但进程未运行)"
            rm -f /tmp/devtoolmp-backend.pid
        fi
    else
        # 检查端口
        if lsof -ti:8080 > /dev/null 2>&1; then
            PID=$(lsof -ti:8080)
            echo -e "状态: ${YELLOW}⚠ 运行中但无PID文件${NC}"
            echo "PID:  $PID"
            echo "端口: 8080"
        else
            echo -e "状态: ${RED}✗ 未运行${NC}"
        fi
    fi

    echo ""
}

# 检查本地前端
check_local_frontend() {
    echo -e "${BLUE}[本地前端]${NC}"
    echo ""

    if [ -f /tmp/devtoolmp-frontend.pid ]; then
        PID=$(cat /tmp/devtoolmp-frontend.pid)
        if ps -p $PID > /dev/null 2>&1; then
            # 测试前端
            if curl -s http://localhost:5173 > /dev/null 2>&1; then
                echo -e "状态: ${GREEN}✓ 运行中${NC}"
                echo "PID:  $PID"
                echo "端口: 5173"
                echo "页面: 响应正常"
            else
                echo -e "状态: ${YELLOW}⚠ 启动中${NC}"
                echo "PID:  $PID"
                echo "端口: 5173"
                echo "页面: 未响应"
            fi
        else
            echo -e "状态: ${RED}✗ 已停止${NC} (PID 文件存在但进程未运行)"
            rm -f /tmp/devtoolmp-frontend.pid
        fi
    else
        # 检查端口
        if lsof -ti:5173 > /dev/null 2>&1; then
            PID=$(lsof -ti:5173)
            echo -e "状态: ${YELLOW}⚠ 运行中但无PID文件${NC}"
            echo "PID:  $PID"
            echo "端口: 5173"
        else
            echo -e "状态: ${RED}✗ 未运行${NC}"
        fi
    fi

    echo ""
}

# 检查端口占用
check_port_conflicts() {
    echo -e "${BLUE}[端口占用]${NC}"
    echo ""

    local ports=(3306 6379 8080 5173)
    local has_conflict=false

    for port in "${ports[@]}"; do
        PID=$(lsof -ti:$port 2>/dev/null || true)
        if [ -n "$PID" ]; then
            PROCESS=$(ps -p $PID -o comm= 2>/dev/null || echo "unknown")
            echo -e "端口 $port: ${YELLOW}被占用${NC} (PID: $PID, $PROCESS)"
            has_conflict=true
        fi
    done

    if ! $has_conflict; then
        echo -e "${GREEN}✓ 所有端口可用${NC}"
    fi

    echo ""
}

# 快速测试
quick_test() {
    echo -e "${BLUE}[快速测试]${NC}"
    echo ""

    echo "测试后端 API..."
    if curl -s http://localhost:8080/api/tools > /dev/null 2>&1; then
        TOOL_COUNT=$(curl -s http://localhost:8080/api/tools | jq -r '.total // 0' 2>/dev/null || echo "N/A")
        echo -e "后端: ${GREEN}✓ 正常${NC} (工具数: $TOOL_COUNT)"
    else
        echo -e "后端: ${RED}✗ 无法访问${NC}"
    fi

    echo "测试前端页面..."
    if curl -s http://localhost:5173 > /dev/null 2>&1; then
        echo -e "前端: ${GREEN}✓ 正常${NC}"
    else
        echo -e "前端: ${RED}✗ 无法访问${NC}"
    fi

    echo ""
}

# 主流程
main() {
    check_docker_services
    check_local_backend
    check_local_frontend
    check_port_conflicts
    quick_test

    echo "================================"
    echo "提示:"
    echo "  启动服务: ./scripts/start.sh"
    echo "  停止服务: ./scripts/stop.sh"
    echo "  查看日志: ./scripts/logs.sh"
    echo "================================"
}

main
