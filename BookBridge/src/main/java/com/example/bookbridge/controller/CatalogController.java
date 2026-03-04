package com.example.bookbridge.controller;

import com.example.bookbridge.entity.BookListing;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/books")
public class CatalogController {

    private final EntityManager em;
    public CatalogController(EntityManager em) { this.em = em; }

    /** 최신 목록 */
    @GetMapping
    public ResponseEntity<?> latest(@RequestParam(value="size", defaultValue="30") int size){
        try{
            int limit = Math.max(1, Math.min(size, 60));
            TypedQuery<BookListing> q = em.createQuery(
                    "SELECT b FROM BookListing b ORDER BY b.id DESC", BookListing.class);
            q.setMaxResults(limit);
            return ResponseEntity.ok(toSimpleList(q.getResultList()));
        }catch(Exception e){ e.printStackTrace(); return ResponseEntity.ok(List.of()); }
    }

    /** 단건 조회 (detail.html 지원) */
    @GetMapping("/{id}")
    public ResponseEntity<?> one(@PathVariable("id") Long id){
        try{
            BookListing b = em.find(BookListing.class, id);
            if (b == null) return ResponseEntity.ok(Map.of());
            return ResponseEntity.ok(toSimpleItem(b));
        }catch(Exception e){ e.printStackTrace(); return ResponseEntity.ok(Map.of()); }
    }

    /** ISBN 정확/부분 조회 (상세검색 스캔 1순위) */
    @GetMapping("/by-isbn")
    public ResponseEntity<?> byIsbn(@RequestParam("isbn") String raw){
        try{
            String isbn = raw == null ? "" : raw.replaceAll("[^0-9Xx]", "");
            if (isbn.isEmpty()) return ResponseEntity.ok(List.of());
            TypedQuery<BookListing> q = em.createQuery(
                    "SELECT b FROM BookListing b " +
                    "WHERE REPLACE(UPPER(b.isbn),' ','') LIKE CONCAT('%', :isbn, '%') " +
                    "ORDER BY b.id DESC",
                    BookListing.class);
            q.setParameter("isbn", isbn.toUpperCase());
            return ResponseEntity.ok(toSimpleList(q.getResultList()));
        }catch(Exception e){ e.printStackTrace(); return ResponseEntity.ok(List.of()); }
    }

    /** 키워드 검색 (title/author/publisher/isbn LIKE) */
    @PostMapping("/search")
    public ResponseEntity<?> search(@RequestBody Map<String,Object> body){
        String qStr = Optional.ofNullable(body.get("q")).map(Object::toString).orElse("").trim();
        int page = parseInt(body.get("page"), 0);
        int size = Math.max(1, Math.min(parseInt(body.get("size"), 30), 60));
        try{
            String jpql =
                "SELECT b FROM BookListing b WHERE " +
                "(:q='' OR LOWER(b.title) LIKE LOWER(CONCAT('%',:q,'%')) " +
                " OR LOWER(b.author) LIKE LOWER(CONCAT('%',:q,'%')) " +
                " OR LOWER(b.publisher) LIKE LOWER(CONCAT('%',:q,'%')) " +
                " OR LOWER(b.isbn) LIKE LOWER(CONCAT('%',:q,'%'))) " +
                "ORDER BY b.id DESC";
            TypedQuery<BookListing> q = em.createQuery(jpql, BookListing.class);
            q.setParameter("q", qStr);
            q.setFirstResult(Math.max(0,page)*size);
            q.setMaxResults(size);
            return ResponseEntity.ok(toSimpleList(q.getResultList()));
        }catch(Exception e){ e.printStackTrace(); return ResponseEntity.ok(List.of()); }
    }

    private int parseInt(Object o, int def){ try{ return Integer.parseInt(String.valueOf(o)); }catch(Exception e){ return def; } }

    private Map<String,Object> toSimpleItem(BookListing b){
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("id", b.getId());
        m.put("title", nz(b.getTitle()));
        m.put("author", nz(b.getAuthor()));
        m.put("publisher", nz(b.getPublisher()));
        m.put("isbn", nz(b.getIsbn()));
        String img = nz(b.getImageUrl());
        m.put("thumbnail", img); m.put("imageUrl", img); m.put("coverImage", img);
        m.put("price", b.getPrice());
        m.put("conditionText", nz(b.getConditionText()));
        m.put("sellerId", b.getSellerId());
        m.put("sellerPhone", nz(b.getSellerPhone()));
        m.put("university", nz(b.getUniversity()));
        return m;
    }

    private List<Map<String,Object>> toSimpleList(List<BookListing> src){
        List<Map<String,Object>> out = new ArrayList<>();
        if (src == null) return out;
        for (BookListing b : src) out.add(toSimpleItem(b));
        return out;
    }
    private String nz(String s){ return s==null? "":s; }
}
