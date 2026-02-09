#!/bin/bash

# Update and Restart Script for CBS Dashboard
# This script stops everything, cleans up, rebuilds, and restarts all services
# Ensures all changes are applied when you run it

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Server IP - can be set via environment variable or will be auto-detected
# You can set it like: SERVER_IP=72.61.116.191 ./update.sh
SERVER_IP=${SERVER_IP:-""}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to print colored messages
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_header() {
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${CYAN}$1${NC}"
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
}

# Function to check if docker-compose is available
check_docker_compose() {
    if command -v docker-compose &> /dev/null; then
        COMPOSE_CMD="docker-compose"
        return 0
    elif docker compose version &> /dev/null 2>&1; then
        COMPOSE_CMD="docker compose"
        return 0
    else
        return 1
    fi
}

# Function to stop all running processes
stop_all_processes() {
    print_header "Stopping All Running Services"
    
    # Stop backend processes (Gradle/Spring Boot)
    print_info "Stopping backend processes..."
    pkill -f "gradlew.*bootRun" 2>/dev/null || true
    pkill -f "java.*cbs-dashboard" 2>/dev/null || true
    # Also ensure nothing is left listening on backend port 8090
    if command -v lsof &> /dev/null; then
        if lsof -ti:8090 &> /dev/null; then
            print_info "Found process listening on port 8090. Forcibly stopping it..."
            lsof -ti:8090 | xargs -r kill -9 2>/dev/null || true
        fi
    fi
    sleep 2
    
    # Stop frontend processes (npm/node)
    print_info "Stopping frontend processes..."
    pkill -f "ng serve" 2>/dev/null || true
    pkill -f "npm start" 2>/dev/null || true
    sleep 2
    
    print_success "All processes stopped"
}

# Function to stop and remove Docker containers
stop_and_clean_containers() {
    print_header "Stopping and Cleaning Docker Containers"
    
    if ! command -v docker &> /dev/null; then
        print_warning "Docker is not installed. Skipping container cleanup."
        return 0
    fi
    
    # Check for docker-compose
    if check_docker_compose; then
        print_info "Stopping docker-compose services (preserving data volumes)..."
        # Stop containers but DO NOT remove volumes to preserve data
        $COMPOSE_CMD down 2>/dev/null || true
        # DO NOT use -v flag - it removes volumes and deletes all data!
    fi
    
    # Remove specific containers mentioned by user (but NOT the database container)
    print_info "Removing cleaning-* containers..."
    docker rm -f cleaning-postgres cleaning-app cleaning-frontend 2>/dev/null || true
    
    # Remove any other cleaning containers
    print_info "Removing any remaining cleaning containers..."
    docker ps -a | grep cleaning | awk '{print $1}' | xargs docker rm -f 2>/dev/null || true
    
    # IMPORTANT: Do NOT remove the database container - it contains persistent data!
    # Only stop it if it's running, but don't remove it
    if docker ps --format '{{.Names}}' | grep -q "^cbs-dashboard-postgres$"; then
        print_info "Stopping (but NOT removing) CBS Dashboard database container to preserve data..."
        docker stop cbs-dashboard-postgres 2>/dev/null || true
    fi
    
    # Remove other CBS Dashboard containers (but NOT the database)
    print_info "Removing other CBS Dashboard containers (preserving database)..."
    docker ps -a --filter "name=cbs-dashboard" --format "{{.Names}} {{.ID}}" | grep -v "postgres" | awk '{print $2}' | xargs docker rm -f 2>/dev/null || true
    
    print_success "Container cleanup completed"
}

