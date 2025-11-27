-- Simple Script to Delete All Daily Report Data
-- This is a simplified version that relies on CASCADE DELETE
-- All child tables will be automatically deleted when daily_reports is deleted

-- WARNING: This will permanently delete ALL daily report data!
-- User data will NOT be affected

-- Delete all daily reports (CASCADE will automatically delete all related records)
DELETE FROM daily_reports;

-- Verify deletion
SELECT 
    'daily_reports' as table_name, 
    COUNT(*) as remaining_records 
FROM daily_reports;

-- Verify users are safe
SELECT 
    COUNT(*) as total_users,
    'Users table is safe' as status
FROM users;



