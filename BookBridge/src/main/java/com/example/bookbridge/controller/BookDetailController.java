package com.example.bookbridge.controller;

import com.example.bookbridge.entity.BookListing;
import com.example.bookbridge.entity.User;
import com.example.bookbridge.repository.BookListingRepository;
import com.example.bookbridge.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
public class BookDetailController {

    private final BookListingRepository listings;
    private final UserRepository users;
    private final EntityManager em;

    public BookDetailController(BookListingRepository listings,
                                UserRepository users,
                                EntityManager em) {
        this.listings = listings;
        this.users = users;
        this.em = em;
    }

    /**
     * GET /api/books/{idOrIsbn}
     * - 숫자면 id, 그 외는 ISBN으로 최신 1건 조회
     * - 응답에 seller(공개정보) 포함
     */
    @GetMapping("/{idOrIsbn}")
    public ResponseEntity<?> get(@PathVariable String idOrIsbn) {
        if (!StringUtils.hasText(idOrIsbn)) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("ok", false);
            err.put("message", "id가 비어있음");
            return ResponseEntity.badRequest().body(err);
        }

        BookListing b = null;

        // 숫자만으로 구성되어 있으면 id로 판단
        if (idOrIsbn.chars().allMatch(Character::isDigit)) {
            b = listings.findById(Long.valueOf(idOrIsbn)).orElse(null);
        } else {
            TypedQuery<BookListing> q = em.createQuery(
                    "SELECT b FROM BookListing b WHERE b.isbn = :isbn ORDER BY b.id DESC",
                    BookListing.class
            );
            q.setParameter("isbn", idOrIsbn);
            q.setMaxResults(1);
            b = q.getResultList().stream().findFirst().orElse(null);
        }

        if (b == null) {
            Map<String, Object> notFound = new LinkedHashMap<>();
            notFound.put("ok", false);
            notFound.put("message", "not found");
            return ResponseEntity.status(404).body(notFound);
        }

        // seller 공개정보
        Map<String, Object> seller = new LinkedHashMap<>();
        Optional<User> u = Optional.ofNullable(b.getSellerId()).flatMap(users::findById);
        if (u.isPresent()) {
            User s = u.get();
            seller.put("id", s.getId());
            seller.put("name", s.getName());
            seller.put("email", s.getEmail());
            seller.put("school", s.getSchool());
            seller.put("major", s.getMajor());
            seller.put("phone", s.getPhone());
        }

        // 본문 조립 (Map.of 대신 LinkedHashMap 사용)
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", b.getId());
        body.put("title", b.getTitle());
        body.put("author", b.getAuthor());
        body.put("isbn", b.getIsbn());
        body.put("publisher", b.getPublisher());
        body.put("price", b.getPrice());                 // Integer/BigDecimal 어느쪽이든 Object로 담김
        body.put("imageUrl", b.getImageUrl());
        body.put("conditionText", b.getConditionText());
        body.put("sellerId", b.getSellerId());
        body.put("sellerPhone", b.getSellerPhone());
        body.put("university", b.getUniversity());
        body.put("createdAt", b.getCreatedAt());
        body.put("seller", seller);

        return ResponseEntity.ok(body);
    }
}
