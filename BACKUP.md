# CBS Dashboard - Database Backup

Automated daily database backups with date/time-organized storage.

## Overview

- **Schedule:** 12:00 PM (noon) daily
- **Location:** `/var/backups/cbs-dashboard/YYYYMMDD_HHMMSS/`
- **Format:** PostgreSQL custom format (`.dump`) for reliable restores

## Quick Setup

```bash
cd /var/www/cbs-dashboard/cbs-dashboard
chmod +x scripts/backup-database.sh scripts/install-backup-cron.sh
./scripts/install-backup-cron.sh
```

## Manual Backup

Run a backup immediately:

```bash
./scripts/backup-database.sh
```

## Backup Structure

Each backup is stored in its own directory:

```
/var/backups/cbs-dashboard/
├── 20250208_120001/
│   ├── cbs_dashboard.dump    # Database dump
│   └── backup_info.txt       # Metadata
├── 20250209_120001/
│   ├── cbs_dashboard.dump
│   └── backup_info.txt
└── ...
```

## Restore

```bash
# From custom format (.dump)
pg_restore -U cbs_user -d cbs_dashboard -c /var/backups/cbs-dashboard/YYYYMMDD_HHMMSS/cbs_dashboard.dump

# If using Docker
docker exec -i cbs-dashboard-postgres pg_restore -U cbs_user -d cbs_dashboard -c < /var/backups/cbs-dashboard/YYYYMMDD_HHMMSS/cbs_dashboard.dump
```

## Retention

Backups older than 30 days are automatically removed. Change this in `scripts/backup-database.sh` by editing the `find ... -mtime +30` line.

## Logs

Backup logs: `/var/log/cbs-dashboard-backup.log`

## Configuration

Environment variables (optional):

| Variable        | Default                    | Description                    |
|----------------|----------------------------|--------------------------------|
| BACKUP_BASE    | /var/backups/cbs-dashboard | Base backup directory          |
| DB_CONTAINER   | cbs-dashboard-postgres     | Docker PostgreSQL container    |
| DB_NAME        | cbs_dashboard              | Database name                  |
| DB_USER        | cbs_user                   | Database user                  |
