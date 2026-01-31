FROM node:20-alpine

# 安装wget用于健康检查
RUN apk add --no-cache wget

WORKDIR /app

# 复制package文件并安装依赖
COPY package*.json ./

# 使用npm ci进行更可靠的安装（保留devDependencies因为需要vite开发服务器）
RUN npm ci

# 复制源代码
COPY . .

# 暴露端口
EXPOSE 5173

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:5173 || exit 1

# 启动开发服务器，监听所有地址以允许外部访问
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
