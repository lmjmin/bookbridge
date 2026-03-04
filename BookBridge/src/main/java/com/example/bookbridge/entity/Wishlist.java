package com.example.bookbridge.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "wishlist",
    uniqueConstraints = @UniqueConstraint(name = "uk_wishlist_user_listing", columnNames = {"user_id","listing_id"}),
    indexes = {
        @Index(name = "idx_wishlist_user", columnList = "user_id"),
        @Index(name = "idx_wishlist_listing", columnList = "listing_id")
    }
)
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 소유 사용자 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 판매글 PK(숫자 저장) */
    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    // ===== Getter / Setter (수동 구현) =====
    public Long getId() { return id; }
    public User getUser() { return user; }
    public Long getListingId() { return listingId; }

    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setListingId(Long listingId) { this.listingId = listingId; }
}
