package com.example.bookbridge.controller;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.bookbridge.entity.BookListing;
import com.example.bookbridge.repository.BookListingRepository;
import com.example.bookbridge.entity.User;
import com.example.bookbridge.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;

/**
 * 판매글 등록/조회 API
 * ⚠️ 경로 충돌 방지를 위해 베이스 경로를 /api/listings 로 변경했습니다.
 *    (CatalogController 가 /api/books 를 단독 담당)
 */
@RestController
@RequestMapping("/api/listings")
public class BookListingApiController {

    private final BookListingRepository repo;
    private final UserRepository users; // 🔹 추가
    private final Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();

    @PersistenceContext
    private EntityManager em;

    public BookListingApiController(BookListingRepository repo,
                                    UserRepository users) {
        this.repo = repo;
        this.users = users;
    }


    @PostConstruct
    public void init() throws IOException { Files.createDirectories(uploadDir); }

    /* ================= 조회 ================= */

    /** 최근 목록(대학 필터 선택)  GET /api/listings  또는 /api/listings/recent */
    @GetMapping({ "", "/recent" })
    public List<BookListing> recent(@RequestParam(value = "univ", required = false) String univ) {
        String u = (univ != null && !univ.isBlank()) ? univ.trim() : null;
        return repo.findRecentByUniversity(u, PageRequest.of(0, 12)).getContent();
    }

    /** 키워드 검색 GET /api/listings/search?q=...&univ=... */
    @GetMapping("/search")
    public List<BookListing> search(@RequestParam("q") String q,
                                    @RequestParam(value = "univ", required = false) String univ) {
        String keyword = (q == null) ? "" : q.trim();
        String u = (univ != null && !univ.isBlank()) ? univ.trim() : null;
        if (keyword.isBlank()) {
            return repo.findRecentByUniversity(u, PageRequest.of(0, 12)).getContent();
        }
        return repo.searchByUniversityAndKeyword(u, keyword);
    }

    /** 대학 기준 추천(간단 버전) GET /api/listings/reco/{uid}?univ=... */
    @GetMapping("/reco/{uid}")
    public List<BookListing> recommendByUniversity(@PathVariable String uid,
                                                   @RequestParam(name = "univ", required = false) String university) {
        if (university != null && !university.isBlank()) {
            return repo.findTop12ByUniversityOrderByIdDesc(university);
        }
        return repo.findTop12ByOrderByIdDesc();
    }

    /** 단건 조회 GET /api/listings/{id}
 *  detail.html 에서 쓰는 용도라서 seller 정보까지 같이 내려준다.
 */
@GetMapping("/{id}")
public ResponseEntity<?> getOne(@PathVariable Long id) {

    return repo.findById(id)
            .<ResponseEntity<?>>map(b -> {

                // ----- seller 정보 조립 -----
                Map<String, Object> seller = new LinkedHashMap<>();

                // 1순위: sellerId로 유저 찾기
                Optional<User> u = Optional.ofNullable(b.getSellerId())
                        .flatMap(users::findById);

                // 2순위: sellerPhone으로 유저 찾기 (기본값 010-0000-0000 은 무시)
                if (u.isEmpty() && StringUtils.hasText(b.getSellerPhone())) {
                    String phone = b.getSellerPhone().trim();
                    if (!"010-0000-0000".equals(phone)) {
                        try {
                            u = users.findByPhone(phone);
                        } catch (Exception ignored) {}
                    }
                }

                if (u.isPresent()) {
                    User s = u.get();
                    String name = StringUtils.hasText(s.getName())
                            ? s.getName()
                            : s.getUsername();

                    seller.put("id", s.getId());
                    seller.put("name", name);
                    seller.put("email", s.getEmail());
                    seller.put("school", s.getSchool());
                    seller.put("major", s.getMajor());
                    seller.put("phone", s.getPhone());
                }

                // ----- 본문 조립 -----
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("id", b.getId());
                body.put("title", b.getTitle());
                body.put("author", b.getAuthor());
                body.put("isbn", b.getIsbn());
                body.put("publisher", b.getPublisher());
                body.put("price", b.getPrice());
                body.put("imageUrl", b.getImageUrl());
                body.put("conditionText", b.getConditionText());
                body.put("sellerId", b.getSellerId());
                body.put("sellerPhone", b.getSellerPhone());
                body.put("university", b.getUniversity());
                body.put("createdAt", b.getCreatedAt());
                body.put("seller", seller); // 🔥 여기! detail.html 에서 쓰는 필드

                return ResponseEntity.ok(body);
            })
            .orElseGet(() -> ResponseEntity.status(404)
                    .body(Map.of("ok", false, "message", "not found")));
}


