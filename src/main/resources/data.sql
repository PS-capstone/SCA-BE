-- ============================================
-- SCA-BE Test Data Initialization Script
-- ============================================

-- 1. Fish (Master Data) - Fish available through gacha
-- fish_id is auto-generated
-- Probability calculation:
-- COMMON (70%): 7 fish -> 70/7 = 10% each
-- RARE (25%): 4 fish -> 25/4 = 6.25% each
-- LEGENDARY (5%): 2 fish -> 5/2 = 2.5% each (빈칸으로 표시)
-- Total: 100%
INSERT INTO fish (fish_name, grade, probability) VALUES
-- COMMON (7개)
('열대어', 'COMMON', 0.10),
('금붕어', 'COMMON', 0.10),
('구피', 'COMMON', 0.10),
('네온테트라', 'COMMON', 0.10),
('흰동가리', 'COMMON', 0.10),
('해마', 'COMMON', 0.10),
('복어', 'COMMON', 0.10),
-- RARE (4개)
('가오리', 'RARE', 0.0625),
('상어', 'RARE', 0.0625),
('범고래', 'RARE', 0.0625),
('바다거북', 'RARE', 0.0625),
-- LEGENDARY (2개) - 빈칸으로 표시
('???', 'LEGENDARY', 0.025),
('???', 'LEGENDARY', 0.025);

-- 2. Members (User Base Info) - 3 Teachers, 10 Students
-- member_id is auto-generated
-- Password: "password123" (BCrypt Hash)
-- BCrypt Hash: $2a$10$sp2WRqBmMCocuoe0zXyiYOG0VQriJ6w98sZs337HQl7hbxEpA4xKq
-- deleted_at is NULL for active users (Soft Delete)
INSERT INTO members (username, password, real_name, nickname, email, role, created_at, deleted_at) VALUES
-- Teachers
('teacher1', '$2a$10$sp2WRqBmMCocuoe0zXyiYOG0VQriJ6w98sZs337HQl7hbxEpA4xKq', 'Kim Teacher', 'Teacher Kim', 'teacher1@sca.edu', 'TEACHER', NOW(), NULL),
('teacher2', '$2a$10$sp2WRqBmMCocuoe0zXyiYOG0VQriJ6w98sZs337HQl7hbxEpA4xKq', 'Lee Teacher', 'Teacher Lee', 'teacher2@sca.edu', 'TEACHER', NOW(), NULL),
('teacher3', '$2a$10$sp2WRqBmMCocuoe0zXyiYOG0VQriJ6w98sZs337HQl7hbxEpA4xKq', 'Park Teacher', 'Teacher Park', 'teacher3@sca.edu', 'TEACHER', NOW(), NULL),
-- Students
('student1', '$2a$10$sp2WRqBmMCocuoe0zXyiYOG0VQriJ6w98sZs337HQl7hbxEpA4xKq', 'Hong Gildong', 'Gildong', 'student1@sca.edu', 'STUDENT', NOW(), NULL),
('student2', '$2a$10$sp2WRqBmMCocuoe0zXyiYOG0VQriJ6w98sZs337HQl7hbxEpA4xKq', 'Kim Cheolsu', 'Cheolsu', 'student2@sca.edu', 'STUDENT', NOW(), NULL),
('student3', '$2a$10$sp2WRqBmMCocuoe0zXyiYOG0VQriJ6w98sZs337HQl7hbxEpA4xKq', 'Lee Younghee', 'Younghee', 'student3@sca.edu', 'STUDENT', NOW(), NULL),
('student4', '$2a$10$sp2WRqBmMCocuoe0zXyiYOG0VQriJ6w98sZs337HQl7hbxEpA4xKq', 'Park Minsu', 'Minsu', 'student4@sca.edu', 'STUDENT', NOW(), NULL),
('student5', '$2a$10$sp2WRqBmMCocuoe0zXyiYOG0VQriJ6w98sZs337HQl7hbxEpA4xKq', 'Choi Jieun', 'Jieun', 'student5@sca.edu', 'STUDENT', NOW(), NULL),
('student6', '$2a$10$sp2WRqBmMCocuoe0zXyiYOG0VQriJ6w98sZs337HQl7hbxEpA4xKq', 'Jung Suhyun', 'Suhyun', 'student6@sca.edu', 'STUDENT', NOW(), NULL),
('student7', '$2a$10$sp2WRqBmMCocuoe0zXyiYOG0VQriJ6w98sZs337HQl7hbxEpA4xKq', 'Kang Minjun', 'Minjun', 'student7@sca.edu', 'STUDENT', NOW(), NULL),
('student8', '$2a$10$sp2WRqBmMCocuoe0zXyiYOG0VQriJ6w98sZs337HQl7hbxEpA4xKq', 'Cho Seoyeon', 'Seoyeon', 'student8@sca.edu', 'STUDENT', NOW(), NULL),
('student9', '$2a$10$sp2WRqBmMCocuoe0zXyiYOG0VQriJ6w98sZs337HQl7hbxEpA4xKq', 'Yoon Haneul', 'Haneul', 'student9@sca.edu', 'STUDENT', NOW(), NULL),
('student10', '$2a$10$sp2WRqBmMCocuoe0zXyiYOG0VQriJ6w98sZs337HQl7hbxEpA4xKq', 'Jang Seojun', 'Seojun', 'student10@sca.edu', 'STUDENT', NOW(), NULL);

