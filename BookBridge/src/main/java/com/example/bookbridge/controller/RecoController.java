package com.example.bookbridge.controller;

import com.example.bookbridge.entity.UserActivity;
import com.example.bookbridge.entity.BookListing;
import com.example.bookbridge.entity.User;
import com.example.bookbridge.repository.UserActivityRepository;
import com.example.bookbridge.repository.BookListingRepository;
import com.example.bookbridge.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books/reco")
public class RecoController {

    private final UserActivityRepository activityRepo;
    private final BookListingRepository bookRepo;
    private final UserRepository userRepo;

    public RecoController(UserActivityRepository activityRepo,
                          BookListingRepository bookRepo,
                          UserRepository userRepo) {
        this.activityRepo = activityRepo;
        this.bookRepo = bookRepo;
        this.userRepo = userRepo;
    }

    /** 추천 결과에 새 목록을 중복 없이 최대 limit 까지만 추가 */
    private void appendUnique(List<BookListing> dest,
                              Set<Long> usedIds,
                              List<BookListing> src,
                              int limit) {
        if (src == null) return;
        for (BookListing b : src) {
            if (b == null || b.getId() == null) continue;
            if (usedIds.contains(b.getId())) continue;
            usedIds.add(b.getId());
            dest.add(b);
            if (dest.size() >= limit) break;
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> recommend(@PathVariable String userId,
                                       @RequestParam(value = "univ", required = false) String univOpt) {

        final int LIMIT = 12;

        // 0. 로그인 사용자 정보에서 학교 가져오기
        User user = userRepo.findByEmail(userId).orElse(null);
        String school = null;

        if (user != null && user.getSchool() != null && !user.getSchool().isBlank()) {
            school = user.getSchool().trim();
        }
        // 쿼리스트링으로 학교를 넘겨주면 (?univ=...) 그걸 우선 사용
        if (univOpt != null && !univOpt.isBlank()) {
            school = univOpt.trim();
        }

        // 1. 최근 활동 50개 기준으로 많이 본 저자 TOP3 뽑기
        var acts = activityRepo.findTop50ByUserIdOrderByViewedAtDesc(userId);

        var topAuthors = acts.stream()
                .map(UserActivity::getAuthor)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.groupingBy(author -> author, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        // 2. 결과를 채워나가기: 학교 → 저자 → 전체 최신순
        List<BookListing> result = new ArrayList<>();
        Set<Long> usedIds = new HashSet<>();

        // 2-1. 같은 학교 최신 책 (searchAll 사용, keyword = null)
        if (school != null && !school.isBlank() && result.size() < LIMIT) {
            var page = bookRepo.searchAll(null, school, PageRequest.of(0, LIMIT));
            appendUnique(result, usedIds, page.getContent(), LIMIT);
        }

        // 2-2. 활동 기반 추천 (많이 본 저자)
        if (!topAuthors.isEmpty() && result.size() < LIMIT) {
            var authorRecs = bookRepo.findTop12ByAuthorInOrderByIdDesc(topAuthors);
            appendUnique(result, usedIds, authorRecs, LIMIT);
        }

        // 3. fallback: 아직 12개가 안 채워졌으면 전체 최신 책으로 보충
        if (result.isEmpty() || result.size() < LIMIT) {
            var latest = bookRepo.findTop12ByOrderByIdDesc();
            appendUnique(result, usedIds, latest, LIMIT);
        }

        return ResponseEntity.ok(result);
    }
}
