#!/bin/bash

echo "🛑 Stopping PostgreSQL database..."
echo "==================================="

# Check if Docker Compose is available
if command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker-compose"
elif docker compose version &> /dev/null; then
    COMPOSE_CMD="docker compose"
else
    echo "❌ Docker Compose is not available."
    exit 1
fi

# Stop PostgreSQL
$COMPOSE_CMD stop postgres

echo "✅ PostgreSQL stopped"

