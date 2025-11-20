-- Fix Table Ownership for Daily Report Tables
-- Run this script as postgres user to transfer ownership to cbs_user

-- Change ownership of all daily report tables to cbs_user
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

-- Also change ownership of sequences (for auto-increment IDs)
ALTER SEQUENCE daily_reports_id_seq OWNER TO cbs_user;
ALTER SEQUENCE chat_communications_id_seq OWNER TO cbs_user;
ALTER SEQUENCE email_communications_id_seq OWNER TO cbs_user;
ALTER SEQUENCE problem_escalations_id_seq OWNER TO cbs_user;
ALTER SEQUENCE training_capacity_buildings_id_seq OWNER TO cbs_user;
ALTER SEQUENCE project_progress_updates_id_seq OWNER TO cbs_user;
ALTER SEQUENCE cbs_team_activities_id_seq OWNER TO cbs_user;
ALTER SEQUENCE pending_activities_id_seq OWNER TO cbs_user;
ALTER SEQUENCE meetings_id_seq OWNER TO cbs_user;
ALTER SEQUENCE afpay_card_requests_id_seq OWNER TO cbs_user;
ALTER SEQUENCE qrmis_issues_id_seq OWNER TO cbs_user;

-- Grant all privileges to cbs_user (in case ownership change doesn't work)
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO cbs_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO cbs_user;

-- Grant usage on schema
GRANT USAGE ON SCHEMA public TO cbs_user;

-- Verify ownership
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

