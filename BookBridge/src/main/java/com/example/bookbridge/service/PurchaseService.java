package com.example.bookbridge.service;

import com.example.bookbridge.entity.Purchase;
import com.example.bookbridge.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepo;

    @Transactional
    public Purchase createPaidOrder(Long userId, Long listingId, Integer price){
        if (userId == null || listingId == null)
            throw new IllegalArgumentException("userId/listingId 누락");

        Purchase p = new Purchase();
        p.setUserId(userId);
        p.setListingId(listingId);
        p.setPrice(price != null ? price : 0);
        p.setStatus("PAID");
        p.setCreatedAt(LocalDateTime.now());
        return purchaseRepo.save(p);
    }

    public List<Purchase> myPurchases(Long userId){
        return purchaseRepo.findByUserIdOrderByIdDesc(userId);
    }
}