# Function to clean volumes if there are data format issues
clean_volumes_if_needed() {
    print_header "Checking Docker Volumes"
    
    if ! command -v docker &> /dev/null; then
        return 0
    fi
    
    # Check if volume exists and might have issues
    if docker volume ls | grep -q "cbs-dashboard_postgres_data"; then
        print_info "PostgreSQL volume exists. Checking for data format issues..."
        
        # Try to start container and check logs
        if check_docker_compose; then
            COMPOSE_FILE=""
            [ -f "compose.yaml" ] && COMPOSE_FILE="compose.yaml"
            [ -f "docker-compose.yml" ] && COMPOSE_FILE="docker-compose.yml"
            [ -f "docker-compose.yaml" ] && COMPOSE_FILE="docker-compose.yaml"
            
            if [ -n "$COMPOSE_FILE" ]; then
                # Try to start and check for errors
                $COMPOSE_CMD -f "$COMPOSE_FILE" up -d postgres 2>&1 | tee /tmp/postgres_start.log
                sleep 5
                
                # Check if container exited with error
                if docker ps -a --filter "name=cbs-dashboard-postgres" --format "{{.Status}}" | grep -q "Exited"; then
                    LOGS=$(docker logs cbs-dashboard-postgres 2>&1 | tail -20)
                    if echo "$LOGS" | grep -q "pg_ctlcluster\|data format\|upgrade"; then
                        print_error "Detected PostgreSQL data format issue."
                        print_warning "IMPORTANT: Auto-deleting data volumes is DISABLED to prevent data loss!"
                        print_info "If you need to reset the database, manually run:"
                        print_info "  docker stop cbs-dashboard-postgres"
                        print_info "  docker rm cbs-dashboard-postgres"
                        print_info "  docker volume rm cbs-dashboard_postgres_data"
                        print_info "  Then restart the application"
                        return 0
                    fi
                fi
            fi
        fi
    fi
    
    return 0
}

# Function to rebuild frontend
rebuild_frontend() {
    print_header "Rebuilding Frontend"
    
    if [ ! -d "frontend" ]; then
        print_warning "Frontend directory not found. Skipping frontend rebuild."
        return 0
    fi
    
    cd frontend
    
    print_info "Installing/updating frontend dependencies..."
    npm install
    
    print_info "Building frontend (if needed)..."
    # Only build if there's a build script, otherwise just ensure dependencies are installed
    if grep -q "\"build\"" package.json; then
        npm run build 2>/dev/null || print_warning "Build step skipped (may not be needed for dev mode)"
    fi
    
    cd ..
    print_success "Frontend dependencies updated"
}

# Function to rebuild backend
rebuild_backend() {
    print_header "Rebuilding Backend"
    
    # Prefer Maven (current CBS Dashboard backend) and fall back to Gradle only if present
    if [ -f "pom.xml" ]; then
        if [ -x "./mvnw" ]; then
            print_info "Detected Maven project. Cleaning and rebuilding backend with ./mvnw..."
            ./mvnw clean install -DskipTests || {
                print_error "Maven backend build failed. Check the output above for details."
                return 1
            }
            print_success "Backend rebuild completed (Maven)"
            return 0
        else
            print_warning "pom.xml found but ./mvnw is missing or not executable. Skipping backend rebuild."
            return 0
        fi
    fi
    
    # Legacy Gradle support (kept for compatibility if someone adds Gradle in future)
    if [ -f "build.gradle" ] || [ -f "gradlew" ]; then
        if [ -x "./gradlew" ]; then
            print_info "Detected Gradle project. Cleaning and rebuilding backend with ./gradlew..."
            ./gradlew clean build -x test 2>/dev/null || {
                print_warning "Backend Gradle build had warnings or failed, but continuing..."
            }
            print_success "Backend rebuild completed (Gradle)"
            return 0
        else
            print_warning "Gradle wrapper not executable. Skipping backend rebuild."
            return 0
        fi
    fi
    
    print_warning "No Maven (pom.xml) or Gradle (build.gradle) project found. Skipping backend rebuild."
    return 0
}

