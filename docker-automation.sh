#!/bin/bash

# Docker Automation Script for CBS Dashboard
# This script automatically manages Docker and PostgreSQL for the application

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

# Function to check if Docker is installed
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed."
        echo ""
        echo "Please install Docker first:"
        echo "  Ubuntu/Debian: sudo apt-get update && sudo apt-get install -y docker.io docker-compose"
        echo "  Or visit: https://docs.docker.com/get-docker/"
        exit 1
    fi
    
    # Check if Docker daemon is running
    if ! docker info &> /dev/null; then
        print_error "Docker daemon is not running."
        echo ""
        echo "Please start Docker:"
        echo "  sudo systemctl start docker"
        echo "  sudo systemctl enable docker  # to start on boot"
        exit 1
    fi
    
    print_success "Docker is installed and running"
}

# Function to check Docker Compose
check_docker_compose() {
    if command -v docker-compose &> /dev/null; then
        COMPOSE_CMD="docker-compose"
        print_success "Docker Compose (standalone) found"
    elif docker compose version &> /dev/null; then
        COMPOSE_CMD="docker compose"
        print_success "Docker Compose (plugin) found"
    else
        print_error "Docker Compose is not available."
        echo ""
        echo "Please install Docker Compose:"
        echo "  sudo apt-get install -y docker-compose"
        exit 1
    fi
}

# Function to start PostgreSQL
start_postgres() {
    print_info "Starting PostgreSQL database..."
    
    # Check if container already exists and is running
    if docker ps --format '{{.Names}}' | grep -q "^cbs-dashboard-postgres$"; then
        print_success "PostgreSQL container is already running"
        return 0
    fi
    
    # Check if container exists but is stopped
    if docker ps -a --format '{{.Names}}' | grep -q "^cbs-dashboard-postgres$"; then
        print_info "Starting existing PostgreSQL container..."
        docker start cbs-dashboard-postgres
    else
        print_info "Creating and starting PostgreSQL container..."
        $COMPOSE_CMD up -d postgres
    fi
    
    # Wait for PostgreSQL to be ready
    print_info "Waiting for PostgreSQL to be ready..."
    max_attempts=30
    attempt=0
    
    while [ $attempt -lt $max_attempts ]; do
        if docker exec cbs-dashboard-postgres pg_isready -U cbs_user -d cbs_dashboard &> /dev/null 2>&1; then
            print_success "PostgreSQL is ready!"
            echo ""
            print_info "Database connection details:"
            echo "  Host: localhost"
            echo "  Port: 5443"
            echo "  Database: cbs_dashboard"
            echo "  Username: cbs_user"
            echo "  Password: admin123"
            return 0
        fi
        attempt=$((attempt + 1))
        sleep 1
        echo -n "."
    done
    
    echo ""
    print_error "PostgreSQL failed to start within $max_attempts seconds"
    print_info "Check logs with: docker logs cbs-dashboard-postgres"
    return 1
}

# Function to stop PostgreSQL
stop_postgres() {
    print_info "Stopping PostgreSQL database..."
    
    if docker ps --format '{{.Names}}' | grep -q "^cbs-dashboard-postgres$"; then
        $COMPOSE_CMD stop postgres
        print_success "PostgreSQL stopped"
    else
        print_warning "PostgreSQL container is not running"
    fi
}

# Function to restart PostgreSQL
restart_postgres() {
    print_info "Restarting PostgreSQL database..."
    stop_postgres
    sleep 2
    start_postgres
}

# Function to check PostgreSQL status
status_postgres() {
    if docker ps --format '{{.Names}}' | grep -q "^cbs-dashboard-postgres$"; then
        print_success "PostgreSQL is running"
        
        # Check if it's healthy
        if docker exec cbs-dashboard-postgres pg_isready -U cbs_user -d cbs_dashboard &> /dev/null 2>&1; then
            print_success "PostgreSQL is healthy and accepting connections"
        else
            print_warning "PostgreSQL is running but not ready yet"
        fi
        
        # Show container info
        echo ""
        docker ps --filter "name=cbs-dashboard-postgres" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    else
        print_warning "PostgreSQL is not running"
        
        # Check if container exists but is stopped
        if docker ps -a --format '{{.Names}}' | grep -q "^cbs-dashboard-postgres$"; then
            print_info "Container exists but is stopped. Start it with: ./docker-automation.sh start"
        fi
    fi
}

# Function to show logs
show_logs() {
    if docker ps -a --format '{{.Names}}' | grep -q "^cbs-dashboard-postgres$"; then
        docker logs cbs-dashboard-postgres "$@"
    else
        print_error "PostgreSQL container does not exist"
    fi
}

# Function to remove PostgreSQL (with data)
remove_postgres() {
    print_warning "This will remove the PostgreSQL container and all data!"
    read -p "Are you sure? (yes/no): " confirm
    
    if [ "$confirm" = "yes" ]; then
        print_info "Removing PostgreSQL container and volumes..."
        $COMPOSE_CMD down -v
        print_success "PostgreSQL container and data removed"
    else
        print_info "Operation cancelled"
    fi
}

# Main script logic
main() {
    case "${1:-start}" in
        start)
            check_docker
            check_docker_compose
            start_postgres
            ;;
        stop)
            check_docker
            check_docker_compose
            stop_postgres
            ;;
        restart)
            check_docker
            check_docker_compose
            restart_postgres
            ;;
        status)
            check_docker
            status_postgres
            ;;
        logs)
            check_docker
            show_logs "${@:2}"
            ;;
        remove)
            check_docker
            check_docker_compose
            remove_postgres
            ;;
        *)
            echo "Usage: $0 {start|stop|restart|status|logs|remove}"
            echo ""
            echo "Commands:"
            echo "  start   - Start PostgreSQL container (default)"
            echo "  stop    - Stop PostgreSQL container"
            echo "  restart - Restart PostgreSQL container"
            echo "  status  - Check PostgreSQL status"
            echo "  logs    - Show PostgreSQL logs (add -f for follow)"
            echo "  remove  - Remove PostgreSQL container and all data"
            exit 1
            ;;
    esac
}

# Run main function
main "$@"

