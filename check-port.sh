#!/bin/bash

# Script to check what's using a specific port

PORT=${1:-5443}

echo "Checking what's using port $PORT..."
echo ""

# Check with lsof
if command -v lsof &> /dev/null; then
    echo "Using lsof:"
    sudo lsof -i :$PORT
    echo ""
fi

# Check with netstat
if command -v netstat &> /dev/null; then
    echo "Using netstat:"
    sudo netstat -tulpn | grep :$PORT
    echo ""
fi

# Check with ss
if command -v ss &> /dev/null; then
    echo "Using ss:"
    sudo ss -tulpn | grep :$PORT
    echo ""
fi

# Check Docker containers
echo "Checking Docker containers:"
docker ps --format "table {{.Names}}\t{{.Ports}}" | grep $PORT || echo "No Docker containers using port $PORT"
echo ""

echo "To free up the port, you can:"
echo "1. Stop the service using it"
echo "2. Change the port in compose.yaml to a different port (e.g., 5443, 5444, etc.)"
echo "3. Update application.properties with the new port"

