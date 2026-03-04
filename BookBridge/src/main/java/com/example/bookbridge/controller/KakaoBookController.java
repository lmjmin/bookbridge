package com.example.bookbridge.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kakao")
public class KakaoBookController {

    // 키가 없어도 서버가 죽지 않도록 기본값("")
    @Value("${app.kakao.rest-key:}")
    private String kakaoRestKey;

    private final RestTemplate rt = new RestTemplate();

    /**
     * 예: GET /api/kakao/books?query=자바&page=1&size=10
     */
    @GetMapping("/books")
    public ResponseEntity<?> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // 키 없으면 프론트 폴백 가능하게 빈 결과 반환
        if (kakaoRestKey == null || kakaoRestKey.isBlank()) {
            return ResponseEntity.ok(Map.of(
                    "documents", List.of(),
                    "meta", Map.of("is_end", true, "pageable_count", 0)
            ));
        }

        URI uri = UriComponentsBuilder.fromUriString("https://dapi.kakao.com/v3/search/book")
                .queryParam("query", query)
                .queryParam("page", page)
                .queryParam("size", size)
                .build(true)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoRestKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> res = rt.exchange(uri, HttpMethod.GET, entity, String.class);
            return ResponseEntity.status(res.getStatusCode()).body(res.getBody());
        } catch (Exception e) {
            // 외부 오류시에도 빈 결과로 응답(서버 크래시 방지)
            return ResponseEntity.ok(Map.of(
                    "documents", List.of(),
                    "meta", Map.of("is_end", true, "pageable_count", 0),
                    "error", e.getClass().getSimpleName()
            ));
        }
    }
}
