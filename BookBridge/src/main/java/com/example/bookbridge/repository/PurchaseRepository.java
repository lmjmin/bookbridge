package com.example.bookbridge.repository;

import com.example.bookbridge.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    /* ========= 현재 서비스/컨트롤러 호출들 모두 호환 =========
       - findByUserIdOrderByIdDesc(Long)      ← 서비스에서 호출
       - findByBuyer_IdOrderByIdDesc(Long)    ← 과거 코드 호환(연관필드 없는 상태라 native로 처리)
       - countByBuyerId(Long)                 ← 마이페이지 집계에서 호출
     */

    // 1) 엔티티에 userId 필드가 있으므로 파생쿼리로 처리 가능
    List<Purchase> findByUserIdOrderByIdDesc(Long userId);

    // 2) 과거 "buyer.id" 스타일 호출을 그대로 살리되, 실제는 user_id 로 질의
    @Query(value = """
            SELECT *
            FROM purchase
            WHERE user_id = :userId
            ORDER BY id DESC
            """, nativeQuery = true)
    List<Purchase> findByBuyer_IdOrderByIdDesc(@Param("userId") Long userId);

    // 3) 마이페이지 집계용 – 메서드명은 기존 그대로 두고 user_id 카운팅
    @Query(value = "SELECT COUNT(*) FROM purchase WHERE user_id = :userId", nativeQuery = true)
    long countByBuyerId(@Param("userId") Long userId);
}
