package com.example.bookbridge.controller;

import com.example.bookbridge.entity.BookListing;
import com.example.bookbridge.repository.BookListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class ReadOnlyCatalogController {

    private final BookListingRepository repo;

    /** 홈에 뿌릴 최신 12건 (읽기 전용) */
    @GetMapping("/latest")
    public List<BookListing> latest() {
        return repo.findTop12ByOrderByIdDesc();
    }
}
