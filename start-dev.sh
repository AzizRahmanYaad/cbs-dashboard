#!/bin/bash

# Start PostgreSQL database first (automated)
echo "🐘 Starting PostgreSQL database (automated)..."
if [ -f "./docker-update.sh" ]; then
    chmod +x ./docker-update.sh
    ./docker-update.sh start-db
    if [ $? -ne 0 ]; then
        echo "❌ Failed to start database. Exiting."
        exit 1
    fi
elif [ -f "./docker-automation.sh" ]; then
    chmod +x ./docker-automation.sh
    ./docker-automation.sh start
    if [ $? -ne 0 ]; then
        echo "❌ Failed to start database. Exiting."
        exit 1
    fi
elif [ -f "./start-database.sh" ]; then
    chmod +x ./start-database.sh
    ./start-database.sh
    if [ $? -ne 0 ]; then
        echo "❌ Failed to start database. Exiting."
        exit 1
    fi
else
    echo "⚠️  Database automation scripts not found. Make sure PostgreSQL is running manually on localhost:5442"
fi

echo ""
# Start backend on port 8090 with dev profile
echo "🚀 Starting Spring Boot backend on port 8090..."
./gradlew bootRun --args='--spring.profiles.active=dev' &
BACKEND_PID=$!

# Wait for backend to start
echo "⏳ Waiting for backend to start..."
sleep 12

# Start Angular frontend on port 5000
echo "🎨 Starting Angular frontend on port 5000..."
cd frontend
npm start &
FRONTEND_PID=$!

# Wait for both processes
wait $BACKEND_PID $FRONTEND_PID
