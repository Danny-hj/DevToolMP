#!/bin/bash

echo "=========================================="
echo "DevToolMP 快速启动脚本"
echo "=========================================="

# 检查 Java 版本
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 版本过低：当前版本 $JAVA_VERSION"
    echo ""
    echo "请先升级 Java 到 17 或更高版本："
    echo ""
    echo "使用 SDKMAN 安装（推荐）："
    echo "  1. curl -s 'https://get.sdkman.io' | bash"
    echo "  2. source ~/.sdkman/bin/sdkman-init.sh"
    echo "  3. sdk install java 17.0.9-tem"
    echo "  4. sdk use java 17.0.9-tem"
    echo ""
    echo "然后重新运行此脚本"
    exit 1
fi

echo "✅ Java 版本: $(java -version 2>&1 | head -n 1)"

# 检查 MySQL 容器
if ! docker ps | grep -q devtoolmp-mysql; then
    echo "❌ MySQL 容器未运行"
    echo ""
    echo "启动 MySQL 容器："
    echo "docker run -d \\"
    echo "  --name devtoolmp-mysql \\"
    echo "  -e MYSQL_ROOT_PASSWORD=rootpassword \\"
    echo "  -e MYSQL_DATABASE=devtoolmp \\"
    echo "  -e MYSQL_USER=devtool \\"
    echo "  -e MYSQL_PASSWORD=devtool123 \\"
    echo "  -p 3306:3306 \\"
    echo "  mysql:8.0 \\"
    echo "  --default-authentication-plugin=mysql_native_password"
    exit 1
fi

echo "✅ MySQL 容器运行中"

# 检查 Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven 未安装"
    exit 1
fi

echo "✅ Maven 已安装"

# 检查 Node.js
if ! command -v node &> /dev/null; then
    echo "❌ Node.js 未安装"
    exit 1
fi

echo "✅ Node.js 已安装: $(node -v)"

echo ""
echo "=========================================="
echo "启动后端服务..."
echo "=========================================="

cd backend
mkdir -p ../logs
nohup mvn spring-boot:run > ../logs/backend.log 2>&1 &
BACKEND_PID=$!
echo "后端 PID: $BACKEND_PID"
cd ..

echo ""
echo "=========================================="
echo "启动前端服务..."
echo "=========================================="

cd frontend
if [ ! -d "node_modules" ]; then
    echo "安装前端依赖..."
    npm config set registry https://registry.npmjs.org/
    npm install
fi

mkdir -p ../logs
nohup npm run dev > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!
echo "前端 PID: $FRONTEND_PID"
cd ..

echo ""
echo "=========================================="
echo "DevToolMP 启动成功！"
echo "=========================================="
echo ""
echo "服务地址："
echo "  前端: http://localhost:5173"
echo "  后端: http://localhost:8080/api"
echo "  数据库: localhost:3306"
echo ""
echo "日志文件："
echo "  后端: logs/backend.log"
echo "  前端: logs/frontend.log"
echo ""
echo "停止服务："
echo "  后端: kill $BACKEND_PID"
echo "  前端: kill $FRONTEND_PID"
echo ""
echo "查看日志："
echo "  tail -f logs/backend.log"
echo "  tail -f logs/frontend.log"
echo "=========================================="

# 等待用户中断
trap "echo ''; echo '停止服务...'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; echo '服务已停止'; exit 0" INT TERM

while true; do
    sleep 1
done