-- 3. Teachers (Teacher Info)
-- member_id is manually assigned (uses @MapsId from Member)
INSERT INTO teachers (member_id) VALUES
(1),
(2),
(3);

-- 4. Classes (Class Info) - Each teacher manages one class
-- class_id is auto-generated
-- deleted_at is NULL for active classes (Soft Delete)
INSERT INTO classes (teacher_id, class_name, invite_code, grade, subject, description, created_at, deleted_at) VALUES
(1, 'Grade 3 Class 1', 'INVITE001', 'Grade 3', 'Math', 'Fun Math Class', NOW(), NULL),
(2, 'Grade 2 Class 3', 'INVITE002', 'Grade 2', 'Science', 'Exciting Science Lab', NOW(), NULL),
(3, 'Grade 1 Class 2', 'INVITE003', 'Grade 1', 'English', 'Enjoyable English Class', NOW(), NULL);

-- 5. Students (Student Info) - Assign class and resources to each student
-- member_id is manually assigned (uses @MapsId from Member)
INSERT INTO students (member_id, class_id, coral, research_data, grade) VALUES
-- Grade 3 Class 1 (Teacher Kim) - member_ids 4-7
(4, 1, 500, 200, 95.0),
(5, 1, 300, 150, 88.5),
(6, 1, 450, 180, 91.2),
(7, 1, 600, 250, 86.7),
-- Grade 2 Class 3 (Teacher Lee) - member_ids 8-10
(8, 2, 400, 160, 90.0),
(9, 2, 350, 140, 84.3),
(10, 2, 550, 220, 92.1),
-- Grade 1 Class 2 (Teacher Park) - member_ids 11-13
(11, 3, 250, 100, 80.0),
(12, 3, 300, 120, 82.4),
(13, 3, 400, 160, 89.5);

-- 6. Collections (Student Gacha Collections)
-- collection_id is auto-generated
INSERT INTO collections (student_id) VALUES
(4),
(5),
(6),
(7),
(8),
(9),
(10),
(11),
(12),
(13);

