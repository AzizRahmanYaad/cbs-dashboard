# Quick Start Guide

## 🚀 One-Command Startup

### Option 1: Complete Setup (Recommended for First Time)

To set up Docker and start everything:

```bash
chmod +x docker-update.sh start.sh
./docker-update.sh setup
./start.sh
```

### Option 2: Quick Start (If Docker is Already Set Up)

To start everything automatically (database + application):

```bash
chmod +x docker-update.sh start.sh
./start.sh
```

That's it! The scripts will:
- ✅ Check Docker installation
- ✅ Start PostgreSQL automatically on port **5443**
- ✅ Start Spring Boot backend
- ✅ Start Angular frontend

## 📋 Available Commands

### Main Scripts

| Script | Description |
|--------|-------------|
| `./start.sh` | **Start everything** (database + app) - Recommended |
| `./start.sh all` | Start everything with full stack mode |
| `./start-dev.sh` | Start app in dev mode (auto-starts DB) |
| `./start-all.sh` | Start app in full stack mode (auto-starts DB) |

### Database Management (Primary - docker-update.sh)

| Command | Description |
|---------|-------------|
| `./docker-update.sh setup` | **Complete system setup** (install Docker + start DB) |
| `./docker-update.sh start-db` | Start PostgreSQL |
| `./docker-update.sh stop-db` | Stop PostgreSQL |
| `./docker-update.sh restart-db` | Restart PostgreSQL |
| `./docker-update.sh status` | Check system status |
| `./docker-update.sh update-restart` | Update system & restart services |
| `./docker-update.sh reboot` | Reboot system |

### Database Management (Alternative - docker-automation.sh)

| Command | Description |
|---------|-------------|
| `./docker-automation.sh start` | Start PostgreSQL |
| `./docker-automation.sh stop` | Stop PostgreSQL |
| `./docker-automation.sh restart` | Restart PostgreSQL |
| `./docker-automation.sh status` | Check PostgreSQL status |
| `./docker-automation.sh logs` | View PostgreSQL logs |
| `./docker-automation.sh logs -f` | Follow PostgreSQL logs |
| `./docker-automation.sh remove` | Remove PostgreSQL and all data |

## 🔧 First Time Setup

1. **Make scripts executable:**
   ```bash
   chmod +x *.sh
   ```

2. **Ensure Docker is installed:**
   ```bash
   docker --version
   docker compose version
   ```

3. **Start the application:**
   ```bash
   ./start.sh
   ```

## 🌐 Access Points

After starting:
- **Frontend:** http://localhost:5000
- **Backend API:** http://localhost:8090
- **Database:** localhost:5443

## 🛑 Stopping Everything

1. **Stop the application:** Press `Ctrl+C` in the terminal
2. **Stop the database:**
   ```bash
   ./docker-update.sh stop-db
   ```

## ❓ Troubleshooting

### Database Connection Refused

```bash
# Check if database is running
./docker-update.sh status

# If not running, start it
./docker-update.sh start-db

# Or run complete setup
./docker-update.sh setup

# Check logs if issues persist
./docker-automation.sh logs
```

### Docker Not Running

```bash
# Start Docker service (Linux)
sudo systemctl start docker
sudo systemctl enable docker  # Auto-start on boot
```

### Port Already in Use

If port 5443 is in use, check what's using it:
```bash
./check-port.sh 5443
# or manually:
sudo lsof -i :5443
# or
sudo netstat -tulpn | grep 5443
```

## 📚 More Information

- See `README_DOCKER_UPDATE.md` for complete docker-update.sh guide
- See `DATABASE_SETUP.md` for detailed database setup
- See `README.md` for project overview

