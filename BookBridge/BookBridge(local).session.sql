-- 최종 스키마 (users.major 사용, book_listings는 department 없어도 됨)
USE bookbridge;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1) 사용자
CREATE TABLE IF NOT EXISTS users (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  username    VARCHAR(50)  NOT NULL UNIQUE,
  email       VARCHAR(120) NOT NULL UNIQUE,
  name        VARCHAR(50)  NOT NULL,
  password    VARCHAR(100) NOT NULL,
  school      VARCHAR(100),
  major       VARCHAR(100),
  phone       VARCHAR(20),
  role        VARCHAR(20)  NOT NULL DEFAULT 'USER',
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_users_username (username),
  INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) 도서 메타
CREATE TABLE IF NOT EXISTS books (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  title      VARCHAR(255),
  author     VARCHAR(255),
  isbn       VARCHAR(32),
  publisher  VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3) 판매글
CREATE TABLE IF NOT EXISTS book_listings (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  seller_id       BIGINT       NOT NULL,
  title           VARCHAR(255) NOT NULL,
  author          VARCHAR(255),
  publisher       VARCHAR(255),
  isbn            VARCHAR(32),
  price           INT          NOT NULL,
  condition_text  VARCHAR(255),
  image_url       VARCHAR(500),
  university      VARCHAR(120),    -- (과거 필드, 현재는 users.major로 필터)
  department      VARCHAR(100),    -- 선택: 나중에 도입하려면 유지 (현재 사용 안 해도 무방)
  status          VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_listings_seller (seller_id),
  INDEX idx_listings_created (created_at),
  INDEX idx_listings_title (title),
  INDEX idx_listings_author (author),
  INDEX idx_listings_publisher (publisher),
  CONSTRAINT fk_listing_seller
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4) 찜
CREATE TABLE IF NOT EXISTS wishlist (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT NOT NULL,
  listing_id  BIGINT NOT NULL,
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_wishlist_user_listing UNIQUE (user_id, listing_id),
  INDEX idx_wishlist_user (user_id),
  INDEX idx_wishlist_listing (listing_id),
  CONSTRAINT fk_wishlist_user    FOREIGN KEY (user_id)    REFERENCES users(id)         ON DELETE CASCADE,
  CONSTRAINT fk_wishlist_listing FOREIGN KEY (listing_id) REFERENCES book_listings(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5) DM 쓰레드
CREATE TABLE IF NOT EXISTS dm_threads (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  buyer_id         BIGINT NOT NULL,
  seller_id        BIGINT NOT NULL,
  listing_id       BIGINT NULL,
  last_message_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_message     VARCHAR(255) NULL,
  unread_buyer     INT NOT NULL DEFAULT 0,
  unread_seller    INT NOT NULL DEFAULT 0,
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_participants CHECK (buyer_id <> seller_id),
  CONSTRAINT uk_thread_once UNIQUE (buyer_id, seller_id, listing_id),
  INDEX idx_thread_buyer   (buyer_id, last_message_at),
  INDEX idx_thread_seller  (seller_id, last_message_at),
  INDEX idx_thread_listing (listing_id),
  CONSTRAINT fk_thread_buyer   FOREIGN KEY (buyer_id)   REFERENCES users(id)          ON DELETE CASCADE,
  CONSTRAINT fk_thread_seller  FOREIGN KEY (seller_id)  REFERENCES users(id)          ON DELETE CASCADE,
  CONSTRAINT fk_thread_listing FOREIGN KEY (listing_id) REFERENCES book_listings(id)  ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6) DM 메시지
CREATE TABLE IF NOT EXISTS dm_messages (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  thread_id   BIGINT NOT NULL,
  sender_id   BIGINT NOT NULL,
  content     TEXT   NOT NULL,
  sent_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  read_at     DATETIME NULL,
  INDEX idx_dm_thread_time (thread_id, sent_at),
  INDEX idx_dm_sender      (sender_id),
  CONSTRAINT fk_dm_thread FOREIGN KEY (thread_id) REFERENCES dm_threads(id) ON DELETE CASCADE,
  CONSTRAINT fk_dm_sender FOREIGN KEY (sender_id) REFERENCES users(id)      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7) 거래
CREATE TABLE IF NOT EXISTS transactions (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  thread_id      BIGINT NOT NULL,
  listing_id     BIGINT NOT NULL,
  buyer_id       BIGINT NOT NULL,
  seller_id      BIGINT NOT NULL,
  status         VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  buyer_confirm  TINYINT(1) NOT NULL DEFAULT 0,
  seller_confirm TINYINT(1) NOT NULL DEFAULT 0,
  completed_at   DATETIME NULL,
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_tx_once UNIQUE (thread_id),
  INDEX idx_tx_buyer   (buyer_id, status, created_at),
  INDEX idx_tx_seller  (seller_id, status, created_at),
  INDEX idx_tx_listing (listing_id),
  CONSTRAINT fk_tx_thread  FOREIGN KEY (thread_id)  REFERENCES dm_threads(id)    ON DELETE RESTRICT,
  CONSTRAINT fk_tx_listing FOREIGN KEY (listing_id) REFERENCES book_listings(id) ON DELETE RESTRICT,
  CONSTRAINT fk_tx_buyer   FOREIGN KEY (buyer_id)   REFERENCES users(id)         ON DELETE CASCADE,
  CONSTRAINT fk_tx_seller  FOREIGN KEY (seller_id)  REFERENCES users(id)         ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8) 리뷰
CREATE TABLE IF NOT EXISTS reviews (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  transaction_id BIGINT NOT NULL,
  reviewer_id    BIGINT NOT NULL,
  target_user_id BIGINT NOT NULL,
  rating         INT    NOT NULL,
  content        TEXT   NULL,
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_rating CHECK (rating BETWEEN 1 AND 5),
  CONSTRAINT uk_review_once UNIQUE (transaction_id, reviewer_id),
  INDEX idx_reviews_target (target_user_id, created_at),
  INDEX idx_reviews_tx     (transaction_id),
  CONSTRAINT fk_review_tx     FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
  CONSTRAINT fk_review_author FOREIGN KEY (reviewer_id)    REFERENCES users(id)        ON DELETE CASCADE,
  CONSTRAINT fk_review_target FOREIGN KEY (target_user_id) REFERENCES users(id)        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 평점 요약 뷰
CREATE OR REPLACE VIEW v_user_rating_summary AS
SELECT
  target_user_id AS user_id,
  COUNT(*)       AS review_count,
  AVG(rating)    AS avg_rating
FROM reviews
GROUP BY target_user_id;

SET FOREIGN_KEY_CHECKS = 1;

USE bookbridge;
SET FOREIGN_KEY_CHECKS = 0;