-- Create Daily Report Module Tables
-- Run this script in your PostgreSQL database to create all required tables

-- Main Daily Report table
CREATE TABLE IF NOT EXISTS daily_reports (
    id                      BIGSERIAL PRIMARY KEY,
    business_date           DATE NOT NULL,
    employee_id             BIGINT NOT NULL,
    cbs_end_time            TIME,
    cbs_start_time_next_day TIME,
    status                  VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    reviewed_by_id          BIGINT,
    reviewed_at             TIMESTAMP,
    review_comments         TEXT,
    reporting_line          VARCHAR(200),
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_daily_reports_employee
        FOREIGN KEY (employee_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_daily_reports_reviewed_by
        FOREIGN KEY (reviewed_by_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT uk_daily_reports_date_employee
        UNIQUE (business_date, employee_id)
);

-- Chat Communications table
CREATE TABLE IF NOT EXISTS chat_communications (
    id                  BIGSERIAL PRIMARY KEY,
    daily_report_id     BIGINT NOT NULL,
    platform            VARCHAR(50) NOT NULL,
    summary             TEXT NOT NULL,
    action_taken        TEXT,
    action_performed    TEXT,
    reference_number    VARCHAR(100),
    CONSTRAINT fk_chat_communications_report
        FOREIGN KEY (daily_report_id) REFERENCES daily_reports(id) ON DELETE CASCADE
);

-- Email Communications table
CREATE TABLE IF NOT EXISTS email_communications (
    id                  BIGSERIAL PRIMARY KEY,
    daily_report_id     BIGINT NOT NULL,
    is_internal         BOOLEAN NOT NULL DEFAULT TRUE,
    sender              VARCHAR(200) NOT NULL,
    receiver            VARCHAR(200) NOT NULL,
    subject             VARCHAR(500) NOT NULL,
    summary             TEXT NOT NULL,
    action_taken        TEXT,
    follow_up_required  BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_email_communications_report
        FOREIGN KEY (daily_report_id) REFERENCES daily_reports(id) ON DELETE CASCADE
);

-- Problem Escalations table
CREATE TABLE IF NOT EXISTS problem_escalations (
    id                  BIGSERIAL PRIMARY KEY,
    daily_report_id     BIGINT NOT NULL,
    escalated_to        VARCHAR(200) NOT NULL,
    reason              TEXT NOT NULL,
    escalation_date_time TIMESTAMP NOT NULL,
    follow_up_status    VARCHAR(100),
    comments            TEXT,
    CONSTRAINT fk_problem_escalations_report
        FOREIGN KEY (daily_report_id) REFERENCES daily_reports(id) ON DELETE CASCADE
);

-- Training & Capacity Building table
CREATE TABLE IF NOT EXISTS training_capacity_buildings (
    id                  BIGSERIAL PRIMARY KEY,
    daily_report_id     BIGINT NOT NULL,
    training_type       VARCHAR(50) NOT NULL,
    topic               VARCHAR(500) NOT NULL,
    duration            VARCHAR(100),
    skills_gained       TEXT,
    trainer_name        VARCHAR(200),
    participants        TEXT,
    CONSTRAINT fk_training_capacity_buildings_report
        FOREIGN KEY (daily_report_id) REFERENCES daily_reports(id) ON DELETE CASCADE
);

-- Project Progress Updates table
CREATE TABLE IF NOT EXISTS project_progress_updates (
    id                      BIGSERIAL PRIMARY KEY,
    daily_report_id         BIGINT NOT NULL,
    project_name            VARCHAR(200) NOT NULL,
    task_or_milestone       VARCHAR(500),
    progress_detail         TEXT NOT NULL,
    roadblocks_issues       TEXT,
    estimated_completion_date DATE,
    comments                TEXT,
    CONSTRAINT fk_project_progress_updates_report
        FOREIGN KEY (daily_report_id) REFERENCES daily_reports(id) ON DELETE CASCADE
);

-- CBS Team Activities table
CREATE TABLE IF NOT EXISTS cbs_team_activities (
    id                  BIGSERIAL PRIMARY KEY,
    daily_report_id     BIGINT NOT NULL,
    description         TEXT NOT NULL,
    branch              VARCHAR(100),
    account_number      VARCHAR(100),
    action_taken        TEXT,
    final_status        VARCHAR(100),
    activity_type       VARCHAR(100),
    CONSTRAINT fk_cbs_team_activities_report
        FOREIGN KEY (daily_report_id) REFERENCES daily_reports(id) ON DELETE CASCADE
);

-- Pending Activities table
CREATE TABLE IF NOT EXISTS pending_activities (
    id                  BIGSERIAL PRIMARY KEY,
    daily_report_id     BIGINT NOT NULL,
    title               VARCHAR(500) NOT NULL,
    description         TEXT NOT NULL,
    status              VARCHAR(100) NOT NULL,
    amount              DECIMAL(19, 2),
    follow_up_required  BOOLEAN NOT NULL DEFAULT FALSE,
    responsible_person  VARCHAR(200),
    CONSTRAINT fk_pending_activities_report
        FOREIGN KEY (daily_report_id) REFERENCES daily_reports(id) ON DELETE CASCADE
);

-- Meetings table
CREATE TABLE IF NOT EXISTS meetings (
    id                  BIGSERIAL PRIMARY KEY,
    daily_report_id     BIGINT NOT NULL,
    meeting_type        VARCHAR(100) NOT NULL,
    topic               VARCHAR(500) NOT NULL,
    summary             TEXT NOT NULL,
    action_taken        TEXT,
    next_step           TEXT,
    participants        TEXT,
    CONSTRAINT fk_meetings_report
        FOREIGN KEY (daily_report_id) REFERENCES daily_reports(id) ON DELETE CASCADE
);

-- AFPay Card Requests table
CREATE TABLE IF NOT EXISTS afpay_card_requests (
    id                      BIGSERIAL PRIMARY KEY,
    daily_report_id         BIGINT NOT NULL,
    request_type            VARCHAR(100) NOT NULL,
    requested_by            VARCHAR(200) NOT NULL,
    request_date            DATE NOT NULL,
    resolution_details      TEXT,
    supporting_document_path VARCHAR(500),
    archived_date           DATE,
    operator                VARCHAR(200),
    CONSTRAINT fk_afpay_card_requests_report
        FOREIGN KEY (daily_report_id) REFERENCES daily_reports(id) ON DELETE CASCADE
);

-- QRMIS Issues table
CREATE TABLE IF NOT EXISTS qrmis_issues (
    id                          BIGSERIAL PRIMARY KEY,
    daily_report_id             BIGINT NOT NULL,
    problem_type                VARCHAR(200) NOT NULL,
    problem_description         TEXT NOT NULL,
    solution_provided           TEXT,
    posted_by                   VARCHAR(200),
    authorized_by               VARCHAR(200),
    supporting_documents_archived VARCHAR(500),
    operator                    VARCHAR(200),
    CONSTRAINT fk_qrmis_issues_report
        FOREIGN KEY (daily_report_id) REFERENCES daily_reports(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_daily_reports_employee ON daily_reports(employee_id);
CREATE INDEX IF NOT EXISTS idx_daily_reports_business_date ON daily_reports(business_date);
CREATE INDEX IF NOT EXISTS idx_daily_reports_status ON daily_reports(status);
CREATE INDEX IF NOT EXISTS idx_daily_reports_reviewed_by ON daily_reports(reviewed_by_id);

CREATE INDEX IF NOT EXISTS idx_chat_communications_report ON chat_communications(daily_report_id);
CREATE INDEX IF NOT EXISTS idx_email_communications_report ON email_communications(daily_report_id);
CREATE INDEX IF NOT EXISTS idx_problem_escalations_report ON problem_escalations(daily_report_id);
CREATE INDEX IF NOT EXISTS idx_training_capacity_buildings_report ON training_capacity_buildings(daily_report_id);
CREATE INDEX IF NOT EXISTS idx_project_progress_updates_report ON project_progress_updates(daily_report_id);
CREATE INDEX IF NOT EXISTS idx_cbs_team_activities_report ON cbs_team_activities(daily_report_id);
CREATE INDEX IF NOT EXISTS idx_pending_activities_report ON pending_activities(daily_report_id);
CREATE INDEX IF NOT EXISTS idx_meetings_report ON meetings(daily_report_id);
CREATE INDEX IF NOT EXISTS idx_afpay_card_requests_report ON afpay_card_requests(daily_report_id);
CREATE INDEX IF NOT EXISTS idx_qrmis_issues_report ON qrmis_issues(daily_report_id);

-- Verify tables were created
SELECT 
    table_name 
FROM 
    information_schema.tables 
WHERE 
    table_schema = 'public' 
    AND table_name LIKE '%daily%' OR table_name LIKE '%chat%' OR table_name LIKE '%email%' 
    OR table_name LIKE '%problem%' OR table_name LIKE '%training%' OR table_name LIKE '%project%'
    OR table_name LIKE '%cbs_team%' OR table_name LIKE '%pending%' OR table_name LIKE '%meeting%'
    OR table_name LIKE '%afpay%' OR table_name LIKE '%qrmis%'
ORDER BY table_name;

