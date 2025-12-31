#!/bin/bash

# Main startup script - Automatically starts everything
# This is the primary script to run the entire application

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "🚀 CBS Dashboard - Automated Startup"
echo "====================================="
echo ""

# Make all scripts executable
chmod +x docker-automation.sh start-database.sh stop-database.sh start-dev.sh start-all.sh 2>/dev/null

# Step 1: Start PostgreSQL automatically
echo "📦 Step 1: Starting PostgreSQL database..."
if [ -f "./docker-update.sh" ]; then
    chmod +x ./docker-update.sh
    ./docker-update.sh start-db
    if [ $? -ne 0 ]; then
        echo "❌ Failed to start database. Please check Docker installation."
        exit 1
    fi
elif [ -f "./docker-automation.sh" ]; then
    chmod +x ./docker-automation.sh
    ./docker-automation.sh start
    if [ $? -ne 0 ]; then
        echo "❌ Failed to start database. Please check Docker installation."
        exit 1
    fi
else
    echo "⚠️  Database automation scripts not found. Skipping database startup."
    echo "   Make sure PostgreSQL is running manually on localhost:5442"
fi

echo ""
echo "📦 Step 2: Starting application..."
echo ""

# Step 2: Start the application
# Use start-dev.sh by default, but allow override
if [ "${1}" = "all" ]; then
    ./start-all.sh
else
    ./start-dev.sh
fi