-- 7. Collection Entries (Fish owned by students)
-- entry_id is auto-generated
-- Note: fish_id and collection_id are now auto-generated, so we reference by position
-- Student 4 (Hong Gildong) - collection_id will be 1
INSERT INTO collection_entries (collection_id, fish_id, fish_count) VALUES
(1, 1, 3),
(1, 2, 2),
(1, 5, 1),
(1, 9, 1),
(1, 10, 1),
-- Student 5 (Kim Cheolsu) - collection_id will be 2
(2, 1, 5),
(2, 3, 2),
(2, 6, 1),
-- Student 6 (Lee Younghee) - collection_id will be 3
(3, 2, 3),
(3, 4, 4),
(3, 7, 2),
(3, 10, 1),
-- Student 7 (Park Minsu) - collection_id will be 4
(4, 1, 2),
(4, 5, 1),
(4, 8, 1),
-- Student 8 (Choi Jieun) - collection_id will be 5
(5, 3, 6),
(5, 6, 2),
-- Student 9 (Jung Suhyun) - collection_id will be 6
(6, 1, 1),
(6, 2, 1),
(6, 3, 1),
(6, 4, 1),
-- Student 10 (Kang Minjun) - collection_id will be 7
(7, 5, 2),
(7, 9, 1),
-- Student 11 (Cho Seoyeon) - collection_id will be 8
(8, 1, 3),
(8, 7, 1),
-- Student 12 (Yoon Haneul) - collection_id will be 9
(9, 2, 2),
(9, 6, 1),
-- Student 13 (Jang Seojun) - collection_id will be 10
(10, 4, 4),
(10, 8, 1);

-- 8. Personal Quests (Individual Quests)
-- quest_id is auto-generated
-- difficulty: 1 (EASY), 2 (BASIC), 3 (MEDIUM), 4 (HARD), 5 (VERY_HARD)
-- deleted_at is NULL for active quests (Soft Delete)
INSERT INTO quests (teacher_id, title, teacher_content, reward_coral_default, reward_research_data_default, deadline, difficulty, created_at, deleted_at) VALUES
(1, 'Math Workbook Practice', 'Complete Chapter 1 of the 2nd semester math workbook and submit photos.', 100, 50, '2025-12-31 23:59:59', 3, NOW(), NULL),
(1, 'Geometry Project', 'Submit a creative project using properties of triangles.', 200, 100, '2025-12-25 23:59:59', 5, NOW(), NULL),
(2, 'Science Experiment Report', 'Conduct water boiling point experiment and write a report.', 150, 75, '2025-12-20 23:59:59', 4, NOW(), NULL),
(2, 'Ecosystem Observation Journal', 'Observe the ecosystem around school and keep a journal for one week.', 120, 60, '2025-12-28 23:59:59', 3, NOW(), NULL),
(3, 'English Essay Writing', 'Write an essay on "My Dream" with at least 200 words.', 100, 50, '2025-12-22 23:59:59', 4, NOW(), NULL);

-- 9. Quest Assignments (Individual Quest Assignments)
-- assignment_id is auto-generated
INSERT INTO quest_assignments (quest_id, student_id, reward_coral_personal, reward_research_data_personal, status) VALUES
-- Quest 1 assignments (Grade 3 Class 1 students: 4,5,6,7)
(1, 4, 100, 50, 'APPROVED'),
(1, 5, 100, 50, 'SUBMITTED'),
(1, 6, 120, 60, 'ASSIGNED'),
(1, 7, 90, 45, 'APPROVED'),
-- Quest 2 assignments
(2, 4, 200, 100, 'SUBMITTED'),
(2, 6, 240, 120, 'ASSIGNED'),
-- Quest 3 assignments (Grade 2 Class 3 students: 8,9,10)
(3, 8, 150, 75, 'APPROVED'),
(3, 9, 165, 82, 'SUBMITTED'),
(3, 10, 150, 75, 'ASSIGNED'),
-- Quest 4 assignments
(4, 8, 120, 60, 'ASSIGNED'),
-- Quest 5 assignments (Grade 1 Class 2 students: 11,12,13)
(5, 11, 100, 50, 'SUBMITTED'),
(5, 12, 100, 50, 'APPROVED'),
(5, 13, 80, 40, 'ASSIGNED');

