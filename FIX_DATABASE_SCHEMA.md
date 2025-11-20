# Fix Database Schema Issue

## Problem
You're getting the error: `column dr1_0.employee_id does not exist`

This means the `daily_reports` table exists but is missing the `employee_id` column, or the table structure is incorrect.

## Solution Options

### Option 1: Run the Fix Script (Recommended)

1. Connect to your PostgreSQL database:
```bash
psql -U cbs_user -d cbs_dashboard
```

2. Run the fix script:
```bash
\i database/fix_daily_reports_schema.sql
```

Or manually copy and paste the contents of `database/fix_daily_reports_schema.sql` into your PostgreSQL client.

### Option 2: Let Hibernate Recreate Tables

1. Temporarily change `application.properties`:
```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

2. Restart the application (this will drop and recreate all tables - **WARNING: This will delete all data**)

3. Change it back to:
```properties
spring.jpa.hibernate.ddl-auto=update
```

4. Restart again

### Option 3: Manually Add the Column

If you want to keep existing data, you can manually add the missing column:

```sql
-- Connect to database
psql -U cbs_user -d cbs_dashboard

-- Add the missing column
ALTER TABLE daily_reports ADD COLUMN IF NOT EXISTS employee_id BIGINT;

-- Add foreign key constraint
ALTER TABLE daily_reports 
ADD CONSTRAINT fk_daily_reports_employee 
FOREIGN KEY (employee_id) REFERENCES users(id);

-- Make it NOT NULL (if you have data, you'll need to populate it first)
-- First, set a default value for existing rows
UPDATE daily_reports SET employee_id = (SELECT id FROM users LIMIT 1) WHERE employee_id IS NULL;

-- Then make it NOT NULL
ALTER TABLE daily_reports ALTER COLUMN employee_id SET NOT NULL;
```

## After Fixing

1. Restart your Spring Boot application
2. Try creating a daily report again
3. The error should be resolved

## Verification

To verify the table structure is correct, run:

```sql
\d daily_reports
```

You should see `employee_id` column listed.

