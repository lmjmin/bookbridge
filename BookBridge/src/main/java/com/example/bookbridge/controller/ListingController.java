package com.example.bookbridge.controller;

import com.example.bookbridge.dto.CreateListingRequest;
import com.example.bookbridge.entity.BookListing;
import com.example.bookbridge.repository.BookListingRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final BookListingRepository repo;

    public ListingController(BookListingRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid CreateListingRequest req,
                                    HttpSession session) {
        Object uid = session.getAttribute("uid");
        if (uid == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("ok", false, "message", "로그인이 필요합니다."));
        }
        if (req.getPrice() == null || req.getPrice() < 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("ok", false, "message", "가격을 올바르게 입력하세요."));
        }

        BookListing b = new BookListing();
        b.setTitle(s(req.getTitle()));
        b.setAuthor(s(req.getAuthor()));
        b.setPublisher(s(req.getPublisher()));
        b.setIsbn(s(req.getIsbn()));
        b.setPrice(req.getPrice());
        b.setImageUrl(s(req.getImageUrl()));

        // 엔티티 필드에 맞게
        b.setConditionText(s(req.getConditionText()));
        b.setSellerPhone(s(req.getSellerPhone()));
        b.setUniversity(s(req.getUniversity()));

        // 세션 uid 저장
        b.setSellerId(Long.valueOf(String.valueOf(uid)));

        BookListing saved = repo.save(b);
        return ResponseEntity.ok(Map.of("ok", true, "id", saved.getId()));
    }

    /** ❗ 삭제 API – 일단 권한 체크 없이 "삭제만 잘 되게" */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.status(404)
                    .body(Map.of("ok", false, "message", "판매글을 찾을 수 없습니다."));
        }
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    private static String s(String v) {
        return v == null ? "" : v.trim();
    }
}