# Function to start database
start_database() {
    print_header "Starting Database"
    
    if ! command -v docker &> /dev/null; then
        # On this server the database is managed outside Docker (e.g. system MySQL/PostgreSQL),
        # so treat missing Docker as a non-fatal condition.
        print_warning "Docker is not installed. Assuming database is managed separately and skipping Docker DB start."
        return 0
    fi
    
    # Check for port conflicts
    print_info "Checking for port conflicts..."
    if lsof -i :5443 &> /dev/null || netstat -tuln 2>/dev/null | grep -q ":5443 " || ss -tuln 2>/dev/null | grep -q ":5443 "; then
        print_warning "Port 5443 is already in use. Checking what's using it..."
        docker ps --filter "publish=5443" --format "  Container: {{.Names}} | Image: {{.Image}} | Status: {{.Status}}"
    fi
    
    # Clean volumes if needed (before starting)
    clean_volumes_if_needed
    
    # Try to use docker-update.sh first
    if [ -f "./docker-update.sh" ]; then
        chmod +x ./docker-update.sh
        ./docker-update.sh start-db
        if [ $? -eq 0 ]; then
            print_success "Database started successfully"
            return 0
        fi
    fi
    
    # Try docker-automation.sh
    if [ -f "./docker-automation.sh" ]; then
        chmod +x ./docker-automation.sh
        ./docker-automation.sh start
        if [ $? -eq 0 ]; then
            print_success "Database started successfully"
            return 0
        fi
    fi
    
    # Try docker-compose
    if check_docker_compose; then
        if [ -f "compose.yaml" ] || [ -f "docker-compose.yml" ] || [ -f "docker-compose.yaml" ]; then
            COMPOSE_FILE=""
            [ -f "compose.yaml" ] && COMPOSE_FILE="compose.yaml"
            [ -f "docker-compose.yml" ] && COMPOSE_FILE="docker-compose.yml"
            [ -f "docker-compose.yaml" ] && COMPOSE_FILE="docker-compose.yaml"
            
            if [ -n "$COMPOSE_FILE" ]; then
                print_info "Starting database with docker-compose..."
                $COMPOSE_CMD -f "$COMPOSE_FILE" up -d postgres
                
                # Wait a moment for container to start
                sleep 3
                
                # Check if container is running or exited
                CONTAINER_STATUS=$(docker ps -a --filter "name=cbs-dashboard-postgres" --format "{{.Status}}" 2>/dev/null)
                if echo "$CONTAINER_STATUS" | grep -q "Exited"; then
                    print_error "Container exited. Checking logs..."
                    docker logs cbs-dashboard-postgres 2>&1 | tail -30
                    
                    # If it's a data format issue, warn user but DON'T auto-delete data
                    if docker logs cbs-dashboard-postgres 2>&1 | grep -q "pg_ctlcluster\|data format\|upgrade"; then
                        print_error "PostgreSQL data format issue detected."
                        print_warning "IMPORTANT: Auto-deleting data volumes is DISABLED to prevent data loss!"
                        print_info "If you need to reset the database, manually run:"
                        print_info "  docker stop cbs-dashboard-postgres"
                        print_info "  docker rm cbs-dashboard-postgres"
                        print_info "  docker volume rm cbs-dashboard_postgres_data"
                        print_info "  Then restart the application"
                        return 1
                    fi
                fi
                
                # Wait for database to be ready
                print_info "Waiting for database to be ready..."
                max_attempts=45
                attempt=0
                while [ $attempt -lt $max_attempts ]; do
                    if docker ps --format '{{.Names}}' | grep -q "^cbs-dashboard-postgres$"; then
                        if docker exec cbs-dashboard-postgres pg_isready -U cbs_user -d cbs_dashboard &> /dev/null 2>&1; then
                            print_success "Database is ready!"
                            return 0
                        fi
                    else
                        # Container might have exited
                        if docker ps -a --filter "name=cbs-dashboard-postgres" --format "{{.Status}}" | grep -q "Exited"; then
                            print_error "Container exited. Last 20 lines of logs:"
                            docker logs cbs-dashboard-postgres 2>&1 | tail -20
                            return 1
                        fi
                    fi
                    attempt=$((attempt + 1))
                    sleep 1
                    echo -n "."
                done
                echo ""
                print_error "Database failed to start within $max_attempts seconds"
                print_info "Container logs:"
                docker logs cbs-dashboard-postgres 2>&1 | tail -30
                return 1
            fi
        fi
    fi
    
    print_warning "Could not start database automatically. Please start it manually."
    return 1
}

