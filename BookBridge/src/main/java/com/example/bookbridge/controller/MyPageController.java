package com.example.bookbridge.controller;

import com.example.bookbridge.repository.BookListingRepository;
import com.example.bookbridge.repository.PurchaseRepository;
import com.example.bookbridge.service.ReviewService;
import com.example.bookbridge.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/mypage", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MyPageController {

    private final BookListingRepository listingRepo;
    private final PurchaseRepository purchaseRepo;
    private final WishlistService wishlistService;
    private final ReviewService reviewService;

    @GetMapping("/{userId}/summary")
    public ResponseEntity<?> summary(@PathVariable Long userId){
        long sales = listingRepo.countBySellerId(userId);
        long purchases = purchaseRepo.countByBuyerId(userId);
        long wish = wishlistService.list(userId).size();
        double avg = reviewService.avgRating(userId);
        long rcount = reviewService.count(userId);

        return ResponseEntity.ok(Map.of(
            "ok", true,
            "userId", userId,
            "salesCount", sales,
            "purchaseCount", purchases,
            "wishlistCount", wish,
            "ratingAvg", avg,
            "ratingCount", rcount
        ));
    }
}
