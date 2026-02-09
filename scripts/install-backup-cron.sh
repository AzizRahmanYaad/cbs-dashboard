#!/bin/bash
# Install cron job for CBS Dashboard daily database backup at 12:00 PM

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_SCRIPT="$SCRIPT_DIR/backup-database.sh"
CRON_ENTRY="0 12 * * * $BACKUP_SCRIPT >> /var/log/cbs-dashboard-backup.log 2>&1"

# Ensure backup script is executable
chmod +x "$BACKUP_SCRIPT"

# Create backup directory
sudo mkdir -p /var/backups/cbs-dashboard
sudo chown "$(whoami):$(whoami)" /var/backups/cbs-dashboard 2>/dev/null || true

# Add to crontab (merge with existing)
(crontab -l 2>/dev/null | grep -v "backup-database.sh" | grep -v "cbs-dashboard-backup"; echo "$CRON_ENTRY") | crontab -

echo "Cron job installed: Daily database backup at 12:00 PM"
echo "Backup location: /var/backups/cbs-dashboard/YYYYMMDD_HHMMSS/"
echo "Log file: /var/log/cbs-dashboard-backup.log"
echo ""
echo "Current crontab:"
crontab -l | grep -E "backup-database|cbs-dashboard"