# Function to get server IP address
get_server_ip() {
    # Try to get the IP from various sources
    if [ -n "$SERVER_IP" ]; then
        echo "$SERVER_IP"
    elif command -v hostname &> /dev/null; then
        # Try to get IP from hostname -I
        IP=$(hostname -I 2>/dev/null | awk '{print $1}')
        if [ -n "$IP" ]; then
            echo "$IP"
        else
            # Fallback to getting IP from network interfaces
            ip route get 8.8.8.8 2>/dev/null | awk '{print $7; exit}' || echo "localhost"
        fi
    else
        # Last resort: try to get from ifconfig or ip command
        ip addr show 2>/dev/null | grep -oP 'inet \K[\d.]+' | grep -v '127.0.0.1' | head -1 || echo "localhost"
    fi
}

# Function to start backend
start_backend() {
    print_header "Starting Backend"
    
    # Prefer Maven-based startup if present, fall back to Gradle jar if present
    if [ -f "pom.xml" ] && [ -x "./mvnw" ]; then
        print_info "Starting Spring Boot backend with Maven on port 8090 (accessible on all interfaces)..."
        SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run > /tmp/backend.log 2>&1 &
    elif [ -x "./gradlew" ]; then
        print_info "Starting Spring Boot backend using built jar on port 8090 (accessible on all interfaces)..."
        chmod +x ./gradlew

        JAR_PATH="build/libs/CBS-Dashboard-0.0.1-SNAPSHOT.jar"

        # Build the jar if it doesn't exist yet
        if [ ! -f "$JAR_PATH" ]; then
            print_info "Backend jar not found. Building jar with Gradle (bootJar)..."
            ./gradlew clean bootJar -x test >> /tmp/backend.log 2>&1 || {
                print_error "Gradle bootJar build failed. See /tmp/backend.log for details."
                return 1
            }
        fi

        if [ ! -f "$JAR_PATH" ]; then
            print_error "Backend jar still not found at $JAR_PATH after build. Cannot start backend."
            return 1
        fi

        # Run the jar directly with java -jar to avoid Gradle bootRun spawn issues
        SPRING_PROFILES_ACTIVE=dev java -jar "$JAR_PATH" > /tmp/backend.log 2>&1 &
    else
        print_warning "No Maven wrapper (mvnw) or Gradle wrapper (gradlew) found. Skipping backend start."
        return 0
    fi
    
    BACKEND_PID=$!
    echo $BACKEND_PID > /tmp/backend.pid
    
    print_info "Backend PID: $BACKEND_PID"
    
    # Wait for backend to initialize
    print_info "Waiting for backend to start (15 seconds)..."
    sleep 15
    
    # Check if backend is still running
    if kill -0 $BACKEND_PID 2>/dev/null; then
        # Wait a bit more and check for actual errors (not warnings)
        sleep 3
        
        # First check if application started successfully
        if grep -qi "Started CbsDashboardApplication\|Tomcat started on port" /tmp/backend.log 2>/dev/null; then
            # Application started - check for critical errors only (exclude warnings and class names)
            CRITICAL_ERRORS=$(grep -iE "(BUILD FAILED|Application run failed|Unable to start|Failed to start|Connection refused|Cannot connect|Caused by:)" /tmp/backend.log 2>/dev/null | grep -vE "WARN|INFO|SqlExceptionHelper|ExceptionHelper" | wc -l)
            if [ "$CRITICAL_ERRORS" -gt 0 ]; then
                print_error "Backend started but has critical errors!"
                print_info "Recent critical errors from backend log:"
                tail -50 /tmp/backend.log | grep -iE "(BUILD FAILED|Application run failed|Unable to start|Failed to start|Connection refused|Cannot connect|Caused by:)" | grep -vE "WARN|INFO|SqlExceptionHelper" | tail -8
                print_info "Full log: /tmp/backend.log"
            else
                # Check if port is listening
                if netstat -tuln 2>/dev/null | grep -q ":8090" || ss -tuln 2>/dev/null | grep -q ":8090"; then
                    print_success "Backend started successfully on port 8090"
                else
                    print_warning "Backend process running but port 8090 not listening yet"
                fi
            fi
        else
            # Application didn't start - check for actual errors
            if grep -qiE "(BUILD FAILED|Caused by:|Unable to|Failed to start)" /tmp/backend.log 2>/dev/null | grep -vE "WARN|INFO|SqlExceptionHelper" > /dev/null; then
                print_error "Backend failed to start!"
                print_info "Last 40 lines of backend log:"
                tail -40 /tmp/backend.log | grep -iE "(BUILD FAILED|Caused by:|Unable to|Failed to start)" | grep -vE "WARN|INFO|SqlExceptionHelper" | tail -10 || tail -20 /tmp/backend.log
            else
                print_warning "Backend may still be starting. Check /tmp/backend.log"
            fi
        fi
        print_info "Backend logs: /tmp/backend.log"
        return 0
    else
        print_error "Backend failed to start!"
        print_info "Last 30 lines of backend log:"
        # Use text mode (-a) to avoid binary file matches noise if log contains non-text bytes
        if command -v grep &> /dev/null; then
            tail -30 /tmp/backend.log 2>/dev/null | grep -a -E -A 3 -B 3 'error|exception|failed' || tail -30 /tmp/backend.log
        else
            tail -30 /tmp/backend.log 2>/dev/null || echo \"No backend log found\"
        fi
        print_info "Full log: /tmp/backend.log"
        return 1
    fi
}

