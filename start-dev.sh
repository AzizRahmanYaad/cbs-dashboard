#!/bin/bash

# Start backend on port 8090 with dev profile
echo "Starting Spring Boot backend on port 8090..."
./gradlew bootRun --args='--spring.profiles.active=dev' &
BACKEND_PID=$!

# Wait for backend to start
echo "Waiting for backend to start..."
sleep 12

# Start Angular frontend on port 5000
echo "Starting Angular frontend on port 5000..."
cd frontend
npm start &
FRONTEND_PID=$!

# Wait for both processes
wait $BACKEND_PID $FRONTEND_PID
