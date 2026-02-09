-- Delete the 10 sample records per training module (reverse order of insert).
-- Removes only data created by database/training_sample_data.sql.

-- 1. Attendances for sessions that belong to sample programs
DELETE FROM attendances
WHERE session_id IN (SELECT id FROM training_sessions WHERE program_id IN (SELECT id FROM training_programs WHERE title LIKE 'Sample Program %'));

-- 2. Assessment results for sample program assessments
DELETE FROM assessment_results
WHERE assessment_id IN (SELECT id FROM assessments WHERE program_id IN (SELECT id FROM training_programs WHERE title LIKE 'Sample Program %'));

-- 3. Certifications for enrollments in sample programs
DELETE FROM certifications
WHERE enrollment_id IN (SELECT id FROM enrollments WHERE program_id IN (SELECT id FROM training_programs WHERE title LIKE 'Sample Program %'));

-- 4. Material reviews for materials in sample programs (if table exists and has data)
DELETE FROM material_reviews
WHERE material_id IN (SELECT id FROM training_materials WHERE program_id IN (SELECT id FROM training_programs WHERE title LIKE 'Sample Program %'));

-- 5. Enrollments for sample programs
DELETE FROM enrollments
WHERE program_id IN (SELECT id FROM training_programs WHERE title LIKE 'Sample Program %');

-- 6. Assessments (assignments) for sample programs
DELETE FROM assessments
WHERE program_id IN (SELECT id FROM training_programs WHERE title LIKE 'Sample Program %');

-- 7. Training materials for sample programs
DELETE FROM training_materials
WHERE program_id IN (SELECT id FROM training_programs WHERE title LIKE 'Sample Program %');

-- 8. Training sessions for sample programs
DELETE FROM training_sessions
WHERE program_id IN (SELECT id FROM training_programs WHERE title LIKE 'Sample Program %');

-- 9. Sample training programs
DELETE FROM training_programs
WHERE title LIKE 'Sample Program %';

-- 10. Student/teacher records for sample users
DELETE FROM student_teachers
WHERE user_id IN (SELECT id FROM users WHERE username IN (
  'student1','student2','student3','student4','student5','student6','student7','student8','student9','student10',
  'teacher1','teacher2','teacher3','teacher4','teacher5','teacher6','teacher7','teacher8','teacher9','teacher10'
));

-- 11. User roles for sample users
DELETE FROM user_roles
WHERE user_id IN (SELECT id FROM users WHERE username IN (
  'student1','student2','student3','student4','student5','student6','student7','student8','student9','student10',
  'teacher1','teacher2','teacher3','teacher4','teacher5','teacher6','teacher7','teacher8','teacher9','teacher10'
));

-- 12. Sample student and teacher users
DELETE FROM users
WHERE username IN (
  'student1','student2','student3','student4','student5','student6','student7','student8','student9','student10',
  'teacher1','teacher2','teacher3','teacher4','teacher5','teacher6','teacher7','teacher8','teacher9','teacher10'
);