# Function to start frontend
start_frontend() {
    print_header "Starting Frontend"
    
    if [ ! -d "frontend" ]; then
        print_warning "Frontend directory not found. Skipping frontend start."
        return 0
    fi
    
    cd frontend
    
    # Frontend is configured in package.json to use --host 0.0.0.0 --port 5000
    print_info "Starting Angular frontend on port 5000 (accessible on all interfaces)..."
    
    # Start frontend in background (package.json has the correct host/port config)
    npm start > /tmp/frontend.log 2>&1 &
    FRONTEND_PID=$!
    echo $FRONTEND_PID > /tmp/frontend.pid
    
    print_info "Frontend PID: $FRONTEND_PID"
    print_info "Frontend logs: /tmp/frontend.log"
    
    cd ..
    
    # Wait a bit for frontend to start
    sleep 8
    
    # Check if frontend is still running
    if kill -0 $FRONTEND_PID 2>/dev/null; then
        # Check if port 5000 is listening
        if netstat -tuln 2>/dev/null | grep -q ":5000" || ss -tuln 2>/dev/null | grep -q ":5000"; then
            print_success "Frontend started successfully on port 5000"
            return 0
        else
            # Check for compilation errors
            if grep -q "ERROR\|Error\|✘" /tmp/frontend.log 2>/dev/null; then
                print_error "Frontend compilation failed!"
                print_info "Last 20 lines of frontend log:"
                tail -20 /tmp/frontend.log | grep -A 5 -B 5 "ERROR\|Error\|✘" || tail -20 /tmp/frontend.log
                print_info "Full log available at: /tmp/frontend.log"
                return 1
            else
                print_warning "Frontend process running but port 5000 not listening yet. Still compiling..."
                print_info "Check progress: tail -f /tmp/frontend.log"
                return 0
            fi
        fi
    else
        print_error "Frontend process died!"
        print_info "Last 30 lines of frontend log:"
        tail -30 /tmp/frontend.log 2>/dev/null || echo "No log file found"
        return 1
    fi
}

