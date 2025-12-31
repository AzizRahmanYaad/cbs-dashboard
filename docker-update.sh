#!/bin/bash

# Docker Update Script - Complete System Management
# This script handles Docker installation, updates, PostgreSQL setup, and system reboots
# Run this script to set up and maintain the entire CBS Dashboard system

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

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

# Function to check if running as root
check_root() {
    if [ "$EUID" -ne 0 ]; then
        print_warning "Some operations require root privileges."
        print_info "You may be prompted for sudo password."
    fi
}

# Function to detect OS
detect_os() {
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$ID
        OS_VERSION=$VERSION_ID
        print_info "Detected OS: $OS $OS_VERSION"
    else
        print_warning "Cannot detect OS. Assuming Ubuntu/Debian."
        OS="ubuntu"
    fi
}

# Function to install Docker
install_docker() {
    print_header "Installing Docker"
    
    if command -v docker &> /dev/null; then
        print_success "Docker is already installed"
        docker --version
        return 0
    fi
    
    print_info "Docker is not installed. Installing now..."
    
    if [ "$OS" = "ubuntu" ] || [ "$OS" = "debian" ]; then
        # Update package index
        print_info "Updating package index..."
        sudo apt-get update -y
        
        # Install prerequisites
        print_info "Installing prerequisites..."
        sudo apt-get install -y \
            ca-certificates \
            curl \
            gnupg \
            lsb-release
        
        # Add Docker's official GPG key
        print_info "Adding Docker's GPG key..."
        sudo install -m 0755 -d /etc/apt/keyrings
        curl -fsSL https://download.docker.com/linux/$OS/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
        sudo chmod a+r /etc/apt/keyrings/docker.gpg
        
        # Set up repository
        print_info "Setting up Docker repository..."
        echo \
          "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/$OS \
          $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
        
        # Install Docker
        print_info "Installing Docker Engine..."
        sudo apt-get update -y
        sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
        
        # Add current user to docker group (if not root)
        if [ "$EUID" -ne 0 ]; then
            print_info "Adding user to docker group..."
            sudo usermod -aG docker $USER
            print_warning "You may need to log out and back in for group changes to take effect."
        fi
        
        # Start and enable Docker
        print_info "Starting Docker service..."
        sudo systemctl start docker
        sudo systemctl enable docker
        
        print_success "Docker installed successfully"
        docker --version
        
    elif [ "$OS" = "centos" ] || [ "$OS" = "rhel" ] || [ "$OS" = "fedora" ]; then
        print_info "Installing Docker on $OS..."
        sudo yum install -y yum-utils
        sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
        sudo yum install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
        sudo systemctl start docker
        sudo systemctl enable docker
        
        if [ "$EUID" -ne 0 ]; then
            sudo usermod -aG docker $USER
        fi
        
        print_success "Docker installed successfully"
    else
        print_error "Unsupported OS. Please install Docker manually."
        print_info "Visit: https://docs.docker.com/get-docker/"
        return 1
    fi
}

# Function to update Docker
update_docker() {
    print_header "Updating Docker"
    
    if ! command -v docker &> /dev/null; then
        print_warning "Docker is not installed. Installing instead..."
        install_docker
        return
    fi
    
    print_info "Checking for Docker updates..."
    
    if [ "$OS" = "ubuntu" ] || [ "$OS" = "debian" ]; then
        sudo apt-get update -y
        sudo apt-get upgrade -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    elif [ "$OS" = "centos" ] || [ "$OS" = "rhel" ] || [ "$OS" = "fedora" ]; then
        sudo yum update -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    fi
    
    print_success "Docker updated"
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
        if [ "$OS" = "ubuntu" ] || [ "$OS" = "debian" ]; then
            print_info "Installing Docker Compose..."
            sudo apt-get install -y docker-compose-plugin
        fi
        COMPOSE_CMD="docker compose"
    fi
}

# Function to start PostgreSQL
start_postgres() {
    print_header "Starting PostgreSQL Database"
    
    check_docker_compose
    
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
            echo "  Port: 5442"
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
    check_docker_compose
    
    if docker ps --format '{{.Names}}' | grep -q "^cbs-dashboard-postgres$"; then
        $COMPOSE_CMD stop postgres
        print_success "PostgreSQL stopped"
    else
        print_warning "PostgreSQL container is not running"
    fi
}

# Function to restart system
reboot_system() {
    print_header "System Reboot"
    
    print_warning "The system will reboot in 10 seconds..."
    print_warning "Press Ctrl+C to cancel"
    
    sleep 10
    
    print_info "Rebooting system now..."
    sudo reboot
}

