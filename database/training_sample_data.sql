-- Training module sample data: 10 records per entity.
-- Run once after application has started (tables created by JPA) and after database/data.sql (admin exists).
-- Requires: admin user, ROLE_USER role. PostgreSQL with pgcrypto.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ========== 1. Student users (10) ==========
INSERT INTO users (username, full_name, email, password, enabled, created_at)
SELECT u.username, u.full_name, u.email, crypt('Student@123'::text, gen_salt('bf')), true, NOW()
FROM (VALUES
  ('student1', 'Alice Johnson', 'student1@cbsdashboard.com'),
  ('student2', 'Bob Smith', 'student2@cbsdashboard.com'),
  ('student3', 'Carol Williams', 'student3@cbsdashboard.com'),
  ('student4', 'David Brown', 'student4@cbsdashboard.com'),
  ('student5', 'Eve Davis', 'student5@cbsdashboard.com'),
  ('student6', 'Frank Miller', 'student6@cbsdashboard.com'),
  ('student7', 'Grace Lee', 'student7@cbsdashboard.com'),
  ('student8', 'Henry Wilson', 'student8@cbsdashboard.com'),
  ('student9', 'Ivy Taylor', 'student9@cbsdashboard.com'),
  ('student10', 'Jack Anderson', 'student10@cbsdashboard.com')
) AS u(username, full_name, email)
ON CONFLICT (username) DO NOTHING;

-- ========== 2. Teacher users (10) ==========
INSERT INTO users (username, full_name, email, password, enabled, created_at)
SELECT u.username, u.full_name, u.email, crypt('Teacher@123'::text, gen_salt('bf')), true, NOW()
FROM (VALUES
  ('teacher1', 'Prof. Sarah Green', 'teacher1@cbsdashboard.com'),
  ('teacher2', 'Prof. Mark Hall', 'teacher2@cbsdashboard.com'),
  ('teacher3', 'Prof. Nina King', 'teacher3@cbsdashboard.com'),
  ('teacher4', 'Prof. Owen Clark', 'teacher4@cbsdashboard.com'),
  ('teacher5', 'Prof. Paula Lewis', 'teacher5@cbsdashboard.com'),
  ('teacher6', 'Prof. Quinn Young', 'teacher6@cbsdashboard.com'),
  ('teacher7', 'Prof. Ryan Walker', 'teacher7@cbsdashboard.com'),
  ('teacher8', 'Prof. Sue Hill', 'teacher8@cbsdashboard.com'),
  ('teacher9', 'Prof. Tom Scott', 'teacher9@cbsdashboard.com'),
  ('teacher10', 'Prof. Uma White', 'teacher10@cbsdashboard.com')
) AS u(username, full_name, email)
ON CONFLICT (username) DO NOTHING;

-- ========== 3. Assign ROLE_USER to all student and teacher users ==========
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN (SELECT id FROM roles WHERE name = 'ROLE_USER' LIMIT 1) r
WHERE u.username IN (
  'student1','student2','student3','student4','student5','student6','student7','student8','student9','student10',
  'teacher1','teacher2','teacher3','teacher4','teacher5','teacher6','teacher7','teacher8','teacher9','teacher10'
)
ON CONFLICT (user_id, role_id) DO NOTHING;

-- ========== 4. Student records (student_teachers, type STUDENT) ==========
INSERT INTO student_teachers (user_id, type, employee_id, student_id, department, is_active, created_by_id, created_at, updated_at)
SELECT u.id, 'STUDENT', 'EMP-S' || (ROW_NUMBER() OVER (ORDER BY u.username)), 'STU-' || (ROW_NUMBER() OVER (ORDER BY u.username)), 'Operations', true, admin.id, NOW(), NOW()
FROM users u
CROSS JOIN (SELECT id FROM users WHERE username = 'admin' LIMIT 1) admin
WHERE u.username LIKE 'student%'
ON CONFLICT (user_id) DO NOTHING;

-- ========== 5. Teacher records (student_teachers, type TEACHER) ==========
INSERT INTO student_teachers (user_id, type, employee_id, department, qualification, is_active, created_by_id, created_at, updated_at)
SELECT u.id, 'TEACHER', 'EMP-T' || (ROW_NUMBER() OVER (ORDER BY u.username)), 'Training', 'Masters', true, admin.id, NOW(), NOW()
FROM users u
CROSS JOIN (SELECT id FROM users WHERE username = 'admin' LIMIT 1) admin
WHERE u.username LIKE 'teacher%'
ON CONFLICT (user_id) DO NOTHING;

