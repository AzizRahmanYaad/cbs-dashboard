-- Simple INSERT query to add ROLE_INDIVIDUAL_REPORT role
-- Run this query in your database

INSERT INTO roles (name)
VALUES ('ROLE_INDIVIDUAL_REPORT')
ON CONFLICT (name) DO NOTHING;

-- Verify the role was added
SELECT id, name FROM roles WHERE name = 'ROLE_INDIVIDUAL_REPORT';

