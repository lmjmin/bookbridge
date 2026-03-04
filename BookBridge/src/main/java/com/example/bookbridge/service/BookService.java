package com.example.bookbridge.service;

import com.example.bookbridge.entity.BookListing;
import com.example.bookbridge.repository.BookListingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookListingRepository repo;

    public BookService(BookListingRepository repo) {
        this.repo = repo;
    }

    /** 추천: 최신 12개 */
    public List<BookListing> recommended() {
        return repo.findTop12ByOrderByIdDesc();
    }

    /** 검색: type(title|author) 없으면 통합(제목/저자/출판사) */
    public List<BookListing> search(String q, String type) {
        if (q == null || q.isBlank()) return List.of();

        if ("title".equalsIgnoreCase(type)) {
            return repo.findByTitleContainingIgnoreCase(q);
        } else if ("author".equalsIgnoreCase(type)) {
            return repo.findByAuthorContainingIgnoreCase(q);
        }
        // 통합 검색
        return repo.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrPublisherContainingIgnoreCase(
                q, q, q
        );
    }
}
