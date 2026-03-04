package com.example.bookbridge.controller;

import com.example.bookbridge.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/wishlist", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    private Long toLong(Object v){
        if (v == null) return null;
        try { return Long.parseLong(v.toString()); } catch (Exception e){ return null; }
    }

    /** 내 찜 목록 listingId 배열 */
    @GetMapping
    public ResponseEntity<?> list(@RequestParam("userId") Long userId){
        var list = wishlistService.list(userId).stream()
                .map(w -> w.getListingId())
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("ok", true, "items", list));
    }

    /** 추가 */
    @PostMapping(path="/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> add(@RequestBody Map<String, Object> body){
        Long userId = toLong(body.get("userId"));
        Long listingId = toLong(body.get("listingId"));
        if (userId == null || listingId == null)
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "userId/listingId 누락"));
        wishlistService.add(userId, listingId);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /** 제거 */
    @PostMapping(path="/remove", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> remove(@RequestBody Map<String, Object> body){
        Long userId = toLong(body.get("userId"));
        Long listingId = toLong(body.get("listingId"));
        if (userId == null || listingId == null)
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "userId/listingId 누락"));
        wishlistService.remove(userId, listingId);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /** 토글 (추가→true, 제거→false) */
    @PostMapping(path="/toggle", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> toggle(@RequestBody Map<String, Object> body){
        Long userId = toLong(body.get("userId"));
        Long listingId = toLong(body.get("listingId"));
        if (userId == null || listingId == null)
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "userId/listingId 누락"));
        boolean nowLiked = wishlistService.toggle(userId, listingId);
        return ResponseEntity.ok(Map.of("ok", true, "liked", nowLiked));
    }
}
