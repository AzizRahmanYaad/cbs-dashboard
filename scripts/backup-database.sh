#!/bin/bash
# CBS Dashboard - Automated Daily Database Backup
# Full database backup stored in date/time-named directories

set -e

# Configuration
BACKUP_BASE="${BACKUP_BASE:-/var/backups/cbs-dashboard}"
DATE_DIR=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="${BACKUP_BASE}/${DATE_DIR}"
DB_CONTAINER="${DB_CONTAINER:-cbs-dashboard-postgres}"
DB_NAME="${DB_NAME:-cbs_dashboard}"
DB_USER="${DB_USER:-cbs_user}"

# Resolve script directory (works when called from cron)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Create backup directory
mkdir -p "$BACKUP_DIR"

# Check if PostgreSQL is running in Docker
if docker ps --format '{{.Names}}' 2>/dev/null | grep -q "^${DB_CONTAINER}$"; then
    log "Backing up database from Docker container: $DB_CONTAINER"
    
    # Create dump inside container, then copy out
    docker exec "$DB_CONTAINER" pg_dump -U "$DB_USER" -d "$DB_NAME" -F c -f /tmp/cbs_backup.dump
    docker cp "${DB_CONTAINER}:/tmp/cbs_backup.dump" "$BACKUP_DIR/cbs_dashboard.dump"
    docker exec "$DB_CONTAINER" rm -f /tmp/cbs_backup.dump
    
    log "Backup saved: $BACKUP_DIR/cbs_dashboard.dump"
else
    # Fallback: direct PostgreSQL connection (localhost:5443)
    log "Docker container not found. Attempting direct PostgreSQL connection..."
    export PGPASSWORD="${DB_PASSWORD:-admin123}"
    
    if pg_dump -h localhost -p 5443 -U "$DB_USER" -d "$DB_NAME" -F c -f "$BACKUP_DIR/cbs_dashboard.dump" 2>/dev/null; then
        log "Backup saved: $BACKUP_DIR/cbs_dashboard.dump"
    else
        # Fallback to plain SQL if custom format fails
        pg_dump -h localhost -p 5443 -U "$DB_USER" -d "$DB_NAME" > "$BACKUP_DIR/cbs_dashboard.sql"
        log "Backup saved: $BACKUP_DIR/cbs_dashboard.sql"
    fi
    unset PGPASSWORD
fi

# Write backup metadata
cat > "$BACKUP_DIR/backup_info.txt" << EOF
CBS Dashboard Database Backup
=============================
Date/Time: $(date '+%Y-%m-%d %H:%M:%S')
Backup Directory: $BACKUP_DIR
Database: $DB_NAME
Source: ${DB_CONTAINER:-localhost:5443}
EOF

log "Backup completed successfully: $BACKUP_DIR"

# Retention: keep last 30 days (optional)
find "$BACKUP_BASE" -maxdepth 1 -type d -name '20*' -mtime +30 -exec rm -rf {} \; 2>/dev/null || true