-- ========== 6. Training programs (10) – skip if sample programs already exist ==========
INSERT INTO training_programs (title, description, training_name, category, duration_hours, status, max_participants, created_by_id, instructor_id, created_at, updated_at)
SELECT 'Sample Program 1', 'Introduction to Compliance', 'Compliance Basics', 'Technical', 8, 'PUBLISHED', 30, a.id, t1.id, NOW(), NOW() FROM (SELECT id FROM users WHERE username = 'admin' LIMIT 1) a, (SELECT id FROM users WHERE username = 'teacher1' LIMIT 1) t1 WHERE NOT EXISTS (SELECT 1 FROM training_programs WHERE title = 'Sample Program 1') UNION ALL
SELECT 'Sample Program 2', 'Safety and Risk Management', 'Safety Fundamentals', 'Safety', 16, 'PUBLISHED', 25, a.id, t2.id, NOW(), NOW() FROM (SELECT id FROM users WHERE username = 'admin' LIMIT 1) a, (SELECT id FROM users WHERE username = 'teacher2' LIMIT 1) t2 WHERE NOT EXISTS (SELECT 1 FROM training_programs WHERE title = 'Sample Program 2') UNION ALL
SELECT 'Sample Program 3', 'Soft Skills for Professionals', 'Communication & Leadership', 'Soft Skills', 12, 'PUBLISHED', 40, a.id, t3.id, NOW(), NOW() FROM (SELECT id FROM users WHERE username = 'admin' LIMIT 1) a, (SELECT id FROM users WHERE username = 'teacher3' LIMIT 1) t3 WHERE NOT EXISTS (SELECT 1 FROM training_programs WHERE title = 'Sample Program 3') UNION ALL
SELECT 'Sample Program 4', 'Technical Writing', 'Documentation Standards', 'Technical', 10, 'PUBLISHED', 20, a.id, t4.id, NOW(), NOW() FROM (SELECT id FROM users WHERE username = 'admin' LIMIT 1) a, (SELECT id FROM users WHERE username = 'teacher4' LIMIT 1) t4 WHERE NOT EXISTS (SELECT 1 FROM training_programs WHERE title = 'Sample Program 4') UNION ALL
SELECT 'Sample Program 5', 'Quality Assurance Basics', 'QA Fundamentals', 'Technical', 14, 'PUBLISHED', 35, a.id, t5.id, NOW(), NOW() FROM (SELECT id FROM users WHERE username = 'admin' LIMIT 1) a, (SELECT id FROM users WHERE username = 'teacher5' LIMIT 1) t5 WHERE NOT EXISTS (SELECT 1 FROM training_programs WHERE title = 'Sample Program 5') UNION ALL
SELECT 'Sample Program 6', 'Data Privacy and GDPR', 'Privacy Awareness', 'Compliance', 6, 'PUBLISHED', 50, a.id, t6.id, NOW(), NOW() FROM (SELECT id FROM users WHERE username = 'admin' LIMIT 1) a, (SELECT id FROM users WHERE username = 'teacher6' LIMIT 1) t6 WHERE NOT EXISTS (SELECT 1 FROM training_programs WHERE title = 'Sample Program 6') UNION ALL
SELECT 'Sample Program 7', 'Project Management Essentials', 'PM Basics', 'Management', 20, 'PUBLISHED', 30, a.id, t7.id, NOW(), NOW() FROM (SELECT id FROM users WHERE username = 'admin' LIMIT 1) a, (SELECT id FROM users WHERE username = 'teacher7' LIMIT 1) t7 WHERE NOT EXISTS (SELECT 1 FROM training_programs WHERE title = 'Sample Program 7') UNION ALL
SELECT 'Sample Program 8', 'Cybersecurity Awareness', 'Security Fundamentals', 'Technical', 4, 'PUBLISHED', 100, a.id, t8.id, NOW(), NOW() FROM (SELECT id FROM users WHERE username = 'admin' LIMIT 1) a, (SELECT id FROM users WHERE username = 'teacher8' LIMIT 1) t8 WHERE NOT EXISTS (SELECT 1 FROM training_programs WHERE title = 'Sample Program 8') UNION ALL
SELECT 'Sample Program 9', 'First Aid and Emergency', 'First Aid at Work', 'Safety', 8, 'PUBLISHED', 15, a.id, t9.id, NOW(), NOW() FROM (SELECT id FROM users WHERE username = 'admin' LIMIT 1) a, (SELECT id FROM users WHERE username = 'teacher9' LIMIT 1) t9 WHERE NOT EXISTS (SELECT 1 FROM training_programs WHERE title = 'Sample Program 9') UNION ALL
SELECT 'Sample Program 10', 'Ethics and Integrity', 'Workplace Ethics', 'Compliance', 6, 'PUBLISHED', 60, a.id, t10.id, NOW(), NOW() FROM (SELECT id FROM users WHERE username = 'admin' LIMIT 1) a, (SELECT id FROM users WHERE username = 'teacher10' LIMIT 1) t10 WHERE NOT EXISTS (SELECT 1 FROM training_programs WHERE title = 'Sample Program 10');

