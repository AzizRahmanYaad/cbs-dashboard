#!/bin/bash

# Script to find an available port for PostgreSQL

echo "Finding an available port for PostgreSQL..."
echo ""

# Start checking from 5443
START_PORT=5443
MAX_PORT=5500
FOUND_PORT=""

for port in $(seq $START_PORT $MAX_PORT); do
    # Check if port is in use
    if ! (lsof -i :$port &> /dev/null || netstat -tuln 2>/dev/null | grep -q ":$port " || ss -tuln 2>/dev/null | grep -q ":$port "); then
        FOUND_PORT=$port
        break
    fi
done

if [ -z "$FOUND_PORT" ]; then
    echo "❌ No available port found between $START_PORT and $MAX_PORT"
    echo "Please manually specify a port or free up a port."
    exit 1
fi

echo "✅ Available port found: $FOUND_PORT"
echo ""
echo "To use this port, update:"
echo "1. compose.yaml: Change '5443:5432' to '${FOUND_PORT}:5432'"
echo "2. application.properties: Change 'localhost:5443' to 'localhost:${FOUND_PORT}'"
echo ""
echo "Or run this command to update automatically:"
echo "  sed -i 's/5443:5432/${FOUND_PORT}:5432/g' compose.yaml"
echo "  sed -i 's/localhost:5443/localhost:${FOUND_PORT}/g' src/main/resources/application.properties"

