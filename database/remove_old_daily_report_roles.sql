-- Simple Script: Remove old Daily Report roles and ensure ROLE_INDIVIDUAL_REPORT exists
-- Run this script to clean up old roles and add the new one

BEGIN;

-- Step 1: Remove user-role associations for old Daily Report roles
DELETE FROM user_roles
WHERE role_id IN (
    SELECT id FROM roles 
    WHERE name IN (
        'ROLE_DAILY_REPORT',
        'ROLE_DAILY_REPORT_EMPLOYEE',
        'ROLE_DAILY_REPORT_SUPERVISOR',
        'ROLE_DAILY_REPORT_DIRECTOR',
        'ROLE_DAILY_REPORT_MANAGER',
        'ROLE_DAILY_REPORT_TEAM_LEAD'
    )
);

-- Step 2: Delete old Daily Report roles
DELETE FROM roles
WHERE name IN (
    'ROLE_DAILY_REPORT',
    'ROLE_DAILY_REPORT_EMPLOYEE',
    'ROLE_DAILY_REPORT_SUPERVISOR',
    'ROLE_DAILY_REPORT_DIRECTOR',
    'ROLE_DAILY_REPORT_MANAGER',
    'ROLE_DAILY_REPORT_TEAM_LEAD'
);

-- Step 3: Add the new ROLE_INDIVIDUAL_REPORT role
INSERT INTO roles (name)
VALUES ('ROLE_INDIVIDUAL_REPORT')
ON CONFLICT (name) DO NOTHING;

COMMIT;

-- Verification queries (optional - run these to verify)
-- SELECT name FROM roles WHERE name LIKE '%DAILY%' OR name LIKE '%REPORT%' ORDER BY name;
-- SELECT COUNT(*) as total_individual_report_role FROM roles WHERE name = 'ROLE_INDIVIDUAL_REPORT';

