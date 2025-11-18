-- Schema bootstrap for test management module
-- Creates tables if they do not already exist (PostgreSQL)

CREATE TABLE IF NOT EXISTS test_modules (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    description     VARCHAR(1000),
    created_by_id   BIGINT NOT NULL,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    CONSTRAINT fk_test_modules_created_by
        FOREIGN KEY (created_by_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS test_cases (
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(500) NOT NULL,
    preconditions    TEXT,
    expected_result  TEXT,
    priority         VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    status           VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    module_id        BIGINT,
    created_by_id    BIGINT NOT NULL,
    assigned_to_id   BIGINT,
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP,
    CONSTRAINT fk_test_cases_module
        FOREIGN KEY (module_id) REFERENCES test_modules(id) ON DELETE SET NULL,
    CONSTRAINT fk_test_cases_created_by
        FOREIGN KEY (created_by_id) REFERENCES users(id),
    CONSTRAINT fk_test_cases_assigned_to
        FOREIGN KEY (assigned_to_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS test_case_steps (
    test_case_id BIGINT NOT NULL,
    step_order   INTEGER NOT NULL,
    step         TEXT NOT NULL,
    PRIMARY KEY (test_case_id, step_order),
    CONSTRAINT fk_test_case_steps_case
        FOREIGN KEY (test_case_id) REFERENCES test_cases(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS test_executions (
    id             BIGSERIAL PRIMARY KEY,
    test_case_id   BIGINT NOT NULL,
    executed_by_id BIGINT NOT NULL,
    status         VARCHAR(50) NOT NULL,
    comments       TEXT,
    executed_at    TIMESTAMP,
    updated_at     TIMESTAMP,
    CONSTRAINT fk_test_executions_case
        FOREIGN KEY (test_case_id) REFERENCES test_cases(id) ON DELETE CASCADE,
    CONSTRAINT fk_test_executions_user
        FOREIGN KEY (executed_by_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS execution_attachments (
    execution_id BIGINT NOT NULL,
    file_path    VARCHAR(500) NOT NULL,
    CONSTRAINT fk_execution_attachments_execution
        FOREIGN KEY (execution_id) REFERENCES test_executions(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS defects (
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(500) NOT NULL,
    description      TEXT,
    severity         VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    status           VARCHAR(50) NOT NULL DEFAULT 'NEW',
    test_case_id     BIGINT,
    test_execution_id BIGINT,
    reported_by_id   BIGINT NOT NULL,
    assigned_to_id   BIGINT,
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP,
    CONSTRAINT fk_defects_case
        FOREIGN KEY (test_case_id) REFERENCES test_cases(id) ON DELETE SET NULL,
    CONSTRAINT fk_defects_execution
        FOREIGN KEY (test_execution_id) REFERENCES test_executions(id) ON DELETE SET NULL,
    CONSTRAINT fk_defects_reporter
        FOREIGN KEY (reported_by_id) REFERENCES users(id),
    CONSTRAINT fk_defects_assignee
        FOREIGN KEY (assigned_to_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS defect_attachments (
    defect_id BIGINT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    CONSTRAINT fk_defect_attachments_defect
        FOREIGN KEY (defect_id) REFERENCES defects(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comments (
    id             BIGSERIAL PRIMARY KEY,
    content        TEXT NOT NULL,
    created_by_id  BIGINT NOT NULL,
    test_case_id   BIGINT,
    defect_id      BIGINT,
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP,
    CONSTRAINT fk_comments_user
        FOREIGN KEY (created_by_id) REFERENCES users(id),
    CONSTRAINT fk_comments_case
        FOREIGN KEY (test_case_id) REFERENCES test_cases(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_defect
        FOREIGN KEY (defect_id) REFERENCES defects(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    entity_id   BIGINT NOT NULL,
    action      VARCHAR(50) NOT NULL,
    user_id     BIGINT NOT NULL,
    old_value   TEXT,
    new_value   TEXT,
    description TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_test_cases_module ON test_cases(module_id);
CREATE INDEX IF NOT EXISTS idx_test_cases_assigned_to ON test_cases(assigned_to_id);
CREATE INDEX IF NOT EXISTS idx_test_executions_case ON test_executions(test_case_id);
CREATE INDEX IF NOT EXISTS idx_defects_case ON defects(test_case_id);
CREATE INDEX IF NOT EXISTS idx_comments_case ON comments(test_case_id);
CREATE INDEX IF NOT EXISTS idx_comments_defect ON comments(defect_id);

