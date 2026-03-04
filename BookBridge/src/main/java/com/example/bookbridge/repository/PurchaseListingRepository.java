package com.example.bookbridge.repository;

import com.example.bookbridge.entity.PurchaseListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseListingRepository extends JpaRepository<PurchaseListing, Long> {
    List<PurchaseListing> findTop50ByOrderByCreatedAtDesc();
    List<PurchaseListing> findByMajorOrderByCreatedAtDesc(String major);
    List<PurchaseListing> findByAuthorOrderByCreatedAtDesc(String author);
    List<PurchaseListing> findByIsbnOrderByCreatedAtDesc(String isbn);
}
