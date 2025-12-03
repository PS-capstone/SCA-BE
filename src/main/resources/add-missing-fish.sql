-- ============================================
-- 빠진 물고기만 컬렉션에 추가
-- Student 4 (Hong Gildong)의 collection_id는 1입니다.
-- ============================================
-- 사용 방법:
-- 1. H2 Console 접속: http://localhost:8080/h2-console
-- 2. JDBC URL: jdbc:h2:file:/Users/baegseoyeon/Desktop/sea_V2/SCA-BE/data/devdb
-- 3. Username: sa
-- 4. Password: (비워두기)
-- 5. 이 SQL을 복사하여 실행
-- ============================================

-- 빠진 물고기만 추가 (이미 있는 것은 무시)
-- RARE 등급 물고기
INSERT INTO collection_entries (collection_id, fish_id, fish_count) VALUES
(1, 8, 1),  -- 바다거북
(1, 11, 1)  -- 전기뱀장어
ON CONFLICT DO NOTHING;  -- H2는 이 구문을 지원하지 않으므로, 중복 시 에러 무시

-- LEGENDARY 등급 물고기
INSERT INTO collection_entries (collection_id, fish_id, fish_count) VALUES
(1, 12, 1)  -- 바다해룡
ON CONFLICT DO NOTHING;

-- ============================================
-- H2용 (ON CONFLICT 미지원)
-- ============================================
-- H2에서는 위 구문이 작동하지 않으므로, 아래 방법 사용:

-- 방법 1: 기존 항목 삭제 후 다시 추가 (안전)
-- DELETE FROM collection_entries WHERE collection_id = 1 AND fish_id IN (8, 11, 12);
-- INSERT INTO collection_entries (collection_id, fish_id, fish_count) VALUES
-- (1, 8, 1),  -- 바다거북
-- (1, 11, 1), -- 전기뱀장어
-- (1, 12, 1); -- 바다해룡

-- 방법 2: 직접 확인 후 추가 (권장)
-- 먼저 확인:
-- SELECT ce.fish_id, f.fish_name 
-- FROM collection_entries ce
-- JOIN fish f ON ce.fish_id = f.fish_id
-- WHERE ce.collection_id = 1 AND ce.fish_id IN (8, 11, 12);

-- 빠진 것만 추가:
INSERT INTO collection_entries (collection_id, fish_id, fish_count) VALUES
(1, 8, 1),  -- 바다거북
(1, 11, 1), -- 전기뱀장어
(1, 12, 1); -- 바다해룡

-- ============================================
-- 확인 쿼리
-- ============================================
-- SELECT ce.fish_id, f.fish_name, f.grade, ce.fish_count
-- FROM collection_entries ce
-- JOIN fish f ON ce.fish_id = f.fish_id
-- WHERE ce.collection_id = 1
-- ORDER BY f.fish_id;





