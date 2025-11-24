-- Migration Script: Remove old Daily Report roles and add ROLE_INDIVIDUAL_REPORT
-- This script should be run to update existing databases

-- Step 1: Remove all user-role associations for old Daily Report roles
-- This prevents foreign key constraint violations when deleting roles
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

-- Step 2: Delete old Daily Report roles from the roles table
DELETE FROM roles
WHERE name IN (
    'ROLE_DAILY_REPORT',
    'ROLE_DAILY_REPORT_EMPLOYEE',
    'ROLE_DAILY_REPORT_SUPERVISOR',
    'ROLE_DAILY_REPORT_DIRECTOR',
    'ROLE_DAILY_REPORT_MANAGER',
    'ROLE_DAILY_REPORT_TEAM_LEAD'
);

-- Step 3: Add the new ROLE_INDIVIDUAL_REPORT role if it doesn't exist
INSERT INTO roles (name)
VALUES ('ROLE_INDIVIDUAL_REPORT')
ON CONFLICT (name) DO NOTHING;

-- Step 4: (Optional) Grant ROLE_INDIVIDUAL_REPORT to users who previously had any Daily Report role
-- This migrates existing users to the new role structure
-- Uncomment the following if you want to automatically assign the new role to affected users

/*
INSERT INTO user_roles (user_id, role_id)
SELECT DISTINCT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE r.name = 'ROLE_INDIVIDUAL_REPORT'
  AND u.id NOT IN (
      SELECT user_id 
      FROM user_roles ur
      JOIN roles r2 ON ur.role_id = r2.id
      WHERE r2.name = 'ROLE_INDIVIDUAL_REPORT'
  )
  AND EXISTS (
      -- Only migrate users who had at least one Daily Report role before
      SELECT 1 FROM user_roles ur2
      JOIN roles r3 ON ur2.role_id = r3.id
      WHERE ur2.user_id = u.id
        AND r3.name IN (
            'ROLE_DAILY_REPORT',
            'ROLE_DAILY_REPORT_EMPLOYEE',
            'ROLE_DAILY_REPORT_SUPERVISOR',
            'ROLE_DAILY_REPORT_DIRECTOR',
            'ROLE_DAILY_REPORT_MANAGER',
            'ROLE_DAILY_REPORT_TEAM_LEAD'
        )
  )
ON CONFLICT DO NOTHING;
*/

-- Verification: Check remaining roles
SELECT name FROM roles WHERE name LIKE '%DAILY%' OR name LIKE '%REPORT%' ORDER BY name;

-- Verification: Count users with ROLE_INDIVIDUAL_REPORT
SELECT COUNT(DISTINCT u.id) as users_with_individual_report_role
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE r.name = 'ROLE_INDIVIDUAL_REPORT';

