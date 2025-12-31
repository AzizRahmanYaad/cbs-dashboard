#!/bin/bash

echo "🐘 Starting PostgreSQL database..."
echo "===================================="

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is available
if command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker-compose"
elif docker compose version &> /dev/null; then
    COMPOSE_CMD="docker compose"
else
    echo "❌ Docker Compose is not available. Please install Docker Compose."
    exit 1
fi

# Check if PostgreSQL container is already running
if docker ps | grep -q cbs-dashboard-postgres; then
    echo "✅ PostgreSQL is already running"
    exit 0
fi

# Start PostgreSQL using Docker Compose
echo "📦 Starting PostgreSQL container..."
$COMPOSE_CMD up -d postgres

# Wait for PostgreSQL to be ready
echo "⏳ Waiting for PostgreSQL to be ready..."
max_attempts=30
attempt=0

while [ $attempt -lt $max_attempts ]; do
    if docker exec cbs-dashboard-postgres pg_isready -U cbs_user -d cbs_dashboard &> /dev/null; then
        echo "✅ PostgreSQL is ready!"
        echo ""
        echo "Database connection details:"
        echo "  Host: localhost"
        echo "  Port: 5432"
        echo "  Database: cbs_dashboard"
        echo "  Username: cbs_user"
        echo "  Password: admin123"
        exit 0
    fi
    attempt=$((attempt + 1))
    sleep 1
    echo -n "."
done

echo ""
echo "❌ PostgreSQL failed to start within $max_attempts seconds"
echo "Check logs with: docker logs cbs-dashboard-postgres"
exit 1

