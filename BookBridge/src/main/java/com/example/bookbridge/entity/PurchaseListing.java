package com.example.bookbridge.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_listings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PurchaseListing {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 구매 희망자(요청자) 이메일 혹은 사용자 id 레퍼런스 */
    @Column(nullable = false)
    private String requesterEmail;

    /** 제목/키워드(교재명 등) */
    @Column(nullable = false, length = 200)
    private String title;

    /** 저자명(추천 연동용) */
    @Column(length = 120)
    private String author;

    /** ISBN(정확 매칭/자동입력 연동용) */
    @Column(length = 20)
    private String isbn;

    /** 학과/전공 태그 */
    @Column(length = 80)
    private String major;

    /** 상태(예: OPEN, MATCHED, CLOSED) */
    @Column(length = 20)
    private String status;

    /** 자유 텍스트(요청 상세) */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 생성/수정 시각 */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) this.status = "OPEN";
    }

    @PreUpdate
    void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
