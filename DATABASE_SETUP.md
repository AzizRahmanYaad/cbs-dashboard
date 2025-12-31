# Database Setup Guide

## Quick Start

The application requires PostgreSQL to be running before starting the Spring Boot backend.

### Option 1: Using Docker Compose (Recommended)

1. **Start the database:**
   ```bash
   chmod +x start-database.sh
   ./start-database.sh
   ```

2. **Start the application:**
   ```bash
   ./start-dev.sh
   ```
   or
   ```bash
   ./start-all.sh
   ```

3. **Stop the database (when done):**
   ```bash
   ./stop-database.sh
   ```

### Option 2: Manual Docker Compose

```bash
docker compose up -d postgres
```

### Option 3: Local PostgreSQL Installation

If you have PostgreSQL installed locally, make sure:
- PostgreSQL service is running
- Database `cbs_dashboard` exists
- User `cbs_user` exists with password `admin123`
- User has proper permissions on the database

## Database Configuration

The application is configured to connect to:
- **Host:** localhost
- **Port:** 5432
- **Database:** cbs_dashboard
- **Username:** cbs_user
- **Password:** admin123

These settings are in `src/main/resources/application.properties`.

## Troubleshooting

### Connection Refused Error

If you see `Connection to localhost:5432 refused`:

1. **Check if PostgreSQL is running:**
   ```bash
   docker ps | grep postgres
   ```

2. **Check PostgreSQL logs:**
   ```bash
   docker logs cbs-dashboard-postgres
   ```

3. **Verify the container is healthy:**
   ```bash
   docker exec cbs-dashboard-postgres pg_isready -U cbs_user -d cbs_dashboard
   ```

### Database Already Exists

If you need to reset the database:
```bash
docker compose down -v  # This removes the volume and all data
docker compose up -d postgres
```

### Port Already in Use

If port 5432 is already in use:
1. Stop the existing PostgreSQL service
2. Or modify `compose.yaml` to use a different port mapping (e.g., `5433:5432`)
3. Update `application.properties` with the new port

## Creating Initial Tables

After starting PostgreSQL, the application will automatically create tables using Hibernate (`spring.jpa.hibernate.ddl-auto=update`).

If you need to run SQL scripts manually:
```bash
docker exec -i cbs-dashboard-postgres psql -U cbs_user -d cbs_dashboard < database/your_script.sql
```

