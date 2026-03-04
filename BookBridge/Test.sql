/* =========================================================
   BookBridge DB 테스트
   - DB 선택 : USE bookbridge; -> 항상 먼저 실행
   - 1) 테스트 데이터 삽입
   - 2) 기본 조회(검증)
   - 3) 구매 완료 시나리오(양쪽 버튼)
   - 4) 평점 남기기(구매자 → 판매자)
   - 5) 찜/구매목록 예시
   - 6) 삭제
   ---------------------------------------------------------
   사용법: 필요 블록만 드래그 -> Ctrl+Enter (선택 실행)
   ========================================================= */

USE bookbridge;

/* =========================================================
   1) 테스트 데이터 삽입
   --------------------------------------------------------- */

-- 1-1) 기초 유저 2명 (판매자/구매자)
INSERT INTO users (username, email, name, password, school, major, phone, role)
VALUES
('seller01', 'seller01@example.com', '판매자원', '$2a$10$hash', 'OO대학교', '경영학', '010-1111-1111', 'USER'),
('buyer01',  'buyer01@example.com',  '구매자원', '$2a$10$hash', 'OO대학교', '컴퓨터공학', '010-2222-2222', 'USER');

-- (확인) 방금 넣은 유저 id
SELECT id, username FROM users WHERE username IN ('seller01','buyer01');

-- 1-2) 판매글 1건 (seller01이 등록)
INSERT INTO book_listings (seller_id, title, author, publisher, isbn, price, condition_text, image_url, university)
VALUES (
  (SELECT id FROM users WHERE username='seller01'),
  '객체지향의 사실과 오해', '조영호', '위키북스', '9791158390174', 12000,
  '겉표지 사용감 약간, 필기 없음', NULL, 'OO대학교'
);

-- (확인) 최신 판매글 id
SELECT id, title, seller_id FROM book_listings ORDER BY id DESC LIMIT 1;

-- 1-3) DM 쓰레드 1건 (buyer01 ↔ seller01, 위 판매글 기준)
INSERT INTO dm_threads (buyer_id, seller_id, listing_id, last_message_at, last_message)
VALUES (
  (SELECT id FROM users WHERE username='buyer01'),
  (SELECT id FROM users WHERE username='seller01'),
  (SELECT id FROM book_listings ORDER BY id DESC LIMIT 1),
  NOW(),
  '안녕하세요, 구매 가능한가요?'
);

-- (확인) 최신 쓰레드 id
SELECT id, buyer_id, seller_id, listing_id FROM dm_threads ORDER BY id DESC LIMIT 1;

-- 1-4) 메시지 3개 (대화 내용)
INSERT INTO dm_messages (thread_id, sender_id, content)
VALUES
((SELECT id FROM dm_threads ORDER BY id DESC LIMIT 1),
 (SELECT id FROM users WHERE username='buyer01'),
 '안녕하세요, 구매 가능한가요?');

INSERT INTO dm_messages (thread_id, sender_id, content)
VALUES
((SELECT id FROM dm_threads ORDER BY id DESC LIMIT 1),
 (SELECT id FROM users WHERE username='seller01'),
 '네, 가능해요. 오늘 학교 앞에서 거래하실래요?');

INSERT INTO dm_messages (thread_id, sender_id, content)
VALUES
((SELECT id FROM dm_threads ORDER BY id DESC LIMIT 1),
 (SELECT id FROM users WHERE username='buyer01'),
 '좋아요! 5시에 뵐게요.');

-- (선택) 쓰레드 요약 갱신
UPDATE dm_threads
SET last_message_at = NOW(),
    last_message = '좋아요! 5시에 뵐게요.'
WHERE id = (SELECT id FROM dm_threads ORDER BY id DESC LIMIT 1);
    -- 위의 쓰레드 요약 갱신에서 에러나면 : You can't specify target table 'dm_threads' for update in FROM clause
    -- 최신 thread id를 안전하게 변수에 저장
    SELECT @tid := id FROM dm_threads ORDER BY id DESC LIMIT 1;
    -- 변수로 업데이트 (같은 테이블 중복참조 없음)
    UPDATE dm_threads
    SET last_message_at = NOW(),
        last_message    = '좋아요! 5시에 뵐게요.'
    WHERE id = @tid;

-- 1-5) 거래 레코드 1건 (대화/판매글 연결, 초기상태=PENDING)
INSERT INTO transactions (thread_id, listing_id, buyer_id, seller_id)
VALUES (
  (SELECT id FROM dm_threads ORDER BY id DESC LIMIT 1),
  (SELECT id FROM book_listings ORDER BY id DESC LIMIT 1),
  (SELECT id FROM users WHERE username='buyer01'),
  (SELECT id FROM users WHERE username='seller01')
);

-- (확인) 거래 상태
SELECT id, status, buyer_confirm, seller_confirm
FROM transactions ORDER BY id DESC LIMIT 1;



/* =========================================================
   2) 기본 조회(검증)
   --------------------------------------------------------- */

-- 2-1) 최근 쓰레드 목록
SELECT
  t.id AS thread_id,
  bu.username AS buyer,
  se.username AS seller,
  l.title AS listing_title,
  t.last_message,
  t.last_message_at
FROM dm_threads t
JOIN users bu ON bu.id = t.buyer_id
JOIN users se ON se.id = t.seller_id
LEFT JOIN book_listings l ON l.id = t.listing_id
ORDER BY t.last_message_at DESC;

-- 2-2) 선택 쓰레드의 메시지 타임라인
SELECT
  m.thread_id,
  u.username AS sender,
  m.content,
  m.sent_at
FROM dm_messages m
JOIN users u ON u.id = m.sender_id
WHERE m.thread_id = (SELECT id FROM dm_threads ORDER BY id DESC LIMIT 1)
ORDER BY m.sent_at ASC;