-- 10. Submissions (Quest Submissions)
-- submission_id is auto-generated
INSERT INTO submissions (assignment_id, attachment_url, submitted_at, comment, student_content) VALUES
(1, 'https://storage.sca.edu/files/student1_math_hw.pdf', '2025-11-10 15:30:00', 'Great job!', 'I worked hard on this.'),
(2, 'https://storage.sca.edu/files/student2_math_hw.pdf', '2025-11-12 18:20:00', NULL, 'I solved all the problems.'),
(4, 'https://storage.sca.edu/files/student4_math_hw.pdf', '2025-11-11 20:00:00', 'Perfect!', 'Photo attached.'),
(5, 'https://storage.sca.edu/files/student1_geometry.pdf', '2025-11-13 14:00:00', NULL, 'Triangle project submission.'),
(7, 'https://storage.sca.edu/files/student5_science.pdf', '2025-11-14 16:30:00', 'Detailed experiment process.', 'Experiment result report.'),
(8, 'https://storage.sca.edu/files/student6_science.pdf', '2025-11-15 10:00:00', NULL, 'Water boiling point measurement complete'),
(11, 'https://storage.sca.edu/files/student8_essay.pdf', '2025-11-14 19:00:00', NULL, 'My Dream essay submission.'),
(12, 'https://storage.sca.edu/files/student9_essay.pdf', '2025-11-13 21:00:00', 'Touching story!', 'English essay submission.');

-- 11. Group Quests (Group Quests)
-- group_quest_id is auto-generated
INSERT INTO group_quests (teacher_id, class_id, title, status, reward_coral, reward_research_data, end_date, content, type, created_at) VALUES
(1, 1, 'Class Attendance Challenge', 'ACTIVE', 50, 25, '2025-11-30 23:59:59', 'Achieve 95% attendance rate for the entire class in November', 'ATTENDANCE', NOW()),
(1, 1, 'Math Assignment 100% Completion', 'COMPLETED', 100, 50, '2025-11-15 23:59:59', 'Everyone submits this weeks math assignment', 'ASSIGNMENT', NOW()),
(2, 2, 'Science Experiment Participation', 'ACTIVE', 75, 35, '2025-11-25 23:59:59', 'All students actively participate in experiments', 'PARTICIPATION', NOW()),
(3, 3, 'Midterm Exam All Pass', 'COMPLETED', 150, 75, '2025-11-10 23:59:59', 'Everyone scores 60+ on midterm exam', 'EXAM', NOW());

-- 12. Group Quest Progress (Group Quest Progress)
-- progress_id is auto-generated
INSERT INTO group_quest_progress (group_quest_id, student_id, is_completed, completed_at) VALUES
-- Attendance challenge (group_quest_id=1, In progress)
(1, 4, true, '2025-11-15 09:00:00'),
(1, 5, true, '2025-11-15 09:00:00'),
(1, 6, false, NULL),
(1, 7, true, '2025-11-15 09:00:00'),
-- Math assignment completion (group_quest_id=2, Completed)
(2, 4, true, '2025-11-14 18:00:00'),
(2, 5, true, '2025-11-14 20:00:00'),
(2, 6, true, '2025-11-15 10:00:00'),
(2, 7, true, '2025-11-14 19:00:00'),
-- Science experiment participation (group_quest_id=3, In progress)
(3, 8, true, '2025-11-14 14:00:00'),
(3, 9, true, '2025-11-14 14:00:00'),
(3, 10, false, NULL),
-- Midterm exam all pass (group_quest_id=4, Completed)
(4, 11, true, '2025-11-08 15:00:00'),
(4, 12, true, '2025-11-08 15:00:00'),
(4, 13, true, '2025-11-08 15:00:00');

