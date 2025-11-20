-- Alternative: Grant Permissions Without Changing Ownership
-- Use this if you want to keep postgres as owner but give cbs_user full access

-- Grant all privileges on tables
GRANT ALL PRIVILEGES ON TABLE daily_reports TO cbs_user;
GRANT ALL PRIVILEGES ON TABLE chat_communications TO cbs_user;
GRANT ALL PRIVILEGES ON TABLE email_communications TO cbs_user;
GRANT ALL PRIVILEGES ON TABLE problem_escalations TO cbs_user;
GRANT ALL PRIVILEGES ON TABLE training_capacity_buildings TO cbs_user;
GRANT ALL PRIVILEGES ON TABLE project_progress_updates TO cbs_user;
GRANT ALL PRIVILEGES ON TABLE cbs_team_activities TO cbs_user;
GRANT ALL PRIVILEGES ON TABLE pending_activities TO cbs_user;
GRANT ALL PRIVILEGES ON TABLE meetings TO cbs_user;
GRANT ALL PRIVILEGES ON TABLE afpay_card_requests TO cbs_user;
GRANT ALL PRIVILEGES ON TABLE qrmis_issues TO cbs_user;

-- Grant privileges on sequences (for auto-increment)
GRANT ALL PRIVILEGES ON SEQUENCE daily_reports_id_seq TO cbs_user;
GRANT ALL PRIVILEGES ON SEQUENCE chat_communications_id_seq TO cbs_user;
GRANT ALL PRIVILEGES ON SEQUENCE email_communications_id_seq TO cbs_user;
GRANT ALL PRIVILEGES ON SEQUENCE problem_escalations_id_seq TO cbs_user;
GRANT ALL PRIVILEGES ON SEQUENCE training_capacity_buildings_id_seq TO cbs_user;
GRANT ALL PRIVILEGES ON SEQUENCE project_progress_updates_id_seq TO cbs_user;
GRANT ALL PRIVILEGES ON SEQUENCE cbs_team_activities_id_seq TO cbs_user;
GRANT ALL PRIVILEGES ON SEQUENCE pending_activities_id_seq TO cbs_user;
GRANT ALL PRIVILEGES ON SEQUENCE meetings_id_seq TO cbs_user;
GRANT ALL PRIVILEGES ON SEQUENCE afpay_card_requests_id_seq TO cbs_user;
GRANT ALL PRIVILEGES ON SEQUENCE qrmis_issues_id_seq TO cbs_user;

-- Grant default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO cbs_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO cbs_user;