-- 2-3) 거래 + 상태 확인
SELECT
  tr.id AS tx_id, tr.status, tr.buyer_confirm, tr.seller_confirm, tr.completed_at,
  l.title, bu.username AS buyer, se.username AS seller
FROM transactions tr
JOIN book_listings l ON l.id = tr.listing_id
JOIN users bu ON bu.id = tr.buyer_id
JOIN users se ON se.id = tr.seller_id
ORDER BY tr.id DESC;



/* =========================================================
   3) 구매 완료 시나리오 (양쪽 버튼)
   --------------------------------------------------------- */

-- 3-1) 구매자 완료 버튼 클릭
UPDATE transactions
SET buyer_confirm = 1
WHERE id = (SELECT id FROM transactions ORDER BY id DESC LIMIT 1);

-- 3-2) 판매자 완료 버튼 클릭
UPDATE transactions
SET seller_confirm = 1
WHERE id = (SELECT id FROM transactions ORDER BY id DESC LIMIT 1);

-- 3-3) 양쪽 다 완료되면 거래 확정(COMPLETED)
UPDATE transactions
SET status = 'COMPLETED',
    completed_at = NOW()
WHERE id = (SELECT id FROM transactions ORDER BY id DESC LIMIT 1)
  AND buyer_confirm = 1
  AND seller_confirm = 1;

-- (확인) 거래 최종 상태
SELECT id, status, buyer_confirm, seller_confirm, completed_at
FROM transactions ORDER BY id DESC LIMIT 1;



/* =========================================================
   4) 평점 남기기 (구매자 → 판매자)
   --------------------------------------------------------- */

-- 4-1) 거래 COMPLETED 조건 하에 리뷰 1회 등록
INSERT INTO reviews (transaction_id, reviewer_id, target_user_id, rating, content)
SELECT
  tr.id,
  tr.buyer_id,        -- 작성자(구매자)
  tr.seller_id,       -- 대상(판매자)
  5,                  -- 별점
  '응대가 빠르고 책 상태도 좋았습니다!'
FROM transactions tr
WHERE tr.id = (SELECT id FROM transactions ORDER BY id DESC LIMIT 1)
  AND tr.status = 'COMPLETED';

-- 4-2) 리뷰 상세 확인
SELECT
  r.id, r.rating, r.content, r.created_at,
  reviewer.username AS reviewer,
  target.username   AS target
FROM reviews r
JOIN users reviewer ON reviewer.id = r.reviewer_id
JOIN users target   ON target.id = r.target_user_id
ORDER BY r.id DESC;

-- 4-3) 판매자(타겟) 평점 요약 뷰 확인
SELECT u.username, s.review_count, ROUND(s.avg_rating,2) AS avg_rating
FROM v_user_rating_summary s
JOIN users u ON u.id = s.user_id
WHERE u.username = 'seller01';



/* =========================================================
   5) 찜 / 구매목록 예시
   --------------------------------------------------------- */

-- 5-1) 찜 추가 (buyer01이 방금 판매글 찜)
INSERT INTO wishlist (user_id, listing_id)
VALUES (
  (SELECT id FROM users WHERE username='buyer01'),
  (SELECT id FROM book_listings ORDER BY id DESC LIMIT 1)
);

-- 5-2) 내 찜 목록 조회 (buyer01)
SELECT w.id, l.title, l.price, w.created_at
FROM wishlist w
JOIN book_listings l ON l.id = w.listing_id
WHERE w.user_id = (SELECT id FROM users WHERE username='buyer01')
ORDER BY w.created_at DESC;



/* =========================================================
   6) 삭제
   - FK 제약 때문에 리뷰 → 거래 → 메시지 → 쓰레드 → 찜 → 판매글 → 유저 순
   --------------------------------------------------------- */

-- 6-1) 리뷰 삭제
DELETE r FROM reviews r
JOIN transactions tr ON tr.id = r.transaction_id
WHERE tr.buyer_id  = (SELECT id FROM users WHERE username='buyer01')
  AND tr.seller_id = (SELECT id FROM users WHERE username='seller01');

-- 6-2) 거래 삭제
DELETE FROM transactions
WHERE buyer_id  = (SELECT id FROM users WHERE username='buyer01')
  AND seller_id = (SELECT id FROM users WHERE username='seller01');

-- 6-3) 메시지 삭제
DELETE m FROM dm_messages m
JOIN dm_threads t ON t.id = m.thread_id
WHERE t.buyer_id  = (SELECT id FROM users WHERE username='buyer01')
  AND t.seller_id = (SELECT id FROM users WHERE username='seller01');

-- 6-4) 쓰레드 삭제
DELETE FROM dm_threads
WHERE buyer_id  = (SELECT id FROM users WHERE username='buyer01')
  AND seller_id = (SELECT id FROM users WHERE username='seller01');

-- 6-5) 찜 삭제
DELETE w FROM wishlist w
WHERE w.user_id = (SELECT id FROM users WHERE username='buyer01');

-- 6-6) 판매글 삭제
DELETE FROM book_listings
WHERE seller_id = (SELECT id FROM users WHERE username='seller01');

-- 6-7) 유저 삭제
DELETE FROM users WHERE username IN ('buyer01','seller01');

-- 6-8) 테이블별 카운트 확인 : 모두 0이면 다 삭제된 것
SELECT COUNT(*) AS users FROM users;
SELECT COUNT(*) AS listings FROM book_listings;
SELECT COUNT(*) AS threads FROM dm_threads;
SELECT COUNT(*) AS msgs FROM dm_messages;
SELECT COUNT(*) AS txs FROM transactions;
SELECT COUNT(*) AS revs FROM reviews;
SELECT COUNT(*) AS wish FROM wishlist;