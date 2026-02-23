-- Ensure ROLE_CFO and ROLE_QUALITY_CONTROL exist for User Management.
-- Run this if the CFO role does not appear when creating/editing users.
-- Usage: psql -U cbs_user -d cbs_dashboard -f insert_cfo_role.sql

INSERT INTO roles (name) VALUES ('ROLE_CFO')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name) VALUES ('ROLE_QUALITY_CONTROL')
ON CONFLICT (name) DO NOTHING;

-- Verify
SELECT id, name FROM roles WHERE name IN ('ROLE_CFO', 'ROLE_QUALITY_CONTROL');
