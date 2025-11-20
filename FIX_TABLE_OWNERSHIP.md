# Fix Table Ownership Issue

## Problem
The Daily Report tables were created but are owned by `postgres` user instead of `cbs_user`. This can cause permission issues when the application tries to access them.

## Solution Options

### Option 1: Change Ownership to cbs_user (Recommended)

1. Connect to PostgreSQL as the `postgres` superuser:
```bash
psql -U postgres -d cbs_dashboard
```

2. Run the ownership fix script:
```bash
\i database/fix_table_ownership.sql
```

Or copy and paste the contents of `database/fix_table_ownership.sql` into your PostgreSQL client.

This will:
- Change ownership of all 11 tables to `cbs_user`
- Change ownership of all sequences to `cbs_user`
- Grant all necessary privileges

### Option 2: Grant Permissions Without Changing Ownership

If you prefer to keep `postgres` as the owner but give `cbs_user` full access:

1. Connect as `postgres`:
```bash
psql -U postgres -d cbs_dashboard
```

2. Run the permissions script:
```bash
\i database/grant_permissions.sql
```

### Option 3: Quick Manual Fix

Connect as `postgres` and run these commands:

```sql
-- Change ownership
ALTER TABLE daily_reports OWNER TO cbs_user;
ALTER TABLE chat_communications OWNER TO cbs_user;
ALTER TABLE email_communications OWNER TO cbs_user;
ALTER TABLE problem_escalations OWNER TO cbs_user;
ALTER TABLE training_capacity_buildings OWNER TO cbs_user;
ALTER TABLE project_progress_updates OWNER TO cbs_user;
ALTER TABLE cbs_team_activities OWNER TO cbs_user;
ALTER TABLE pending_activities OWNER TO cbs_user;
ALTER TABLE meetings OWNER TO cbs_user;
ALTER TABLE afpay_card_requests OWNER TO cbs_user;
ALTER TABLE qrmis_issues OWNER TO cbs_user;

-- Grant all privileges
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO cbs_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO cbs_user;
```

## Verify the Fix

After running the script, verify ownership:

```sql
SELECT 
    schemaname,
    tablename,
    tableowner
FROM 
    pg_tables
WHERE 
    schemaname = 'public'
    AND (
        tablename LIKE '%daily%' 
        OR tablename LIKE '%chat%' 
        OR tablename LIKE '%email%' 
        OR tablename LIKE '%problem%' 
        OR tablename LIKE '%training%' 
        OR tablename LIKE '%project%'
        OR tablename LIKE '%cbs_team%' 
        OR tablename LIKE '%pending%' 
        OR tablename LIKE '%meeting%'
        OR tablename LIKE '%afpay%' 
        OR tablename LIKE '%qrmis%'
    )
ORDER BY tablename;
```

All tables should show `cbs_user` as the owner (if you used Option 1).

## Test the Application

1. Restart your Spring Boot application
2. Try creating a daily report
3. It should work now!

## Troubleshooting

### If you get "permission denied" errors:

Make sure you're running the script as the `postgres` superuser, not as `cbs_user`. Only superusers can change ownership.

### If ownership change doesn't work:

Use Option 2 (grant permissions) instead. This gives `cbs_user` full access without changing ownership.

### If sequences are missing:

The sequences are automatically created when you create tables with `BIGSERIAL`. If they don't exist, you can create them manually or just grant privileges on all sequences:

```sql
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO cbs_user;
```

