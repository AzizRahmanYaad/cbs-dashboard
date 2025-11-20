-- Fix script for daily_reports table
-- Run this if you get "column employee_id does not exist" error

-- First, drop the table if it exists (this will also drop all related tables)
DROP TABLE IF EXISTS qrmis_issues CASCADE;
DROP TABLE IF EXISTS afpay_card_requests CASCADE;
DROP TABLE IF EXISTS meetings CASCADE;
DROP TABLE IF EXISTS pending_activities CASCADE;
DROP TABLE IF EXISTS cbs_team_activities CASCADE;
DROP TABLE IF EXISTS project_progress_updates CASCADE;
DROP TABLE IF EXISTS training_capacity_buildings CASCADE;
DROP TABLE IF EXISTS problem_escalations CASCADE;
DROP TABLE IF EXISTS email_communications CASCADE;
DROP TABLE IF EXISTS chat_communications CASCADE;
DROP TABLE IF EXISTS daily_reports CASCADE;

-- Now create all tables with correct schema
CREATE TABLE daily_reports (
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
    created_at              TIMESTAMP,
    updated_at              TIMESTAMP,
    CONSTRAINT fk_daily_reports_employee
        FOREIGN KEY (employee_id) REFERENCES users(id),
    CONSTRAINT fk_daily_reports_reviewed_by
        FOREIGN KEY (reviewed_by_id) REFERENCES users(id),
    CONSTRAINT uk_daily_reports_date_employee
        UNIQUE (business_date, employee_id)
);

CREATE TABLE chat_communications (
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

CREATE TABLE email_communications (
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

CREATE TABLE problem_escalations (
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

CREATE TABLE training_capacity_buildings (
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

CREATE TABLE project_progress_updates (
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

CREATE TABLE cbs_team_activities (
    id                  BIGSERIAL PRIMARY KEY,
    daily_report_id      BIGINT NOT NULL,
    description         TEXT NOT NULL,
    branch              VARCHAR(100),
    account_number      VARCHAR(100),
    action_taken        TEXT,
    final_status        VARCHAR(100),
    activity_type       VARCHAR(100),
    CONSTRAINT fk_cbs_team_activities_report
        FOREIGN KEY (daily_report_id) REFERENCES daily_reports(id) ON DELETE CASCADE
);

CREATE TABLE pending_activities (
    id                  BIGSERIAL PRIMARY KEY,
    daily_report_id     BIGINT NOT NULL,
    title               VARCHAR(500) NOT NULL,
    description         TEXT NOT NULL,
    status              VARCHAR(100) NOT NULL,
    amount              DECIMAL(19, 2),
    follow_up_required  BOOLEAN NOT NULL DEFAULT FALSE,
    responsible_person   VARCHAR(200),
    CONSTRAINT fk_pending_activities_report
        FOREIGN KEY (daily_report_id) REFERENCES daily_reports(id) ON DELETE CASCADE
);

CREATE TABLE meetings (
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

CREATE TABLE afpay_card_requests (
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

CREATE TABLE qrmis_issues (
    id                          BIGSERIAL PRIMARY KEY,
    daily_report_id             BIGINT NOT NULL,
    problem_type                VARCHAR(200) NOT NULL,
    problem_description         TEXT NOT NULL,
    solution_provided           TEXT,
    posted_by                   VARCHAR(200),
    authorized_by                VARCHAR(200),
    supporting_documents_archived VARCHAR(500),
    operator                    VARCHAR(200),
    CONSTRAINT fk_qrmis_issues_report
        FOREIGN KEY (daily_report_id) REFERENCES daily_reports(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_daily_reports_employee ON daily_reports(employee_id);
CREATE INDEX idx_daily_reports_business_date ON daily_reports(business_date);
CREATE INDEX idx_daily_reports_status ON daily_reports(status);
CREATE INDEX idx_chat_communications_report ON chat_communications(daily_report_id);
CREATE INDEX idx_email_communications_report ON email_communications(daily_report_id);
CREATE INDEX idx_problem_escalations_report ON problem_escalations(daily_report_id);
CREATE INDEX idx_training_capacity_buildings_report ON training_capacity_buildings(daily_report_id);
CREATE INDEX idx_project_progress_updates_report ON project_progress_updates(daily_report_id);
CREATE INDEX idx_cbs_team_activities_report ON cbs_team_activities(daily_report_id);
CREATE INDEX idx_pending_activities_report ON pending_activities(daily_report_id);
CREATE INDEX idx_meetings_report ON meetings(daily_report_id);
CREATE INDEX idx_afpay_card_requests_report ON afpay_card_requests(daily_report_id);
CREATE INDEX idx_qrmis_issues_report ON qrmis_issues(daily_report_id);

