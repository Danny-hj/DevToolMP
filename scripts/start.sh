#!/bin/bash

# DevToolMP 项目启动脚本
# 用途: 一键启动开发环境

set -e

echo "=== DevToolMP 开发环境启动脚本 ==="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目根目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_ROOT"

# 检查 Docker 是否运行
check_docker() {
    echo -e "${BLUE}[检查]${NC} Docker 运行状态..."
    if ! docker info > /dev/null 2>&1; then
        echo -e "${RED}✗ 错误: Docker 未运行，请先启动 Docker Desktop${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Docker 运行正常${NC}"
}

# 检查必要的命令
check_commands() {
    echo -e "${BLUE}[检查]${NC} 必要命令..."
    local missing=0

    if ! command -v docker &> /dev/null; then
        echo -e "${RED}✗ 错误: 未找到 docker 命令${NC}"
        missing=1
    fi

    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        echo -e "${RED}✗ 错误: 未找到 docker-compose 命令${NC}"
        missing=1
    fi

    if [ $missing -eq 1 ]; then
        exit 1
    fi

    echo -e "${GREEN}✓ Docker 命令检查通过${NC}"
}

# 检查端口占用
check_ports() {
    echo -e "${BLUE}[检查]${NC} 端口占用情况..."
    local ports=(3306 6379 8080 5173)
    local occupied=0

    for port in "${ports[@]}"; do
        if lsof -i ":$port" > /dev/null 2>&1; then
            echo -e "${YELLOW}⚠ 警告: 端口 $port 已被占用${NC}"
            occupied=1
        fi
    done

    if [ $occupied -eq 1 ]; then
        echo -e "${YELLOW}提示: 如果启动失败，请先停止占用端口的进程${NC}"
        read -p "是否继续? [y/N]: " continue
        if [[ ! "$continue" =~ ^[Yy]$ ]]; then
            exit 0
        fi
    else
        echo -e "${GREEN}✓ 所有端口可用${NC}"
    fi
}

# 选择启动模式
select_mode() {
    echo ""
    echo "请选择启动模式:"
    echo "  1) 混合模式 (Docker MySQL/Redis + 本地后端/前端) - 推荐"
    echo "  2) 完全 Docker 模式 (所有服务在 Docker 中)"
    echo "  3) 仅基础设施 (只启动 MySQL 和 Redis)"
    echo "  4) 快速启动 (跳过检查，使用默认配置)"
    echo ""
    read -p "请输入选项 [1-4，默认1]: " mode
    mode=${mode:-1}

    case $mode in
        1|2|3|4)
            ;;
        *)
            echo -e "${RED}无效选项，使用默认模式 1${NC}"
            mode=1
            ;;
    esac
}

# 启动基础设施服务
start_infrastructure() {
    echo ""
    echo -e "${YELLOW}[启动]${NC} MySQL 和 Redis..."
    docker-compose up -d mysql redis

    echo -e "${BLUE}[等待]${NC} 服务启动中..."
    sleep 5

    # 等待 MySQL 健康
    echo -e "${BLUE}[等待]${NC} MySQL 健康检查..."
    local retries=0
    local max_retries=30

    while [ $retries -lt $max_retries ]; do
        if docker-compose ps | grep mysql | grep -q "healthy"; then
            echo -e "${GREEN}✓ MySQL 已就绪${NC}"
            break
        fi
        retries=$((retries + 1))
        echo -n "."
        sleep 2
    done
    echo ""

    if [ $retries -eq $max_retries ]; then
        echo -e "${RED}✗ MySQL 启动超时${NC}"
        echo -e "${YELLOW}提示: 查看日志: docker-compose logs mysql${NC}"
        return 1
    fi

    # 检查 Redis
    if docker-compose ps | grep redis | grep -q "healthy"; then
        echo -e "${GREEN}✓ Redis 已就绪${NC}"
    else
        echo -e "${YELLOW}⚠ Redis 健康检查未完成，但继续启动${NC}"
    fi

    return 0
}

