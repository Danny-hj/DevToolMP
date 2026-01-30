#!/bin/bash

echo "Initializing database..."

echo "Waiting for MySQL to be ready..."
until docker exec devtoolmp-mysql mysqladmin ping -h localhost --silent; do
    echo "Waiting for MySQL..."
    sleep 2
done

echo "MySQL is ready!"

echo "Running schema.sql..."
docker exec -i devtoolmp-mysql mysql -u devtool -pdevtool123 devtoolmp < backend/src/main/resources/schema.sql

echo "Running data.sql..."
docker exec -i devtoolmp-mysql mysql -u devtool -pdevtool123 devtoolmp < backend/src/main/resources/data.sql

echo "Database initialized successfully!"
