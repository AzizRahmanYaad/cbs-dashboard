-- Add ROLE_CFO for CFO - Training Oversight & Financial Governance
-- Exclusive read-only executive analytics access

INSERT INTO roles (name)
VALUES ('ROLE_CFO')
ON CONFLICT (name) DO NOTHING;

-- Verify
SELECT id, name FROM roles WHERE name = 'ROLE_CFO';