    /** 내 판매글 조회(옵션) GET /api/listings/mine?email=... */
    @GetMapping("/mine")
    public List<BookListing> mine(@RequestParam(value = "email", required = false) String email) {
        if (email == null || email.isBlank()) {
            return repo.findRecentByUniversity(null, PageRequest.of(0, 30)).getContent();
        }
        boolean hasSellerEmail = false;
        try { BookListing.class.getDeclaredField("sellerEmail"); hasSellerEmail = true; }
        catch (NoSuchFieldException ignored) {}
        if (hasSellerEmail) {
            String jpql = "SELECT b FROM BookListing b WHERE b.sellerEmail = :email ORDER BY b.createdAt DESC";
            TypedQuery<BookListing> q = em.createQuery(jpql, BookListing.class);
            q.setParameter("email", email.trim());
            q.setMaxResults(50);
            return q.getResultList();
        } else {
            return repo.findRecentByUniversity(null, PageRequest.of(0, 30)).getContent();
        }
    }

    /* ================= 등록 ================= */

    /**
     * 등록(multipart) POST /api/listings
     * Content-Type: multipart/form-data
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createMultipart(
            @RequestParam(required = false) String isbn,
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam String publisher,
            @RequestParam(name = "price") String priceRaw,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String conditionText,
            @RequestParam(required = false) String sellerPhone,
            @RequestParam(required = false) String sellerEmail,
            @RequestParam(required = false) String university,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return saveListing(
                isbn, title, author, publisher, priceRaw,
                firstNonNull(conditionText, description),
                sellerPhone, sellerEmail, university, image
        );
    }

    /**
     * 등록(JSON) POST /api/listings
     * Content-Type: application/json
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createJson(@RequestBody Map<String, Object> body) {
        String isbn        = str(body.get("isbn"));
        String title       = str(body.get("title"));
        String author      = str(body.get("author"));
        String publisher   = str(body.get("publisher"));
        String priceRaw    = str(body.get("price"));
        String description = str(body.containsKey("conditionText") ? body.get("conditionText") : body.get("description"));
        String sellerPhone = str(body.get("sellerPhone"));
        String sellerEmail = str(body.get("sellerEmail"));
        String university  = str(body.get("university"));

        return saveListing(isbn, title, author, publisher, priceRaw,
                description, sellerPhone, sellerEmail, university, null);
    }

    /* ============== 내부 공통 저장 로직 ============== */

    private ResponseEntity<?> saveListing(
            String isbn, String title, String author, String publisher, String priceRaw,
            String description, String sellerPhone, String sellerEmail, String university,
            MultipartFile image
    ) {
        int price;
        try { price = parsePrice(priceRaw); }
        catch (Exception ex) { return ResponseEntity.badRequest().body(Map.of("error","가격 형식이 올바르지 않습니다.")); }

        if (isBlank(title) || isBlank(author) || isBlank(publisher) || price <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error","title/author/publisher/price 는 필수입니다."));
        }

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                String original = image.getOriginalFilename();
                if (nonBlank(original)) {
                    String clean = StringUtils.cleanPath(original);
                    String ext = getExt(clean);
                    String name = java.util.UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
                    Path target = uploadDir.resolve(name);
                    Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                    imageUrl = "/uploads/" + name;
                }
            } catch (IOException e) {
                return ResponseEntity.internalServerError().body(Map.of("error","이미지 업로드 실패","detail",e.getMessage()));
            }
        }

        BookListing e = new BookListing();
        e.setTitle(title);
        e.setAuthor(author);
        e.setPublisher(publisher);
        e.setIsbn(normalizeIsbn(isbn));
        e.setPrice(price);
        e.setConditionText(description);
        e.setSellerPhone(isBlank(sellerPhone) ? "010-0000-0000" : sellerPhone.trim());
        e.setCreatedAt(LocalDateTime.now());
        e.setImageUrl(imageUrl);
        e.setUniversity(isBlank(university) ? null : university.trim());

        // 엔티티에 sellerEmail 필드가 있으면 반영(없으면 무시)
        try { BookListing.class.getMethod("setSellerEmail", String.class).invoke(e, sellerEmail); }
        catch (NoSuchMethodException ignore) {}
        catch (Exception ignore) {}

        BookListing saved = repo.save(e);

        Map<String,Object> resp = new HashMap<>();
        resp.put("id", saved.getId());
        if (nonBlank(saved.getImageUrl())) resp.put("imageUrl", saved.getImageUrl());
        return ResponseEntity.ok(resp);
    }

    /* ============== 유틸 ============== */
    private static String str(Object v){ return v==null? null : String.valueOf(v).trim(); }
    private static boolean isBlank(String s){ return s==null || s.trim().isEmpty(); }
    private static boolean nonBlank(String s){ return s!=null && !s.isBlank(); }
    private static int parsePrice(String raw){
        if (raw == null) return 0;
        String onlyNum = raw.replaceAll("[^0-9]","");
        return onlyNum.isEmpty() ? 0 : Integer.parseInt(onlyNum);
    }
    private static String getExt(String name){
        int i = name.lastIndexOf('.');
        return (i > -1 && i < name.length()-1) ? name.substring(i+1) : "";
    }
    private static String normalizeIsbn(String v){
        if (v == null) return null;
        String n = v.replaceAll("[^0-9Xx]","");
        return n.isEmpty() ? null : n;
    }
    private static <T> T firstNonNull(T a, T b){ return a!=null ? a : b; }
}
