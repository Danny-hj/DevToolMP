#!/bin/bash

echo "=========================================="
echo "DevToolMP 本地启动脚本"
echo "=========================================="

echo ""
echo "检查必要的环境..."

# 检查 Java
if ! command -v java &> /dev/null; then
    echo "❌ Java 未安装"
    echo "请安装 Java 17 或更高版本"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 版本过低：当前版本 $JAVA_VERSION"
    echo "请安装 Java 17 或更高版本"
    echo ""
    echo "推荐使用 SDKMAN 安装："
    echo "curl -s 'https://get.sdkman.io' | bash"
    echo "sdk install java 17.0.9-tem"
    exit 1
fi
echo "✅ Java 版本: $(java -version 2>&1 | head -n 1)"

# 检查 Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven 未安装"
    echo "请安装 Maven 3.6+"
    exit 1
fi
echo "✅ Maven 版本: $(mvn -version | head -n 1)"

# 检查 Node.js
if ! command -v node &> /dev/null; then
    echo "❌ Node.js 未安装"
    echo "请安装 Node.js 18 或更高版本"
    exit 1
fi
echo "✅ Node.js 版本: $(node -v)"

# 检查 MySQL
if ! command -v mysql &> /dev/null; then
    echo "⚠️  MySQL 未在系统 PATH 中找到"
    echo "请确保 MySQL 8.0+ 已安装并启动"
    echo ""
    echo "如果使用 Docker 启动 MySQL，请先运行："
    echo "docker run --name devtoolmp-mysql \\"
    echo "  -e MYSQL_ROOT_PASSWORD=rootpassword \\"
    echo "  -e MYSQL_DATABASE=devtoolmp \\"
    echo "  -e MYSQL_USER=devtool \\"
    echo "  -e MYSQL_PASSWORD=devtool123 \\"
    echo "  -p 3306:3306 \\"
    echo "  -d mysql:8.0"
    echo ""
    read -p "MySQL 是否已准备好？(y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
else
    echo "✅ MySQL 已安装"
fi

echo ""
echo "=========================================="
echo "初始化数据库..."
echo "=========================================="

# 检查 MySQL 是否可连接
if mysql -h localhost -u devtool -pdevtool123 devtoolmp -e "SELECT 1;" &> /dev/null; then
    echo "✅ 数据库连接成功"
else
    echo "❌ 数据库连接失败"
    echo "请检查 MySQL 是否运行，以及配置是否正确"
    exit 1
fi

echo ""
echo "=========================================="
echo "启动后端服务..."
echo "=========================================="

cd backend

# 检查是否有 mvnw
if [ -f "mvnw" ]; then
    echo "使用 Maven Wrapper 启动后端..."
    nohup ./mvnw spring-boot:run > ../logs/backend.log 2>&1 &
    BACKEND_PID=$!
else
    echo "使用系统 Maven 启动后端..."
    nohup mvn spring-boot:run > ../logs/backend.log 2>&1 &
    BACKEND_PID=$!
fi

echo "后端 PID: $BACKEND_PID"
echo "后端日志: logs/backend.log"

cd ..

echo ""
echo "=========================================="
echo "启动前端服务..."
echo "=========================================="

cd frontend

if [ ! -d "node_modules" ]; then
    echo "安装前端依赖..."
    npm install
fi

echo "启动前端开发服务器..."
nohup npm run dev > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!

echo "前端 PID: $FRONTEND_PID"
echo "前端日志: logs/frontend.log"

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
echo "按 Ctrl+C 停止所有服务"
echo "=========================================="

# 等待用户中断
trap "echo ''; echo '停止服务...'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; echo '服务已停止'; exit 0" INT TERM

# 保持脚本运行
while true; do
    sleep 1
done
