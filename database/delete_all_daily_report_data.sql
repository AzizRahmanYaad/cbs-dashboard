-- Script to Delete All Daily Report Data
-- This script will delete ALL records from daily report tables
-- WARNING: This will permanently delete all daily report data!
-- User data will NOT be affected

-- Step 1: Disable foreign key checks temporarily (PostgreSQL doesn't need this, but included for safety)
-- PostgreSQL handles CASCADE automatically

-- Step 2: Delete all data from child tables first (optional, CASCADE will handle it)
-- But we'll do it explicitly to be clear about what's being deleted

-- Delete from all activity/communication tables
DELETE FROM chat_communications;
DELETE FROM email_communications;
DELETE FROM problem_escalations;
DELETE FROM training_capacity_buildings;
DELETE FROM project_progress_updates;
DELETE FROM cbs_team_activities;
DELETE FROM pending_activities;
DELETE FROM meetings;
DELETE FROM afpay_card_requests;
DELETE FROM qrmis_issues;

-- Step 3: Delete all daily reports (this will also cascade delete any remaining child records)
DELETE FROM daily_reports;

-- Step 4: Reset sequences (optional - to reset auto-increment counters)
-- Uncomment the lines below if you want to reset the ID sequences to start from 1

-- ALTER SEQUENCE daily_reports_id_seq RESTART WITH 1;
-- ALTER SEQUENCE chat_communications_id_seq RESTART WITH 1;
-- ALTER SEQUENCE email_communications_id_seq RESTART WITH 1;
-- ALTER SEQUENCE problem_escalations_id_seq RESTART WITH 1;
-- ALTER SEQUENCE training_capacity_buildings_id_seq RESTART WITH 1;
-- ALTER SEQUENCE project_progress_updates_id_seq RESTART WITH 1;
-- ALTER SEQUENCE cbs_team_activities_id_seq RESTART WITH 1;
-- ALTER SEQUENCE pending_activities_id_seq RESTART WITH 1;
-- ALTER SEQUENCE meetings_id_seq RESTART WITH 1;
-- ALTER SEQUENCE afpay_card_requests_id_seq RESTART WITH 1;
-- ALTER SEQUENCE qrmis_issues_id_seq RESTART WITH 1;

-- Step 5: Verify deletion (check that all tables are empty)
SELECT 
    'daily_reports' as table_name, 
    COUNT(*) as remaining_records 
FROM daily_reports
UNION ALL
SELECT 'chat_communications', COUNT(*) FROM chat_communications
UNION ALL
SELECT 'email_communications', COUNT(*) FROM email_communications
UNION ALL
SELECT 'problem_escalations', COUNT(*) FROM problem_escalations
UNION ALL
SELECT 'training_capacity_buildings', COUNT(*) FROM training_capacity_buildings
UNION ALL
SELECT 'project_progress_updates', COUNT(*) FROM project_progress_updates
UNION ALL
SELECT 'cbs_team_activities', COUNT(*) FROM cbs_team_activities
UNION ALL
SELECT 'pending_activities', COUNT(*) FROM pending_activities
UNION ALL
SELECT 'meetings', COUNT(*) FROM meetings
UNION ALL
SELECT 'afpay_card_requests', COUNT(*) FROM afpay_card_requests
UNION ALL
SELECT 'qrmis_issues', COUNT(*) FROM qrmis_issues
ORDER BY table_name;

-- Step 6: Verify users table is intact (should show all users)
SELECT 
    'users' as table_name,
    COUNT(*) as user_count,
    'Users table is safe' as status
FROM users;



