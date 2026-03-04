package com.example.bookbridge.service;

import com.example.bookbridge.entity.Review;
import com.example.bookbridge.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    /** 컨트롤러 호환: avgRating(Long) */
    @Transactional(readOnly = true)
    public double avgRating(Long userId) {
        Double v = reviewRepository.avgRatingOfUser(userId);
        return v == null ? 0.0 : v;
    }

    /** 컨트롤러 호환: count(Long) */
    @Transactional(readOnly = true)
    public long count(Long userId) {
        return reviewRepository.countForSeller(userId);
    }

    /**
     * 컨트롤러 호환: create(transactionId, reviewerId, sellerId, rating, content)
     * sellerId는 트랜잭션 테이블에서 유도 가능하므로 여기서는 사용하지 않아도 무방.
     * 엔티티에 setReviewerId/setTransactionId가 없어 네이티브 INSERT로 저장.
     */
    @Transactional
    public Review create(Integer transactionId, Long reviewerId, Long sellerId, Integer rating, String content) {
        reviewRepository.insertReview(transactionId, reviewerId, rating, content);
        // 반환값이 필요 없는 호출도 많아 보이므로, 저장된 내용을 간단 Stub로 반환
        Review stub = new Review();
        try {
            // 필드가 존재한다면 설정(없어도 컴파일 되도록 리플렉션은 쓰지 않음)
            stub.setRating(rating);
            stub.setContent(content);
        } catch (Throwable ignore) { }
        return stub;
    }
}
