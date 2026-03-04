package com.example.bookbridge.controller;

import com.example.bookbridge.entity.BookListing;
import com.example.bookbridge.repository.BookListingRepository;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books-adv")
public class AdvancedSearchController {

    private final BookListingRepository repo;
    public AdvancedSearchController(BookListingRepository repo) { this.repo = repo; }

    /** 모바일 카메라/직접입력 ISBN → 정확 매칭 */
    @GetMapping("/by-isbn")
    public List<BookListing> byIsbn(@RequestParam String isbn) {
        String normalized = (isbn == null) ? "" : isbn.replaceAll("[^0-9Xx]", "");
        if (normalized.isBlank()) return List.of();
        return repo.findTop50ByIsbn(normalized);
    }

    /** 상세검색: users.major 조인 + 키워드/가격/이미지/설명 필터 */
    @GetMapping("/filter")
    public Page<BookListing> filter(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Boolean hasImage,
            @RequestParam(required = false) String conditionText,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(100, Math.max(1, size)),
                Sort.by(Sort.Direction.DESC, "id")
        );
        String kw   = (q == null) ? "" : q.trim();
        String cond = (conditionText == null) ? "" : conditionText.trim();
        String dept = (department == null || department.isBlank()) ? null : department.trim();

        // ❗︎ Repository 메서드 이름 정확히 일치 (searchWithFilters 아님)
        return repo.searchBySellerMajorWithKeywordAndFilters(
                dept, kw, minPrice, maxPrice, hasImage, cond, pageable
        );
    }
}
