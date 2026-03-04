package com.example.bookbridge.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/external")
public class BookSearchController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kakao.api.key:}")
    private String kakaoApiKeyFromProps;

    // 환경변수도 허용 (예: PowerShell: setx KAKAO_API_KEY "xxxx")
    @Value("${KAKAO_API_KEY:}")
    private String kakaoApiKeyFromEnv;

    private String resolvedKey;

    @PostConstruct
    public void init() {
        resolvedKey = chooseKey();
        System.out.println("[KAKAO] key loaded? len=" + (resolvedKey == null ? 0 : resolvedKey.length()));
    }

    private String chooseKey() {
        if (StringUtils.hasText(kakaoApiKeyFromProps)) return kakaoApiKeyFromProps.trim();
        if (StringUtils.hasText(kakaoApiKeyFromEnv))   return kakaoApiKeyFromEnv.trim();
        return null;
    }

    @GetMapping("/books/search")
    public ResponseEntity<?> searchBooks(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String isbn,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String key = (resolvedKey != null ? resolvedKey : chooseKey());
        if (!StringUtils.hasText(key)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Kakao API key is missing. Set 'kakao.api.key' or env 'KAKAO_API_KEY'.\"}");
        }

        if (!StringUtils.hasText(query) && !StringUtils.hasText(isbn)) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Either 'query' or 'isbn' parameter is required.\"}");
        }

        int p = Math.max(1, Math.min(page, 50));
        int s = Math.max(1, Math.min(size, 50));

        UriComponentsBuilder ub = UriComponentsBuilder
                .fromHttpUrl("https://dapi.kakao.com/v3/search/book")
                .queryParam("page", p)
                .queryParam("size", s);

        if (StringUtils.hasText(query)) {
            // 한글 포함 검색어도 안전하게 전달되도록 나중에 UTF-8 인코딩하여 요청
            ub.queryParam("query", query);
        } else {
            // ISBN 검색은 target=isbn 을 붙여 정확도 향상
            String cleaned = isbn.replaceAll("-", "");
            ub.queryParam("query", cleaned)
              .queryParam("target", "isbn");
        }

        // ★ 핵심: UTF-8로 확실히 인코딩된 URI 생성
        URI uri = ub.build().encode(StandardCharsets.UTF_8).toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + key);

        try {
            ResponseEntity<String> resp = restTemplate.exchange(
                    uri, HttpMethod.GET, new HttpEntity<>(headers), String.class
            );
            return ResponseEntity.status(resp.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(resp.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Kakao API request failed\",\"detail\":\""
                            + e.getClass().getSimpleName() + ": " + e.getMessage() + "\"}");
        }
    }
}
