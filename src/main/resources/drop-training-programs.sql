-- Drop training_programs table and related foreign key constraints
-- Run this script to completely remove the training_programs table

-- Drop foreign key constraints first
ALTER TABLE training_sessions DROP FOREIGN KEY IF EXISTS fk_training_sessions_program;
ALTER TABLE enrollments DROP FOREIGN KEY IF EXISTS fk_enrollments_program;
ALTER TABLE training_materials DROP FOREIGN KEY IF EXISTS fk_training_materials_program;
ALTER TABLE assessments DROP FOREIGN KEY IF EXISTS fk_assessments_program;

-- Drop the training_programs table
DROP TABLE IF EXISTS training_programs;
