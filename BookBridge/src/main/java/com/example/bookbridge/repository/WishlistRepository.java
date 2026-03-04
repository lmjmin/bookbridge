package com.example.bookbridge.repository;

import com.example.bookbridge.entity.User;
import com.example.bookbridge.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUser(User user);
    boolean existsByUserAndListingId(User user, Long listingId);
    Optional<Wishlist> findByUserAndListingId(User user, Long listingId);
    long deleteByUserAndListingId(User user, Long listingId);
}
