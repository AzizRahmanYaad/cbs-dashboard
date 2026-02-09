# Data Persistence Fix

## Issue
Data (students, teachers, programs) was disappearing when refreshing the page or restarting services.

## Root Cause
The `update.sh` script was:
1. Removing the database container with `docker rm -f cbs-dashboard-postgres`
2. Using `docker compose down -v` which removes volumes, deleting all data

## Solution Applied
1. **Stopped removing database container**: Changed to only stop (not remove) the database container
2. **Removed volume deletion**: Removed the `-v` flag from `docker compose down` to preserve data volumes
3. **Disabled auto-volume cleanup**: Changed volume cleanup functions to warn instead of auto-deleting

## Changes Made

### update.sh
- Line 94: Removed `-v` flag from `docker compose down` (preserves volumes)
- Line 107-110: Changed to stop container instead of removing it
- Line 267-272: Disabled automatic volume deletion on data format issues
- Line 147-152: Disabled automatic volume deletion in cleanup function

## How Data is Now Preserved

1. **Database Volume**: The `cbs-dashboard_postgres_data` volume is never removed automatically
2. **Container**: The database container is stopped but not removed, preserving the connection to the volume
3. **Manual Reset**: If you need to reset the database, you must manually run:
   ```bash
   docker stop cbs-dashboard-postgres
   docker rm cbs-dashboard-postgres
   docker volume rm cbs-dashboard_postgres_data
   ```

## Verification

To verify data persistence:
```bash
# Check data exists
docker exec cbs-dashboard-postgres psql -U cbs_user -d cbs_dashboard -c "SELECT COUNT(*) FROM student_teachers;"
docker exec cbs-dashboard-postgres psql -U cbs_user -d cbs_dashboard -c "SELECT COUNT(*) FROM training_programs;"

# Restart services
./update.sh

# Check data still exists
docker exec cbs-dashboard-postgres psql -U cbs_user -d cbs_dashboard -c "SELECT COUNT(*) FROM student_teachers;"
```

## Important Notes

- **Data is now persistent** across restarts and updates
- **Volume is preserved** when using `update.sh`
- **Container is preserved** (stopped but not removed)
- **Manual intervention required** if you need to reset the database
