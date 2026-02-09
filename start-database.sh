#!/bin/bash

# Wrapper script that uses docker-automation.sh
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Use the docker-automation.sh script
if [ -f "./docker-automation.sh" ]; then
    chmod +x ./docker-automation.sh
    ./docker-automation.sh start
else
    echo "❌ docker-automation.sh not found. Please ensure it exists in the project root."
    exit 1
fi

