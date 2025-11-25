-- Migration: Add full_name column to users table
-- This script adds a full_name column to store the user's full name

-- Add full_name column to users table
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS full_name VARCHAR(200);

-- Add comment to the column
COMMENT ON COLUMN users.full_name IS 'Full name of the user';

-- Update existing users to use username as full_name if full_name is null (optional)
-- UPDATE users SET full_name = username WHERE full_name IS NULL;

