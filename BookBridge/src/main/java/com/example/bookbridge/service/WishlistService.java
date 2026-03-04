package com.example.bookbridge.service;

import com.example.bookbridge.entity.User;
import com.example.bookbridge.entity.Wishlist;
import com.example.bookbridge.repository.UserRepository;
import com.example.bookbridge.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;

    public WishlistService(WishlistRepository wishlistRepository, UserRepository userRepository) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
    }

    /** userId로 User 조회 (없으면 IllegalArgumentException) */
    private User getUserOrThrow(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다: " + userId));
    }

    @Transactional(readOnly = true)
    public List<Wishlist> list(Long userId){
        User u = getUserOrThrow(userId);
        return wishlistRepository.findByUser(u);
    }

    @Transactional
    public void add(Long userId, Long listingId){
        User u = getUserOrThrow(userId);
        if (!wishlistRepository.existsByUserAndListingId(u, listingId)) {
            Wishlist w = new Wishlist();
            w.setUser(u);
            w.setListingId(listingId);
            wishlistRepository.save(w);
        }
    }

    @Transactional
    public void remove(Long userId, Long listingId){
        User u = getUserOrThrow(userId);
        wishlistRepository.deleteByUserAndListingId(u, listingId);
    }

    /** 토글(추가되면 true, 제거되면 false 반환) */
    @Transactional
    public boolean toggle(Long userId, Long listingId){
        User u = getUserOrThrow(userId);
        return wishlistRepository.findByUserAndListingId(u, listingId)
                .map(w -> { wishlistRepository.delete(w); return false; }) // existed -> now removed
                .orElseGet(() -> {
                    Wishlist w = new Wishlist();
                    w.setUser(u);
                    w.setListingId(listingId);
                    wishlistRepository.save(w);
                    return true; // created
                });
    }
}
