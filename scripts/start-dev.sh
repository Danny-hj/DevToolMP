#!/bin/bash

echo "Starting DevToolMP development environment..."

echo "Starting MySQL..."
docker-compose up -d mysql

echo "Waiting for MySQL to be ready..."
sleep 15

echo "Starting Backend..."
cd backend
./mvnw spring-boot:run &
BACKEND_PID=$!

echo "Starting Frontend..."
cd ../frontend
npm install
npm run dev &
FRONTEND_PID=$!

echo ""
echo "=========================================="
echo "DevToolMP development environment started!"
echo "=========================================="
echo "Backend: http://localhost:8080/api"
echo "Frontend: http://localhost:5173"
echo "MySQL: localhost:3306"
echo ""
echo "Press Ctrl+C to stop all services"
echo "=========================================="

trap "kill $BACKEND_PID $FRONTEND_PID; docker-compose down" EXIT

wait
