# Create Daily Report Tables - Instructions

## Problem
The Daily Report tables don't exist in your database yet.

## Solution: Run the SQL Script

### Method 1: Using psql Command Line

1. Open your terminal/command prompt

2. Connect to your PostgreSQL database:
```bash
psql -U cbs_user -d cbs_dashboard
```

3. Enter your password when prompted

4. Run the SQL script:
```bash
\i database/create_daily_report_tables.sql
```

Or if you're in a different directory:
```bash
\i /full/path/to/database/create_daily_report_tables.sql
```

### Method 2: Using pgAdmin or Another GUI Tool

1. Open pgAdmin (or your preferred PostgreSQL GUI tool)

2. Connect to your `cbs_dashboard` database

3. Open the Query Tool

4. Open the file `database/create_daily_report_tables.sql`

5. Copy and paste the entire contents into the query editor

6. Click "Execute" or press F5

### Method 3: Copy-Paste Directly

1. Open the file `database/create_daily_report_tables.sql`

2. Copy all the SQL statements

3. Connect to your database using any PostgreSQL client

4. Paste and execute the SQL

### Method 4: Let Hibernate Create Tables (Alternative)

If you prefer to let Hibernate create the tables automatically:

1. Open `src/main/resources/application.properties`

2. Temporarily change:
```properties
spring.jpa.hibernate.ddl-auto=create
```

3. Start your Spring Boot application

4. Hibernate will create all tables based on your entities

5. Change it back to:
```properties
spring.jpa.hibernate.ddl-auto=update
```

**Note:** This method will drop and recreate ALL tables if they exist, so use with caution if you have existing data.

## Verify Tables Were Created

After running the script, verify the tables exist:

```sql
-- List all daily report related tables
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
  AND (
    table_name LIKE '%daily%' 
    OR table_name LIKE '%chat%' 
    OR table_name LIKE '%email%' 
    OR table_name LIKE '%problem%' 
    OR table_name LIKE '%training%' 
    OR table_name LIKE '%project%'
    OR table_name LIKE '%cbs_team%' 
    OR table_name LIKE '%pending%' 
    OR table_name LIKE '%meeting%'
    OR table_name LIKE '%afpay%' 
    OR table_name LIKE '%qrmis%'
  )
ORDER BY table_name;
```

You should see these 11 tables:
- daily_reports
- chat_communications
- email_communications
- problem_escalations
- training_capacity_buildings
- project_progress_updates
- cbs_team_activities
- pending_activities
- meetings
- afpay_card_requests
- qrmis_issues

## After Creating Tables

1. Restart your Spring Boot application

2. Try creating a daily report again

3. The error should be resolved!

## Troubleshooting

### If you get "relation users does not exist" error:
Make sure the `users` table exists first. The daily_reports table has a foreign key to users.

### If you get "permission denied" error:
Make sure your database user has CREATE TABLE permissions:
```sql
GRANT ALL PRIVILEGES ON DATABASE cbs_dashboard TO cbs_user;
```

### If tables still don't appear:
- Check you're connected to the correct database
- Check for any error messages when running the script
- Try running each CREATE TABLE statement individually to identify which one fails