# Function to show status
show_status() {
    print_header "Service Status"
    
    SERVER_IP=$(get_server_ip)
    
    echo "Database:"
    if docker ps --format '{{.Names}}' | grep -qE "(cbs-dashboard-postgres|cleaning-postgres)"; then
        print_success "Running"
        docker ps --filter "name=postgres" --format "  Container: {{.Names}} | Status: {{.Status}} | Ports: {{.Ports}}"
    else
        print_warning "Not running"
    fi
    
    echo ""
    echo "Backend:"
    if [ -f "/tmp/backend.pid" ] && kill -0 $(cat /tmp/backend.pid) 2>/dev/null; then
        print_success "Running (PID: $(cat /tmp/backend.pid))"
    else
        print_warning "Not running"
    fi
    
    echo ""
    echo "Frontend:"
    if [ -f "/tmp/frontend.pid" ] && kill -0 $(cat /tmp/frontend.pid) 2>/dev/null; then
        print_success "Running (PID: $(cat /tmp/frontend.pid))"
    else
        print_warning "Not running"
    fi
    
    echo ""
    print_header "Access URLs"
    print_success "Frontend Application:"
    print_info "  Local:  http://localhost:5000"
    print_info "  Remote: http://${SERVER_IP}:5000"
    echo ""
    print_success "Backend API:"
    print_info "  Local:  http://localhost:8090"
    print_info "  Remote: http://${SERVER_IP}:8090"
    echo ""
    print_info "Make sure firewall allows connections on ports 5000 and 8090"
}

# Main function
main() {
    print_header "CBS Dashboard - Update and Restart"
    
    # Step 1: Stop everything
    stop_all_processes
    stop_and_clean_containers
    
    echo ""
    
    # Step 2: Rebuild (if requested or if changes detected)
    REBUILD=${1:-"yes"}
    if [ "$REBUILD" = "yes" ] || [ "$REBUILD" = "rebuild" ]; then
        rebuild_frontend
        echo ""
        rebuild_backend
        echo ""
    else
        print_info "Skipping rebuild (use 'rebuild' argument to rebuild)"
    fi
    
    # Step 3: Start everything
    if ! start_database; then
        print_error "Failed to start database. Cannot continue."
        print_info "You can try:"
        print_info "  1. Check logs: docker logs cbs-dashboard-postgres"
        print_info "  2. Clean volume: docker volume rm cbs-dashboard_postgres_data"
        print_info "  3. Run again: ./update.sh"
        exit 1
    fi
    echo ""
    
    sleep 3  # Give database a moment to fully initialize
    
    start_backend
    echo ""
    
    sleep 2  # Give backend a moment to start
    
    start_frontend
    echo ""
    
    # Step 4: Show status
    sleep 3
    show_status
    
    echo ""
    print_success "Update and restart completed!"
    echo ""
    SERVER_IP=$(get_server_ip)
    print_info "🌐 Access your application:"
    print_info "   Frontend: http://${SERVER_IP}:5000"
    print_info "   Backend API: http://${SERVER_IP}:8090"
    echo ""
    print_info "📋 Useful commands:"
    print_info "  Stop all services: ./update.sh stop"
    print_info "  Check status: ./update.sh status"
    print_info "  View logs:"
    print_info "    Backend: tail -f /tmp/backend.log"
    print_info "    Frontend: tail -f /tmp/frontend.log"
    echo ""
    print_warning "⚠️  Make sure your firewall allows connections on ports 5000 and 8090"
}

# Handle stop command
if [ "${1}" = "stop" ]; then
    print_header "Stopping All Services"
    stop_all_processes
    stop_and_clean_containers
    print_success "All services stopped"
    exit 0
fi

# Handle clean-volumes command
if [ "${1}" = "clean-volumes" ]; then
    print_header "Cleaning Docker Volumes"
    stop_all_processes
    stop_and_clean_containers
    print_info "Removing PostgreSQL volume..."
    docker volume rm cbs-dashboard_postgres_data 2>/dev/null || print_warning "Volume not found or already removed"
    print_success "Volumes cleaned. You can now run ./update.sh to start fresh."
    exit 0
fi

# Handle status command
if [ "${1}" = "status" ]; then
    show_status
    exit 0
fi

# Run main function
main "$@"

