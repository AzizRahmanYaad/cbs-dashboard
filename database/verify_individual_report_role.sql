-- Verification Script: Check ROLE_INDIVIDUAL_REPORT setup
-- Run this to verify the role exists and is properly assigned

-- 1. Check if the role exists
SELECT 'Role Check' as check_type, id, name 
FROM roles 
WHERE name = 'ROLE_INDIVIDUAL_REPORT';

-- 2. Check which users have this role
SELECT 'Users with Role' as check_type, u.id, u.username, u.email, r.name as role_name
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE r.name = 'ROLE_INDIVIDUAL_REPORT'
ORDER BY u.username;

-- 3. Check all Daily Report related roles (should only be ROLE_INDIVIDUAL_REPORT)
SELECT 'All Daily Report Roles' as check_type, id, name 
FROM roles 
WHERE name LIKE '%DAILY%' OR name LIKE '%REPORT%' OR name LIKE '%INDIVIDUAL%'
ORDER BY name;

-- 4. Count total roles
SELECT 'Total Roles Count' as check_type, COUNT(*) as total_roles FROM roles;

-- 5. Count users with ROLE_INDIVIDUAL_REPORT
SELECT 'Users Count with Role' as check_type, COUNT(DISTINCT u.id) as user_count
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE r.name = 'ROLE_INDIVIDUAL_REPORT';