# Function to setup complete system
setup_complete() {
    print_header "Complete System Setup"
    
    # Detect OS
    detect_os
    
    # Install/Update Docker
    if ! command -v docker &> /dev/null; then
        install_docker
    else
        read -p "Update Docker? (y/n): " update_docker_choice
        if [ "$update_docker_choice" = "y" ] || [ "$update_docker_choice" = "Y" ]; then
            update_docker
        fi
    fi
    
    # Ensure Docker daemon is running
    if ! docker info &> /dev/null; then
        print_info "Starting Docker daemon..."
        sudo systemctl start docker
        sudo systemctl enable docker
    fi
    
    # Start PostgreSQL
    start_postgres
    
    print_success "System setup complete!"
    echo ""
    print_info "Next steps:"
    echo "  1. Start the application: ./start.sh"
    echo "  2. Check database status: ./docker-automation.sh status"
    echo "  3. View logs: ./docker-automation.sh logs"
}

# Function to update and restart
update_and_restart() {
    print_header "Update and Restart System"
    
    detect_os
    
    # Update system packages
    print_info "Updating system packages..."
    if [ "$OS" = "ubuntu" ] || [ "$OS" = "debian" ]; then
        sudo apt-get update -y
        sudo apt-get upgrade -y
    elif [ "$OS" = "centos" ] || [ "$OS" = "rhel" ] || [ "$OS" = "fedora" ]; then
        sudo yum update -y
    fi
    
    # Update Docker
    update_docker
    
    # Restart Docker service
    print_info "Restarting Docker service..."
    sudo systemctl restart docker
    
    # Restart PostgreSQL
    stop_postgres
    sleep 2
    start_postgres
    
    print_success "System updated and restarted"
}

# Main menu
show_menu() {
    print_header "CBS Dashboard - Docker Update & Management"
    echo "1. Complete System Setup (Install Docker + Start PostgreSQL)"
    echo "2. Install/Update Docker Only"
    echo "3. Start PostgreSQL Database"
    echo "4. Stop PostgreSQL Database"
    echo "5. Restart PostgreSQL Database"
    echo "6. Update System & Restart Services"
    echo "7. Reboot System"
    echo "8. Check System Status"
    echo "9. Exit"
    echo ""
    read -p "Select an option (1-9): " choice
    
    case $choice in
        1)
            setup_complete
            ;;
        2)
            detect_os
            if ! command -v docker &> /dev/null; then
                install_docker
            else
                update_docker
            fi
            ;;
        3)
            start_postgres
            ;;
        4)
            stop_postgres
            ;;
        5)
            stop_postgres
            sleep 2
            start_postgres
            ;;
        6)
            update_and_restart
            ;;
        7)
            reboot_system
            ;;
        8)
            print_header "System Status"
            echo "Docker:"
            if command -v docker &> /dev/null; then
                docker --version
                docker info &> /dev/null && print_success "Docker daemon is running" || print_error "Docker daemon is not running"
            else
                print_error "Docker is not installed"
            fi
            echo ""
            echo "PostgreSQL:"
            if docker ps --format '{{.Names}}' | grep -q "^cbs-dashboard-postgres$"; then
                print_success "PostgreSQL container is running"
                docker ps --filter "name=cbs-dashboard-postgres" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
            else
                print_warning "PostgreSQL container is not running"
            fi
            ;;
        9)
            print_info "Exiting..."
            exit 0
            ;;
        *)
            print_error "Invalid option"
            ;;
    esac
}

# Main script logic
main() {
    check_root
    
    # If arguments provided, run specific command
    case "${1:-menu}" in
        setup)
            setup_complete
            ;;
        install-docker)
            detect_os
            install_docker
            ;;
        update-docker)
            detect_os
            update_docker
            ;;
        start-db|start)
            start_postgres
            ;;
        stop-db|stop)
            stop_postgres
            ;;
        restart-db|restart)
            stop_postgres
            sleep 2
            start_postgres
            ;;
        update-restart)
            update_and_restart
            ;;
        reboot)
            reboot_system
            ;;
        status)
            print_header "System Status"
            echo "Docker:"
            if command -v docker &> /dev/null; then
                docker --version
                docker info &> /dev/null && print_success "Docker daemon is running" || print_error "Docker daemon is not running"
            else
                print_error "Docker is not installed"
            fi
            echo ""
            echo "PostgreSQL:"
            if docker ps --format '{{.Names}}' | grep -q "^cbs-dashboard-postgres$"; then
                print_success "PostgreSQL container is running"
                docker ps --filter "name=cbs-dashboard-postgres" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
            else
                print_warning "PostgreSQL container is not running"
            fi
            ;;
        menu|*)
            show_menu
            ;;
    esac
}

# Run main function
main "$@"

