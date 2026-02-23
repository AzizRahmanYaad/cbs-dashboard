-- Delete ALL training session records so you can insert new ones.
--
-- Run (choose one):
--   From project root:
--     psql -U cbs_user -d cbs_dashboard -f database/delete_all_training_sessions.sql
--   If DB is in Docker:
--     docker exec -i cbs-dashboard-postgres psql -U cbs_user -d cbs_dashboard < database/delete_all_training_sessions.sql

-- 1. Delete attendances (they reference session_id)
DELETE FROM attendances;

-- 2. Unlink enrollments from sessions (enrollments stay; only session_id is cleared)
UPDATE enrollments SET session_id = NULL WHERE session_id IS NOT NULL;

-- 3. Delete all training sessions
DELETE FROM training_sessions;

-- Verify
SELECT 'training_sessions' AS table_name, COUNT(*) AS remaining FROM training_sessions
UNION ALL
SELECT 'attendances', COUNT(*) FROM attendances
UNION ALL
SELECT 'enrollments (session_id set)', COUNT(*) FROM enrollments WHERE session_id IS NOT NULL;