-- ========== 7. Training sessions (10) – one per program ==========
INSERT INTO training_sessions (program_id, start_date_time, end_date_time, location, session_type, status, topic, sequence_order, instructor_id, created_by_id, created_at, updated_at)
SELECT tp.id,
  (CURRENT_DATE + (tp.rn || ' days')::interval) + time '09:00',
  (CURRENT_DATE + (tp.rn || ' days')::interval) + time '11:00',
  'Room ' || tp.rn, 'In-Person', 'SCHEDULED', 'Session 1: ' || tp.title, 1, t.id, a.id, NOW(), NOW()
FROM (SELECT id, title, ROW_NUMBER() OVER (ORDER BY id) AS rn FROM training_programs WHERE title LIKE 'Sample Program %') tp
CROSS JOIN (SELECT id FROM users WHERE username = 'admin' LIMIT 1) a
JOIN LATERAL (SELECT id FROM users WHERE username = 'teacher' || tp.rn LIMIT 1) t ON true
WHERE NOT EXISTS (SELECT 1 FROM training_sessions WHERE program_id = tp.id);

-- ========== 8. Training materials (10) – one per program ==========
INSERT INTO training_materials (program_id, title, description, material_type, file_name, is_required, display_order, uploaded_by_id, created_at, updated_at)
SELECT tp.id, 'Material: ' || tp.title, 'Supporting material for ' || tp.title, 'PDF', 'sample-doc-' || tp.rn || '.pdf', true, tp.rn, a.id, NOW(), NOW()
FROM (SELECT id, title, ROW_NUMBER() OVER (ORDER BY id) AS rn FROM training_programs WHERE title LIKE 'Sample Program %') tp
CROSS JOIN (SELECT id FROM users WHERE username = 'admin' LIMIT 1) a
WHERE NOT EXISTS (SELECT 1 FROM training_materials WHERE program_id = tp.id AND title = 'Material: ' || tp.title);

-- ========== 9. Assignments / assessments (10) – one per program ==========
INSERT INTO assessments (program_id, title, description, assessment_type, passing_score, max_score, is_required, status, created_by_id, created_at, updated_at)
SELECT tp.id, 'Assignment: ' || tp.title, 'Post-session assignment for ' || tp.title, 'QUIZ', 70.0, 100.0, true, 'PUBLISHED', a.id, NOW(), NOW()
FROM (SELECT id, title, ROW_NUMBER() OVER (ORDER BY id) AS rn FROM training_programs WHERE title LIKE 'Sample Program %') tp
CROSS JOIN (SELECT id FROM users WHERE username = 'admin' LIMIT 1) a
WHERE NOT EXISTS (SELECT 1 FROM assessments WHERE program_id = tp.id AND title = 'Assignment: ' || tp.title);

-- ========== 10. Enrollments – assign students to programs (each program 3–5 students, each student in 2–3 programs) ==========
INSERT INTO enrollments (program_id, session_id, participant_id, status, enrollment_date, enrolled_by_id, created_at, updated_at)
SELECT tp.id, ts.id, s.id, 'CONFIRMED', NOW(), a.id, NOW(), NOW()
FROM (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn FROM training_programs WHERE title LIKE 'Sample Program %') tp
CROSS JOIN (SELECT id FROM users WHERE username = 'admin' LIMIT 1) a
JOIN LATERAL (SELECT id FROM training_sessions WHERE program_id = tp.id ORDER BY id LIMIT 1) ts ON true
CROSS JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn FROM users WHERE username LIKE 'student%') s
WHERE s.rn IN (
  (tp.rn - 1) % 10 + 1,
  tp.rn % 10 + 1,
  (tp.rn + 1) % 10 + 1,
  (tp.rn + 2) % 10 + 1,
  (tp.rn + 3) % 10 + 1
)
AND NOT EXISTS (SELECT 1 FROM enrollments e WHERE e.program_id = tp.id AND e.participant_id = s.id);

