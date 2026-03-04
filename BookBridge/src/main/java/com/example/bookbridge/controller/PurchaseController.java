package com.example.bookbridge.controller;

import com.example.bookbridge.entity.Purchase;
import com.example.bookbridge.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/purchase", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    private Long toLong(Object v){
        if (v == null) return null;
        try { return Long.parseLong(v.toString()); } catch (Exception e){ return null; }
    }
    private Integer toInt(Object v){
        if (v == null) return null;
        try { return Integer.parseInt(v.toString()); } catch (Exception e){ return null; }
    }

    /** 내 구매목록 */
    @GetMapping("/my")
    public ResponseEntity<?> my(@RequestParam("userId") Long userId){
        var list = purchaseService.myPurchases(userId).stream().map((Purchase p) -> Map.of(
            "id", p.getId(),
            "listingId", p.getListingId(),
            "price", p.getPrice(),
            "status", p.getStatus(),
            "createdAt", p.getCreatedAt().toString()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("ok", true, "items", list));
    }

    /** 구매(결제 완료로 저장) */
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body){
        Long userId = toLong(body.get("userId"));
        Long listingId = toLong(body.get("listingId")); // PurchaseListing의 PK(id)
        Integer price = toInt(body.get("price"));
        if (userId == null || listingId == null){
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "userId/listingId 누락"));
        }
        try{
            Purchase p = purchaseService.createPaidOrder(userId, listingId, price);
            return ResponseEntity.ok(Map.of("ok", true, "id", p.getId(), "status", p.getStatus()));
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", e.getMessage()));
        }catch(Exception e){
            return ResponseEntity.internalServerError().body(Map.of("ok", false, "message", "서버 오류: " + e.getClass().getSimpleName()));
        }
    }
}