-- 13. Raids (Raids)
-- raid_id is auto-generated
INSERT INTO raids (teacher_id, class_id, raid_name, boss_type, difficulty, status, start_date, end_date, total_boss_hp, current_boss_hp, reward_coral, special_reward_description) VALUES
(1, 1, '중간고사 대비 크라켄', 'KRAKEN', 'HIGH', 'ACTIVE', '2025-11-10 00:00:00', '2025-12-29 23:59:59', 10000, 3500, 200, 'Legendary grade fish acquisition opportunity'),
(2, 2, '헬릭스 인더스트리 소탕전', 'ZELUS_INDUSTRY', 'MEDIUM', 'COMPLETED', '2025-11-05 00:00:00', '2025-12-29 23:59:59', 5000, 0, 150, 'Rare grade fish x3'),
(3, 3, '기말 대비 정찰 작전', 'KRAKEN', 'LOW', 'ACTIVE', '2025-11-15 00:00:00', '2025-12-29 23:59:59', 3000, 2100, 100, 'Common grade fish x10');

-- 14. Contributions (Raid Contributions)
-- contribution_id is auto-generated
INSERT INTO contributions (raid_id, student_id, damage) VALUES
-- Raid 1 contributions (Grade 3 Class 1)
(1, 4, 2000),
(1, 5, 1500),
(1, 6, 1800),
(1, 7, 1200),
-- Raid 2 contributions (Grade 2 Class 3 - Completed)
(2, 8, 2000),
(2, 9, 1800),
(2, 10, 1200),
-- Raid 3 contributions (Grade 1 Class 2)
(3, 11, 300),
(3, 12, 400),
(3, 13, 200);

-- 15. Notices (Notifications)
-- notice_id is auto-generated
INSERT INTO notice (notice_type, student_id, assignment_id, group_quest_id, raid_id, title, content, created_at) VALUES
('PERSONAL_QUEST_ASSIGNED', 4, 1, NULL, NULL, 'New personal quest assigned', 'Math workbook practice quest assigned.', NOW()),
('PERSONAL_QUEST_APPROVED', 4, 1, NULL, NULL, 'Quest approved', 'Math workbook practice quest approved. Check your rewards!', NOW()),
('COMMUNITY_QUEST_ASSIGNED', 4, NULL, 1, NULL, 'New group quest', 'Class attendance challenge has started.', NOW()),
('COMMUNITY_QUEST_FINISHED', 4, NULL, 2, NULL, 'Group quest completed', 'Math assignment 100% completion achieved! Claim your rewards.', NOW()),
('RAID_STARTED', 4, NULL, NULL, 1, 'Raid started', 'Kraken raid has begun. Join now!', NOW()),
('RAID_FINISHED', 8, NULL, NULL, 2, 'Raid completed', 'Zelus Industry raid successfully completed!', NOW());

-- 16. Action Logs (Activity Logs)
-- log_id is auto-generated
INSERT INTO action_logs (student_id, action_type, assignment_id, group_quest_id, raid_id, change_coral, change_research, log_message, created_at) VALUES
(4, 'REWARD_RECEIVED', 1, NULL, NULL, 100, 50, 'Math workbook practice quest completion reward', NOW()),
(7, 'REWARD_RECEIVED', 4, NULL, NULL, 90, 45, 'Math workbook practice quest completion reward', NOW()),
(4, 'GROUP_QUEST_COMPLETE', NULL, 2, NULL, 100, 50, 'Math assignment 100% completion reward', NOW()),
(5, 'GROUP_QUEST_COMPLETE', NULL, 2, NULL, 100, 50, 'Math assignment 100% completion reward', NOW()),
(8, 'RAID_REWARD_RECEIVED', NULL, NULL, 2, 150, 0, 'Zelus Industry raid completion reward', NOW()),
(9, 'RAID_REWARD_RECEIVED', NULL, NULL, 2, 150, 0, 'Zelus Industry raid completion reward', NOW()),
(12, 'REWARD_RECEIVED', 12, NULL, NULL, 100, 50, 'My Dream essay writing completion reward', NOW());

