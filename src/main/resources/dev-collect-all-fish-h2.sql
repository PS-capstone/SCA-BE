-- ============================================
-- 개발/테스트용: 모든 물고기를 학생 컬렉션에 추가 (H2용)
-- ============================================
-- 이 스크립트는 Student 4 (Hong Gildong)의 컬렉션에
-- 모든 물고기를 추가합니다.
-- 
-- 사용 방법:
-- 1. H2 Console에서 실행: http://localhost:8080/h2-console
-- 2. JDBC URL: jdbc:h2:file:./data/devdb
-- 3. 이 스크립트를 복사하여 실행
-- ============================================

-- 기존 컬렉션 엔트리 삭제 (선택사항 - 모든 물고기를 새로 추가하려면)
-- DELETE FROM collection_entries WHERE collection_id = 1;

-- 모든 물고기를 컬렉션에 추가
-- Student 4 (Hong Gildong)의 collection_id는 1입니다.

-- COMMON 등급 물고기 (fish_id 1~7)
INSERT INTO collection_entries (collection_id, fish_id, fish_count) VALUES
(1, 1, 1),  -- 열대어
(1, 2, 1),  -- 금붕어
(1, 3, 1),  -- 구피
(1, 4, 1),  -- 네온테트라
(1, 5, 1),  -- 흰동가리
(1, 6, 1),  -- 해마
(1, 7, 1);  -- 복어

-- RARE 등급 물고기 (fish_id 8~11)
INSERT INTO collection_entries (collection_id, fish_id, fish_count) VALUES
(1, 8, 1),  -- 가오리
(1, 9, 1),  -- 상어
(1, 10, 1), -- 범고래
(1, 11, 1); -- 바다거북

-- LEGENDARY 등급 물고기 (fish_id 12~13)
INSERT INTO collection_entries (collection_id, fish_id, fish_count) VALUES
(1, 12, 1), -- LEGENDARY 1
(1, 13, 1); -- LEGENDARY 2

-- ============================================
-- 확인 쿼리
-- ============================================
-- SELECT ce.*, f.fish_name, f.grade 
-- FROM collection_entries ce
-- JOIN fish f ON ce.fish_id = f.fish_id
-- WHERE ce.collection_id = 1
-- ORDER BY f.grade, f.fish_id;





