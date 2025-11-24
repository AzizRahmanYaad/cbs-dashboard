-- Enable pgcrypto for bcrypt hashing (run once per database)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Create default roles
INSERT INTO roles (name)
VALUES 
    ('ROLE_ADMIN'),
    ('ROLE_USER'),
    ('ROLE_TRAINING'),
    ('ROLE_DRILL_TESTING'),
    ('ROLE_INDIVIDUAL_REPORT'),
    ('ROLE_QA_LEAD'),
    ('ROLE_TESTER'),
    ('ROLE_MANAGER')
ON CONFLICT (name) DO NOTHING;

-- Create default administrator
INSERT INTO users (username, email, password, enabled, created_at)
VALUES (
    'admin',
    'admin@cbsdashboard.com',
    crypt('Admin@123', gen_salt('bf')),
    true,
    NOW()
)
ON CONFLICT (username) DO NOTHING;

-- Grant all roles to administrator
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON TRUE
WHERE u.username = 'admin'
ON CONFLICT DO NOTHING;

-- Example: create a daily-report-only user
INSERT INTO users (username, email, password, enabled, created_at)
VALUES (
    'daily_report_user',
    'daily@cbsdashboard.com',
    crypt('Daily@123', gen_salt('bf')),
    true,
    NOW()
)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ROLE_INDIVIDUAL_REPORT'
WHERE u.username = 'daily_report_user'
ON CONFLICT DO NOTHING;

