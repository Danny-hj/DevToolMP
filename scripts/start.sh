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
NC='\033[0m' # No Color

# 检查 Docker 是否运行
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        echo -e "${RED}错误: Docker 未运行，请先启动 Docker Desktop${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Docker 运行正常${NC}"
}

# 检查必要的命令
check_commands() {
    local missing=0

    if ! command -v docker &> /dev/null; then
        echo -e "${RED}错误: 未找到 docker 命令${NC}"
        missing=1
    fi

    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        echo -e "${RED}错误: 未找到 docker-compose 命令${NC}"
        missing=1
    fi

    if [ $missing -eq 1 ]; then
        exit 1
    fi

    echo -e "${GREEN}✓ Docker 命令检查通过${NC}"
}

# 选择启动模式
select_mode() {
    echo ""
    echo "请选择启动模式:"
    echo "  1) 混合模式 (Docker MySQL/Redis + 本地后端/前端) - 推荐"
    echo "  2) 完全 Docker 模式 (所有服务在 Docker 中)"
    echo "  3) 仅基础设施 (只启动 MySQL 和 Redis)"
    echo ""
    read -p "请输入选项 [1-3]: " mode

    case $mode in
        1|2|3)
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
    echo -e "${YELLOW}启动 MySQL 和 Redis...${NC}"
    docker-compose up -d mysql redis

    echo "等待服务就绪..."
    sleep 5

    # 等待 MySQL 健康
    echo "等待 MySQL 健康检查..."
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

    if [ $retries -eq $max_retries ]; then
        echo -e "${RED}MySQL 启动超时${NC}"
        return 1
    fi

    echo -e "${GREEN}✓ Redis 已就绪${NC}"
    return 0
}

# 启动本地后端
start_backend_local() {
    echo ""
    echo -e "${YELLOW}检查本地后端环境...${NC}"

    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}错误: 未找到 Maven${NC}"
        return 1
    fi

    if ! command -v java &> /dev/null; then
        echo -e "${RED}错误: 未找到 Java${NC}"
        return 1
    fi

    echo -e "${GREEN}✓ 本地环境检查通过${NC}"
    echo "启动后端服务 (端口 8080)..."

    cd backend
    mvn spring-boot:run -Dspring-boot.run.profiles=dev > /tmp/devtoolmp-backend.log 2>&1 &
    BACKEND_PID=$!
    echo $BACKEND_PID > /tmp/devtoolmp-backend.pid
    cd ..

    echo "等待后端启动..."
    sleep 15

    if curl -s http://localhost:8080/api/tools > /dev/null 2>&1; then
        echo -e "${GREEN}✓ 后端启动成功 (PID: $BACKEND_PID)${NC}"
        return 0
    else
        echo -e "${RED}✗ 后端启动失败，查看日志: tail -f /tmp/devtoolmp-backend.log${NC}"
        return 1
    fi
}

# 启动本地前端
start_frontend_local() {
    echo ""
    echo -e "${YELLOW}检查本地前端环境...${NC}"

    if ! command -v npm &> /dev/null; then
        echo -e "${RED}错误: 未找到 npm${NC}"
        return 1
    fi

    echo -e "${GREEN}✓ 本地环境检查通过${NC}"
    echo "启动前端服务 (端口 5173)..."

    cd frontend
    npm run dev > /tmp/devtoolmp-frontend.log 2>&1 &
    FRONTEND_PID=$!
    echo $FRONTEND_PID > /tmp/devtoolmp-frontend.pid
    cd ..

    echo "等待前端启动..."
    sleep 10

    if curl -s http://localhost:5173 > /dev/null 2>&1; then
        echo -e "${GREEN}✓ 前端启动成功 (PID: $FRONTEND_PID)${NC}"
        return 0
    else
        echo -e "${RED}✗ 前端启动失败，查看日志: tail -f /tmp/devtoolmp-frontend.log${NC}"
        return 1
    fi
}

# 启动 Docker 容器
start_docker_services() {
    echo ""
    echo -e "${YELLOW}启动所有 Docker 服务...${NC}"
    docker-compose up -d --build

    echo "等待服务启动..."
    sleep 30

    echo ""
    echo -e "${GREEN}服务状态:${NC}"
    docker-compose ps
}

# 显示服务信息
show_info() {
    echo ""
    echo "=== 服务信息 ==="
    echo "前端: http://localhost:5173"
    echo "后端 API: http://localhost:8080/api"
    echo ""
    echo "测试命令:"
    echo "  curl http://localhost:8080/api/tools"
    echo "  curl http://localhost:8080/api/tools/ranking/daily"
    echo ""
    echo "停止服务:"
    echo "  docker-compose down              # 停止 Docker 服务"
    echo "  kill \$(cat /tmp/devtoolmp-backend.pid)      # 停止本地后端"
    echo "  kill \$(cat /tmp/devtoolmp-frontend.pid)     # 停止本地前端"
    echo ""
    echo "查看日志:"
    echo "  docker-compose logs -f           # Docker 日志"
    echo "  tail -f /tmp/devtoolmp-backend.log   # 后端日志"
    echo "  tail -f /tmp/devtoolmp-frontend.log  # 前端日志"
    echo ""
}

# 主流程
main() {
    check_docker
    check_commands
    select_mode

    case $mode in
        1)
            # 混合模式
            start_infrastructure
            start_backend_loc
            al
            start_frontend_local
            ;;
        2)
            # 完全 Docker 模式
            start_docker_services
            ;;
        3)
            # 仅基础设施
            start_infrastructure
            ;;
    esac

    show_info

    echo -e "${GREEN}=== 启动完成 ===${NC}"
}

# 运行主流程
main
