-- ============================================
-- 개발/테스트용: 모든 물고기를 학생 컬렉션에 추가
-- ============================================
-- 이 스크립트는 특정 학생(student_id = 4, Hong Gildong)의 컬렉션에
-- 모든 물고기를 추가합니다.
-- 
-- 사용 방법:
-- 1. H2 Console에서 실행: http://localhost:8080/h2-console
-- 2. 또는 application-dev.yaml의 sql.init.data-locations에 추가
-- ============================================

-- Student 4 (Hong Gildong)의 collection_id는 1입니다.
-- 모든 물고기(fish_id 1~13)를 컬렉션에 추가
-- 이미 존재하는 경우는 무시됩니다 (UNIQUE 제약조건)

-- COMMON 등급 물고기 (1~7)
INSERT INTO collection_entries (collection_id, fish_id, fish_count) VALUES
(1, 1, 1),  -- 열대어
(1, 2, 1),  -- 금붕어
(1, 3, 1),  -- 구피
(1, 4, 1),  -- 네온테트라
(1, 5, 1),  -- 흰동가리
(1, 6, 1),  -- 해마
(1, 7, 1)   -- 복어
ON DUPLICATE KEY UPDATE fish_count = fish_count;  -- MySQL용
-- H2에서는 위 구문이 작동하지 않으므로, 중복 시 에러가 발생할 수 있습니다.
-- 이미 존재하는 항목은 수동으로 제거하거나 무시하세요.

-- RARE 등급 물고기 (8~11)
INSERT INTO collection_entries (collection_id, fish_id, fish_count) VALUES
(1, 8, 1),  -- 가오리
(1, 9, 1),  -- 상어
(1, 10, 1), -- 범고래
(1, 11, 1)  -- 바다거북
ON DUPLICATE KEY UPDATE fish_count = fish_count;

-- LEGENDARY 등급 물고기 (12~13)
INSERT INTO collection_entries (collection_id, fish_id, fish_count) VALUES
(1, 12, 1), -- LEGENDARY 1
(1, 13, 1)  -- LEGENDARY 2
ON DUPLICATE KEY UPDATE fish_count = fish_count;

-- ============================================
-- H2 데이터베이스용 (ON DUPLICATE KEY UPDATE 미지원)
-- ============================================
-- H2를 사용하는 경우, 아래 스크립트를 사용하세요:
-- 
-- DELETE FROM collection_entries WHERE collection_id = 1;
-- 
-- INSERT INTO collection_entries (collection_id, fish_id, fish_count) VALUES
-- (1, 1, 1), (1, 2, 1), (1, 3, 1), (1, 4, 1), (1, 5, 1), (1, 6, 1), (1, 7, 1),
-- (1, 8, 1), (1, 9, 1), (1, 10, 1), (1, 11, 1),
-- (1, 12, 1), (1, 13, 1);