-- ============================================
-- AI Learning System Data (students_factors, students_quest_factors, ai_learning_logs)
-- ============================================

-- 17. Students Factors (학생 보정계수)
-- id is auto-generated
-- All students get initial factors based on their grades
INSERT INTO students_factors (student_id, global_factor, initialized, initial_score, initialized_at, total_learning_count, last_learning_at, avg_modification_rate, created_at, updated_at) VALUES
-- Grade 3 Class 1 students
(4, 0.95, true, 95, NOW(), 10, NOW(), 0.08, NOW(), NOW()),   -- Hong Gildong (high performer)
(5, 1.065, true, 88, NOW(), 8, NOW(), 0.12, NOW(), NOW()),   -- Kim Cheolsu
(6, 0.988, true, 91, NOW(), 9, NOW(), 0.09, NOW(), NOW()),   -- Lee Younghee
(7, 1.083, true, 86, NOW(), 7, NOW(), 0.15, NOW(), NOW()),   -- Park Minsu
-- Grade 2 Class 3 students
(8, 1.0, true, 90, NOW(), 12, NOW(), 0.10, NOW(), NOW()),    -- Choi Jieun
(9, 1.107, true, 84, NOW(), 5, NOW(), 0.18, NOW(), NOW()),   -- Jung Suhyun
(10, 0.979, true, 92, NOW(), 11, NOW(), 0.07, NOW(), NOW()), -- Kang Minjun
-- Grade 1 Class 2 students
(11, 1.25, true, 80, NOW(), 3, NOW(), 0.25, NOW(), NOW()),   -- Cho Seoyeon (needs more support)
(12, 1.176, true, 82, NOW(), 4, NOW(), 0.20, NOW(), NOW()),  -- Yoon Haneul
(13, 1.055, true, 89, NOW(), 6, NOW(), 0.13, NOW(), NOW());  -- Jang Seojun

