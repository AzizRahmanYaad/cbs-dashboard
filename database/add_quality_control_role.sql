-- Add Quality Control Role to the system
-- This role allows users to view, review, and manage all submitted individual reports

-- Insert the Quality Control role if it doesn't exist
INSERT INTO roles (name, description, created_at, updated_at)
SELECT 'ROLE_QUALITY_CONTROL', 'Quality Control - Can view, review, and manage all submitted individual reports', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'ROLE_QUALITY_CONTROL'
);

-- Verify the role was created
SELECT id, name, description FROM roles WHERE name = 'ROLE_QUALITY_CONTROL';

-- To assign this role to a user, use:
-- INSERT INTO user_roles (user_id, role_id)
-- SELECT u.id, r.id
-- FROM users u, roles r
-- WHERE u.username = 'username_here' AND r.name = 'ROLE_QUALITY_CONTROL'
-- AND NOT EXISTS (
--     SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id
-- );

