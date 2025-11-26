-- Delete All Records from Daily Reports Module
-- WARNING: This will permanently delete ALL daily reports and all related data
-- Use with caution! This operation cannot be undone.

-- Since all related tables have ON DELETE CASCADE foreign keys,
-- deleting from daily_reports will automatically delete all related records in:
--   - chat_communications
--   - email_communications
--   - problem_escalations
--   - training_capacity_buildings
--   - project_progress_updates
--   - cbs_team_activities
--   - pending_activities
--   - meetings
--   - afpay_card_requests
--   - qrmis_issues

-- Option 1: Delete all daily reports (recommended - uses CASCADE)
DELETE FROM daily_reports;

-- Option 2: If you want to delete from each table explicitly (not necessary due to CASCADE)
-- Uncomment the following if you prefer explicit deletion:
/*
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
DELETE FROM daily_reports;
*/

-- Verify deletion (should return 0 rows)
SELECT COUNT(*) as remaining_daily_reports FROM daily_reports;
SELECT COUNT(*) as remaining_chat_communications FROM chat_communications;
SELECT COUNT(*) as remaining_email_communications FROM email_communications;
SELECT COUNT(*) as remaining_problem_escalations FROM problem_escalations;
SELECT COUNT(*) as remaining_training_capacity_buildings FROM training_capacity_buildings;
SELECT COUNT(*) as remaining_project_progress_updates FROM project_progress_updates;
SELECT COUNT(*) as remaining_cbs_team_activities FROM cbs_team_activities;
SELECT COUNT(*) as remaining_pending_activities FROM pending_activities;
SELECT COUNT(*) as remaining_meetings FROM meetings;
SELECT COUNT(*) as remaining_afpay_card_requests FROM afpay_card_requests;
SELECT COUNT(*) as remaining_qrmis_issues FROM qrmis_issues;

