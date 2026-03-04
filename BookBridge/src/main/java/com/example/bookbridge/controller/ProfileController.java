package com.example.bookbridge.controller;

import com.example.bookbridge.entity.Review;
import com.example.bookbridge.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/profile", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ProfileController {

    private final ReviewService reviewService;

    @GetMapping("/{userId}/rating")
    public ResponseEntity<?> rating(@PathVariable Long userId){
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "userId", userId,
                "avg", reviewService.avgRating(userId),
                "count", reviewService.count(userId)
        ));
    }

    @PostMapping(path = "/review", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createReview(@RequestBody Map<String,Object> body){
        Integer txId     = toInt(body.get("transactionId"));
        Long reviewerId  = toLong(body.get("reviewerId"));
        Long sellerId    = toLong(body.get("sellerId"));
        Integer rating   = toInt(body.get("rating"));
        String content   = body.getOrDefault("content","").toString();

        if (txId==null || reviewerId==null || sellerId==null || rating==null) {
            return ResponseEntity.badRequest().body(Map.of("ok",false,"message","필수값 누락"));
        }

        try{
            Review r = reviewService.create(txId, reviewerId, sellerId, rating, content);
            return ResponseEntity.ok(Map.of("ok", true, "id", r.getReviewId(), "message", "리뷰 등록 성공"));
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", e.getMessage()));
        }catch (Exception e){
            return ResponseEntity.internalServerError().body(Map.of("ok", false, "message", e.getClass().getSimpleName()));
        }
    }

    private static Long toLong(Object o){ try{ return o==null?null:Long.valueOf(o.toString()); }catch(Exception e){ return null; } }
    private static Integer toInt(Object o){ try{ return o==null?null:Integer.valueOf(o.toString()); }catch(Exception e){ return null; } }
}