-- 18. Students Quest Factors (학생 난이도별 보정계수)
-- id is auto-generated
-- Each student has difficulty-specific factors
-- difficulty: 1 (EASY), 2 (BASIC), 3 (MEDIUM), 4 (HARD), 5 (VERY_HARD)
INSERT INTO students_quest_factors (student_factor_id, difficulty, factor_value, learning_count, created_at, updated_at) VALUES
-- Student 4 (Hong Gildong - id 1) - High performer
(1, 1, 0.80, 2, NOW(), NOW()),
(1, 2, 0.85, 2, NOW(), NOW()),
(1, 3, 0.90, 3, NOW(), NOW()),
(1, 4, 0.95, 2, NOW(), NOW()),
(1, 5, 1.00, 1, NOW(), NOW()),
-- Student 5 (Kim Cheolsu - id 2)
(2, 1, 0.90, 2, NOW(), NOW()),
(2, 2, 0.95, 2, NOW(), NOW()),
(2, 3, 1.05, 2, NOW(), NOW()),
(2, 4, 1.10, 1, NOW(), NOW()),
(2, 5, 1.15, 1, NOW(), NOW()),
-- Student 6 (Lee Younghee - id 3)
(3, 1, 0.85, 2, NOW(), NOW()),
(3, 2, 0.90, 2, NOW(), NOW()),
(3, 3, 0.95, 3, NOW(), NOW()),
(3, 4, 1.00, 1, NOW(), NOW()),
(3, 5, 1.05, 1, NOW(), NOW()),
-- Student 7 (Park Minsu - id 4)
(4, 1, 0.95, 2, NOW(), NOW()),
(4, 2, 1.00, 1, NOW(), NOW()),
(4, 3, 1.10, 2, NOW(), NOW()),
(4, 4, 1.15, 1, NOW(), NOW()),
(4, 5, 1.20, 1, NOW(), NOW()),
-- Student 8 (Choi Jieun - id 5)
(5, 1, 0.85, 3, NOW(), NOW()),
(5, 2, 0.90, 3, NOW(), NOW()),
(5, 3, 1.00, 3, NOW(), NOW()),
(5, 4, 1.05, 2, NOW(), NOW()),
(5, 5, 1.10, 1, NOW(), NOW()),
-- Student 9 (Jung Suhyun - id 6)
(6, 1, 1.00, 1, NOW(), NOW()),
(6, 2, 1.05, 1, NOW(), NOW()),
(6, 3, 1.15, 2, NOW(), NOW()),
(6, 4, 1.20, 1, NOW(), NOW()),
(6, 5, 1.25, 0, NOW(), NOW()),
-- Student 10 (Kang Minjun - id 7)
(7, 1, 0.80, 3, NOW(), NOW()),
(7, 2, 0.85, 3, NOW(), NOW()),
(7, 3, 0.95, 3, NOW(), NOW()),
(7, 4, 1.00, 1, NOW(), NOW()),
(7, 5, 1.05, 1, NOW(), NOW()),
-- Student 11 (Cho Seoyeon - id 8)
(8, 1, 1.10, 1, NOW(), NOW()),
(8, 2, 1.15, 1, NOW(), NOW()),
(8, 3, 1.30, 1, NOW(), NOW()),
(8, 4, 1.35, 0, NOW(), NOW()),
(8, 5, 1.40, 0, NOW(), NOW()),
-- Student 12 (Yoon Haneul - id 9)
(9, 1, 1.05, 1, NOW(), NOW()),
(9, 2, 1.10, 1, NOW(), NOW()),
(9, 3, 1.20, 1, NOW(), NOW()),
(9, 4, 1.25, 1, NOW(), NOW()),
(9, 5, 1.30, 0, NOW(), NOW()),
-- Student 13 (Jang Seojun - id 10)
(10, 1, 0.95, 2, NOW(), NOW()),
(10, 2, 1.00, 1, NOW(), NOW()),
(10, 3, 1.10, 2, NOW(), NOW()),
(10, 4, 1.15, 1, NOW(), NOW()),
(10, 5, 1.20, 0, NOW(), NOW());

-- 19. AI Learning Logs (AI 학습 로그)
-- id is auto-generated
-- Sample learning logs for approved quest assignments
-- difficulty: 1 (EASY), 2 (BASIC), 3 (MEDIUM), 4 (HARD), 5 (VERY_HARD)
INSERT INTO ai_learning_logs (assignment_id, student_id, difficulty, cognitive_score, effort_score, ai_coral, ai_research_data, teacher_coral, teacher_research_data, learned, learned_at, created_at) VALUES
-- Assignment 1: Student 4, Quest 1 (difficulty 3 - MEDIUM) - APPROVED
(1, 4, 3, 4, 6, 95, 48, 100, 50, true, NOW(), NOW()),
-- Assignment 4: Student 7, Quest 1 (difficulty 3 - MEDIUM) - APPROVED
(4, 7, 3, 3, 7, 85, 42, 90, 45, true, NOW(), NOW()),
-- Assignment 7: Student 8, Quest 3 (difficulty 4 - HARD) - APPROVED
(7, 8, 4, 5, 8, 145, 72, 150, 75, true, NOW(), NOW()),
-- Assignment 12: Student 12, Quest 5 (difficulty 4 - HARD) - APPROVED
(12, 12, 4, 4, 7, 95, 48, 100, 50, true, NOW(), NOW()),
-- Additional learning logs (some not yet learned)
(2, 5, 3, 4, 6, 98, 49, 100, 50, false, NULL, NOW()),
(8, 9, 4, 3, 8, 155, 77, 165, 82, false, NULL, NOW());

-- ============================================
-- Data Initialization Complete
-- ============================================
