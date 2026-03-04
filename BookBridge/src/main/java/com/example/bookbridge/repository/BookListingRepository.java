package com.example.bookbridge.repository;

import com.example.bookbridge.entity.BookListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookListingRepository
        extends JpaRepository<BookListing, Long>, JpaSpecificationExecutor<BookListing> {

    /* ===== 공통 ===== */
    List<BookListing> findTop12ByOrderByIdDesc();
    List<BookListing> findByTitleContainingIgnoreCase(String title);
    List<BookListing> findByAuthorContainingIgnoreCase(String author);

    List<BookListing> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrPublisherContainingIgnoreCase(
            String title, String author, String publisher
    );

    List<BookListing> findTop12ByAuthorInOrderByIdDesc(List<String> authors);
    Page<BookListing> findAll(Pageable pageable);

    /* ===== 판매자 기준 카운트 (엔티티 필드 sellerId 추가에 맞춤) ===== */
    long countBySellerId(Long sellerId);

    /* ===== ISBN 정확 매칭 ===== */
    List<BookListing> findTop50ByIsbn(String isbn);

    /* ===== 학과 상세검색(조인): users.major 기준 (native + pageable) ===== */
    @Query(
      value = """
        SELECT b.*
        FROM book_listing b
        JOIN users u ON u.id = b.seller_id
        WHERE (:dept IS NULL OR LOWER(u.major) = LOWER(:dept))
          AND (
            (:q IS NULL OR :q = '')
            OR LOWER(b.title)     LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(b.author)    LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :q, '%'))
          )
          AND (:minPrice IS NULL OR b.price >= :minPrice)
          AND (:maxPrice IS NULL OR b.price <= :maxPrice)
          AND (
            :hasImage IS NULL
            OR (:hasImage = TRUE  AND b.image_url IS NOT NULL AND TRIM(b.image_url) <> '')
            OR (:hasImage = FALSE AND (b.image_url IS NULL OR TRIM(b.image_url) = ''))
          )
          AND (
            :cond IS NULL OR :cond = ''
            OR LOWER(COALESCE(b.condition_text, '')) LIKE LOWER(CONCAT('%', :cond, '%'))
          )
        ORDER BY b.id DESC
      """,
      countQuery = """
        SELECT COUNT(*)
        FROM book_listing b
        JOIN users u ON u.id = b.seller_id
        WHERE (:dept IS NULL OR LOWER(u.major) = LOWER(:dept))
          AND (
            (:q IS NULL OR :q = '')
            OR LOWER(b.title)     LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(b.author)    LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :q, '%'))
          )
          AND (:minPrice IS NULL OR b.price >= :minPrice)
          AND (:maxPrice IS NULL OR b.price <= :maxPrice)
          AND (
            :hasImage IS NULL
            OR (:hasImage = TRUE  AND b.image_url IS NOT NULL AND TRIM(b.image_url) <> '')
            OR (:hasImage = FALSE AND (b.image_url IS NULL OR TRIM(b.image_url) = ''))
          )
          AND (
            :cond IS NULL OR :cond = ''
            OR LOWER(COALESCE(b.condition_text, '')) LIKE LOWER(CONCAT('%', :cond, '%'))
          )
      """,
      nativeQuery = true
    )
    Page<BookListing> searchBySellerMajorWithKeywordAndFilters(
            @Param("dept") String department,
            @Param("q") String q,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("hasImage") Boolean hasImage,
            @Param("cond") String conditionText,
            Pageable pageable
    );

    /* ===== University(호환) — 엔티티의 university 컬럼 사용 ===== */
    @Query("""
           SELECT b FROM BookListing b
           WHERE LOWER(b.university) LIKE LOWER(CONCAT('%', :univ, '%'))
           """)
    List<BookListing> findByUniversityContainingIgnoreCase(@Param("univ") String university);

    @Query("""
           SELECT b FROM BookListing b
           WHERE b.university = :univ
           ORDER BY b.id DESC
           """)
    List<BookListing> findTop12ByUniversityOrderByIdDesc(@Param("univ") String university);

    @Query("""
           SELECT b FROM BookListing b
           WHERE (:univ IS NULL OR b.university = :univ)
           ORDER BY b.id DESC
           """)
    Page<BookListing> findRecentByUniversity(@Param("univ") String university, Pageable pageable);

    @Query("""
           SELECT b FROM BookListing b
           WHERE (:univ IS NULL OR b.university = :univ)
             AND (
                 LOWER(b.title)     LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(b.author)    LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :q, '%'))
             )
           ORDER BY b.id DESC
           """)
    List<BookListing> searchByUniversityAndKeyword(@Param("univ") String university,
                                                   @Param("q") String q);

    @Query("""
           SELECT b FROM BookListing b
           WHERE b.author IN (:authors)
             AND b.university = :univ
           ORDER BY b.id DESC
           """)
    List<BookListing> findTop12ByAuthorInAndUniversityOrderByIdDesc(@Param("authors") List<String> authors,
                                                                    @Param("univ") String university);

    /* =========================================================
       아래 2개가 컨트롤러 호환용 신규 메서드 (추가)
       - findLatest : 홈 최신목록
       - searchAll  : 키워드(제목/저자/출판사/ISBN) + 선택적 university
       ========================================================= */

    @Query("""
        SELECT b FROM BookListing b
        WHERE (:univ IS NULL OR b.university = :univ)
        ORDER BY b.id DESC
    """)
    Page<BookListing> findLatest(@Param("univ") String university, Pageable pageable);

    @Query("""
        SELECT b FROM BookListing b
        WHERE
          (
            :q IS NULL
            OR LOWER(b.title)     LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(b.author)    LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :q, '%'))
            OR REPLACE(b.isbn,'-','') LIKE REPLACE(CONCAT('%', :q, '%'),'-','')
          )
          AND (:univ IS NULL OR b.university = :univ)
        ORDER BY b.id DESC
    """)
    Page<BookListing> searchAll(@Param("q") String keyword,
                                @Param("univ") String university,
                                Pageable pageable);
}
