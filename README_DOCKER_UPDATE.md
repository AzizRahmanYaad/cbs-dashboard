# Docker Update Script - Complete Guide

## 🚀 Quick Start

**Run the main script to set up everything:**

```bash
chmod +x docker-update.sh
./docker-update.sh
```

This will show an interactive menu with all options.

## 📋 Available Commands

### Interactive Menu Mode

```bash
./docker-update.sh
# or
./docker-update.sh menu
```

Shows an interactive menu with options:
1. Complete System Setup (Install Docker + Start PostgreSQL)
2. Install/Update Docker Only
3. Start PostgreSQL Database
4. Stop PostgreSQL Database
5. Restart PostgreSQL Database
6. Update System & Restart Services
7. Reboot System
8. Check System Status
9. Exit

### Direct Command Mode

```bash
# Complete system setup (recommended for first time)
./docker-update.sh setup

# Install Docker
./docker-update.sh install-docker

# Update Docker
./docker-update.sh update-docker

# Start PostgreSQL
./docker-update.sh start-db
# or
./docker-update.sh start

# Stop PostgreSQL
./docker-update.sh stop-db
# or
./docker-update.sh stop

# Restart PostgreSQL
./docker-update.sh restart-db
# or
./docker-update.sh restart

# Update system and restart services
./docker-update.sh update-restart

# Reboot system
./docker-update.sh reboot

# Check system status
./docker-update.sh status
```

## 🔧 What the Script Does

### Complete System Setup (`setup`)

1. ✅ Detects your operating system
2. ✅ Installs Docker (if not installed)
3. ✅ Updates Docker (if already installed)
4. ✅ Ensures Docker daemon is running
5. ✅ Starts PostgreSQL container
6. ✅ Waits for database to be ready

### System Updates (`update-restart`)

1. ✅ Updates system packages
2. ✅ Updates Docker
3. ✅ Restarts Docker service
4. ✅ Restarts PostgreSQL container

### Database Management

- **Start**: Creates and starts PostgreSQL container on port **5442**
- **Stop**: Gracefully stops PostgreSQL container
- **Restart**: Stops and starts PostgreSQL container
- **Status**: Shows Docker and PostgreSQL status

## 🌐 Database Configuration

After running the script, PostgreSQL will be available at:

- **Host:** localhost
- **Port:** 5443 (external) → 5432 (internal container)
- **Database:** cbs_dashboard
- **Username:** cbs_user
- **Password:** admin123

## 🔄 System Reboot

The script includes a reboot option that:
- Warns you 10 seconds before rebooting
- Allows cancellation with Ctrl+C
- Safely reboots the system

**Note:** After reboot, you'll need to start PostgreSQL again:
```bash
./docker-update.sh start-db
```

## 📊 Status Check

Check the current status of Docker and PostgreSQL:

```bash
./docker-update.sh status
```

This shows:
- Docker version and daemon status
- PostgreSQL container status
- Container ports and health

## 🛠️ Troubleshooting

### Docker Not Installed

The script will automatically install Docker for:
- Ubuntu/Debian
- CentOS/RHEL/Fedora

For other systems, install Docker manually first.

### Docker Permission Denied

If you get permission errors:
```bash
sudo usermod -aG docker $USER
# Then log out and back in
```

Or run with sudo:
```bash
sudo ./docker-update.sh
```

### Port 5443 Already in Use

Check what's using the port:
```bash
./check-port.sh 5443
# or manually:
sudo lsof -i :5443
# or
sudo netstat -tulpn | grep 5443
```

Stop the conflicting service or change the port in `compose.yaml`.

### PostgreSQL Won't Start

Check the logs:
```bash
docker logs cbs-dashboard-postgres
```

Or use the automation script:
```bash
./docker-automation.sh logs
```

## 🔐 Security Notes

- The script may require sudo for system operations
- Docker group membership allows running Docker without sudo
- Database password is set in `compose.yaml` and `application.properties`

## 📝 Integration with Other Scripts

The `start.sh`, `start-dev.sh`, and `start-all.sh` scripts automatically use `docker-update.sh` to start the database before starting the application.

## 🎯 Typical Workflow

1. **First time setup:**
   ```bash
   ./docker-update.sh setup
   ```

2. **Start application:**
   ```bash
   ./start.sh
   ```

3. **Check status:**
   ```bash
   ./docker-update.sh status
   ```

4. **Update system:**
   ```bash
   ./docker-update.sh update-restart
   ```

5. **Stop everything:**
   ```bash
   ./docker-update.sh stop-db
   ```

## 💡 Tips

- Run `./docker-update.sh status` regularly to check system health
- Use `update-restart` after system package updates
- The script handles OS detection automatically
- All operations are logged with clear status messages

