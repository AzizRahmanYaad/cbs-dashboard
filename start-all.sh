#!/bin/bash

echo "🚀 Starting CBS Dashboard - Full Stack Application"
echo "=================================================="

# Start PostgreSQL database first
echo "🐘 Starting PostgreSQL database..."
if [ -f "./start-database.sh" ]; then
    chmod +x ./start-database.sh
    ./start-database.sh
    if [ $? -ne 0 ]; then
        echo "❌ Failed to start database. Exiting."
        exit 1
    fi
    echo ""
else
    echo "⚠️  start-database.sh not found. Make sure PostgreSQL is running manually."
    echo ""
fi

# Start Spring Boot backend on port 8080 in background
echo "📦 Starting Spring Boot backend on port 8080..."
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun > /tmp/backend.log 2>&1 &
BACKEND_PID=$!
echo "   Backend PID: $BACKEND_PID"

# Wait for backend to initialize
echo "⏳ Waiting for backend to start (15 seconds)..."
sleep 15

# Check if backend is running
if kill -0 $BACKEND_PID 2>/dev/null; then
    echo "✅ Backend started successfully"
else
    echo "❌ Backend failed to start. Check /tmp/backend.log"
    exit 1
fi

# Start Angular frontend on port 5000 (this will be the main webview)
echo "🎨 Starting Angular frontend on port 5000..."
cd frontend
echo "N" | npm start

# If frontend exits, kill backend
kill $BACKEND_PID 2>/dev/null
