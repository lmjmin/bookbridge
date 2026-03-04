package com.example.bookbridge.repository;

import com.example.bookbridge.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    /** 판매자(userId)가 받은 평균 평점 */
    @Query(value = """
            SELECT COALESCE(AVG(r.rating), 0)
            FROM review r
            JOIN transaction t ON r.transaction_id = t.transaction_id
            WHERE t.seller_id = :userId
            """, nativeQuery = true)
    Double avgRatingOfUser(@Param("userId") Long userId);

    /** 판매자(userId)가 받은 리뷰 개수 */
    @Query(value = """
            SELECT COUNT(*)
            FROM review r
            JOIN transaction t ON r.transaction_id = t.transaction_id
            WHERE t.seller_id = :userId
            """, nativeQuery = true)
    long countForSeller(@Param("userId") Long userId);

    /** 판매자(userId)가 받은 리뷰 리스트(옵션, 필요 시 사용) */
    @Query(value = """
            SELECT r.*
            FROM review r
            JOIN transaction t ON r.transaction_id = t.transaction_id
            WHERE t.seller_id = :userId
            ORDER BY r.created_at DESC
            """, nativeQuery = true)
    List<Review> findBySeller(@Param("userId") Long userId);

    /** 리뷰 생성 – 엔티티에 reviewerId/transactionId 세터가 없어도 저장 가능하도록 네이티브 INSERT */
    @Modifying
    @Query(value = """
            INSERT INTO review (reviewer_id, transaction_id, rating, content, created_at)
            VALUES (:reviewerId, :transactionId, :rating, :content, CURRENT_TIMESTAMP)
            """, nativeQuery = true)
    int insertReview(@Param("transactionId") Integer transactionId,
                     @Param("reviewerId")   Long reviewerId,
                     @Param("rating")       Integer rating,
                     @Param("content")      String content);
}
