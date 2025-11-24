-- Step 1: Check if the user exists
SELECT id, username, email FROM users WHERE username = 'username_here';

-- Step 2: Check if the role exists
SELECT id, name FROM roles WHERE name = 'ROLE_INDIVIDUAL_REPORT';

-- Step 3: Check if the user already has this role
SELECT u.id as user_id, u.username, r.id as role_id, r.name as role_name
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'username_here'
  AND r.name = 'ROLE_INDIVIDUAL_REPORT';

-- Step 4: If user and role exist but no association, use this simpler INSERT
-- Replace 'username_here' with the actual username
-- Replace the role_id with the actual role ID from step 2
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'username_here'
  AND r.name = 'ROLE_INDIVIDUAL_REPORT'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

-- Alternative: Direct INSERT using IDs (if you know the user_id and role_id)
-- INSERT INTO user_roles (user_id, role_id) VALUES (user_id_here, role_id_here)
-- ON CONFLICT DO NOTHING;