# 启动本地后端
start_backend_local() {
    echo ""
    echo -e "${YELLOW}[启动]${NC} 本地后端服务..."
    echo -e "${BLUE}[检查]${NC} 后端环境..."

    # 检查Java
    if ! command -v java &> /dev/null; then
        echo -e "${RED}✗ 错误: 未找到 Java${NC}"
        echo -e "${YELLOW}提示: 请安装 JDK 21${NC}"
        return 1
    fi

    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}✗ 错误: 未找到 Maven${NC}"
        echo -e "${YELLOW}提示: 请安装 Maven${NC}"
        return 1
    fi

    # 检查Java版本
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 21 ]; then
        echo -e "${YELLOW}⚠ 警告: 推荐使用 JDK 21，当前版本: $JAVA_VERSION${NC}"
    fi

    echo -e "${GREEN}✓ 后端环境检查通过${NC}"

    # 检查是否已有后端进程
    if [ -f /tmp/devtoolmp-backend.pid ]; then
        OLD_PID=$(cat /tmp/devtoolmp-backend.pid)
        if ps -p $OLD_PID > /dev/null 2>&1; then
            echo -e "${YELLOW}⚠ 后端已在运行 (PID: $OLD_PID)${NC}"
            read -p "是否重启? [y/N]: " restart
            if [[ "$restart" =~ ^[Yy]$ ]]; then
                kill $OLD_PID 2>/dev/null || true
                sleep 2
            else
                return 0
            fi
        fi
    fi

    echo "启动后端服务 (端口 8080)..."

    cd backend
    nohup mvn spring-boot:run -Dspring-boot.run.profiles=dev > /tmp/devtoolmp-backend.log 2>&1 &
    BACKEND_PID=$!
    echo $BACKEND_PID > /tmp/devtoolmp-backend.pid
    cd ..

    echo -e "${BLUE}[等待]${NC} 后端启动中..."

    # 等待后端启动，最多等待60秒
    local retries=0
    local max_retries=30

    while [ $retries -lt $max_retries ]; do
        if curl -s http://localhost:8080/api/tools > /dev/null 2>&1; then
            echo -e "${GREEN}✓ 后端启动成功 (PID: $BACKEND_PID)${NC}"
            return 0
        fi
        retries=$((retries + 1))
        echo -n "."
        sleep 2
    done
    echo ""

    # 检查进程是否还在运行
    if ps -p $BACKEND_PID > /dev/null 2>&1; then
        echo -e "${YELLOW}⚠ 后端进程运行中，但API未响应${NC}"
        echo -e "${YELLOW}提示: 查看日志: tail -f /tmp/devtoolmp-backend.log${NC}"
        return 0
    else
        echo -e "${RED}✗ 后端启动失败${NC}"
        echo -e "${YELLOW}提示: 查看日志: tail -f /tmp/devtoolmp-backend.log${NC}"
        rm -f /tmp/devtoolmp-backend.pid
        return 1
    fi
}

# 启动本地前端
start_frontend_local() {
    echo ""
    echo -e "${YELLOW}[启动]${NC} 本地前端服务..."
    echo -e "${BLUE}[检查]${NC} 前端环境..."

    # 检查npm
    if ! command -v npm &> /dev/null; then
        echo -e "${RED}✗ 错误: 未找到 npm${NC}"
        echo -e "${YELLOW}提示: 请安装 Node.js 18+${NC}"
        return 1
    fi

    echo -e "${GREEN}✓ 前端环境检查通过${NC}"

    # 检查是否已有前端进程
    if [ -f /tmp/devtoolmp-frontend.pid ]; then
        OLD_PID=$(cat /tmp/devtoolmp-frontend.pid)
        if ps -p $OLD_PID > /dev/null 2>&1; then
            echo -e "${YELLOW}⚠ 前端已在运行 (PID: $OLD_PID)${NC}"
            read -p "是否重启? [y/N]: " restart
            if [[ "$restart" =~ ^[Yy]$ ]]; then
                kill $OLD_PID 2>/dev/null || true
                sleep 2
            else
                return 0
            fi
        fi
    fi

    # 检查node_modules
    if [ ! -d "frontend/node_modules" ]; then
        echo -e "${YELLOW}[安装]${NC} 前端依赖..."
        cd frontend
        npm install
        cd ..
    fi

    echo "启动前端服务 (端口 5173)..."

    cd frontend
    nohup npm run dev > /tmp/devtoolmp-frontend.log 2>&1 &
    FRONTEND_PID=$!
    echo $FRONTEND_PID > /tmp/devtoolmp-frontend.pid
    cd ..

    echo -e "${BLUE}[等待]${NC} 前端启动中..."

    # 等待前端启动，最多等待30秒
    local retries=0
    local max_retries=15

    while [ $retries -lt $max_retries ]; do
        if curl -s http://localhost:5173 > /dev/null 2>&1; then
            echo -e "${GREEN}✓ 前端启动成功 (PID: $FRONTEND_PID)${NC}"
            return 0
        fi
        retries=$((retries + 1))
        echo -n "."
        sleep 2
    done
    echo ""

    # 检查进程是否还在运行
    if ps -p $FRONTEND_PID > /dev/null 2>&1; then
        echo -e "${YELLOW}⚠ 前端进程运行中，但可能未完全就绪${NC}"
        echo -e "${YELLOW}提示: 查看日志: tail -f /tmp/devtoolmp-frontend.log${NC}"
        return 0
    else
        echo -e "${RED}✗ 前端启动失败${NC}"
        echo -e "${YELLOW}提示: 查看日志: tail -f /tmp/devtoolmp-frontend.log${NC}"
        rm -f /tmp/devtoolmp-frontend.pid
        return 1
    fi
}

# 启动 Docker 容器
start_docker_services() {
    echo ""
    echo -e "${YELLOW}[启动]${NC} 所有 Docker 服务..."

    # 构建镜像
    echo -e "${BLUE}[构建]${NC} Docker 镜像..."
    docker-compose build

    # 启动服务
    echo -e "${BLUE}[启动]${NC} Docker 容器..."
    docker-compose up -d

    echo -e "${BLUE}[等待]${NC} 服务启动中..."
    sleep 30

    # 检查服务状态
    echo ""
    echo -e "${BLUE}[状态]${NC} Docker 服务:"
    docker-compose ps

    # 检查健康状态
    echo ""
    echo -e "${BLUE}[检查]${NC} 服务健康状态:"
    if docker-compose ps | grep -q "unhealthy"; then
        echo -e "${YELLOW}⚠ 部分服务不健康${NC}"
        echo -e "${YELLOW}提示: 使用 'docker-compose logs' 查看日志${NC}"
    else
        echo -e "${GREEN}✓ 所有服务正常${NC}"
    fi
}

# 显示服务信息
show_info() {
    echo ""
    echo "================================"
    echo -e "${GREEN}✓ 服务启动完成${NC}"
    echo "================================"
    echo ""
    echo "📱 访问地址:"
    echo "  前端页面:   http://localhost:5173"
    echo "  后端 API:   http://localhost:8080/api"
    echo ""
    echo "🔍 测试命令:"
    echo "  curl http://localhost:8080/api/tools"
    echo "  curl http://localhost:8080/api/tools/ranking/daily"
    echo ""
    echo "🛑 停止服务:"
    echo "  ./scripts/stop.sh              # 交互式停止"
    echo "  docker-compose down            # 停止 Docker 服务"
    echo ""
    echo "📋 查看日志:"
    echo "  docker-compose logs -f backend    # 后端日志"
    echo "  docker-compose logs -f frontend   # 前端日志"
    echo "  tail -f /tmp/devtoolmp-backend.log   # 本地后端日志"
    echo "  tail -f /tmp/devtoolmp-frontend.log  # 本地前端日志"
    echo ""
    echo "📊 查看状态:"
    echo "  ./scripts/status.sh           # 查看所有服务状态"
    echo "  docker-compose ps              # Docker 服务状态"
    echo ""
}

# 主流程
main() {
    # 快速启动模式跳过检查
    if [ "$mode" != "4" ]; then
        check_docker
        check_commands
        check_ports
    fi

    case $mode in
        1)
            # 混合模式
            echo -e "${BLUE}[模式]${NC} 混合模式 (Docker 基础设施 + 本地应用)"
            start_infrastructure
            start_backend_local
            start_frontend_local
            ;;
        2)
            # 完全 Docker 模式
            echo -e "${BLUE}[模式]${NC} 完全 Docker 模式"
            start_docker_services
            ;;
        3)
            # 仅基础设施
            echo -e "${BLUE}[模式]${NC} 仅基础设施"
            start_infrastructure
            ;;
        4)
            # 快速启动
            echo -e "${BLUE}[模式]${NC} 快速启动 (混合模式)"
            start_infrastructure
            start_backend_local
            start_frontend_local
            ;;
    esac

    show_info
}

# 运行主流程
main
