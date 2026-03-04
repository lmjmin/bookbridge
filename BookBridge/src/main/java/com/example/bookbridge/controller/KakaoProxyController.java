package com.example.bookbridge.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/kakao/proxy")
public class KakaoProxyController {

    @Value("${app.kakao.rest-key:}")
    private String kakaoKey;

    private final RestTemplate rt = new RestTemplate();

    @GetMapping("/books-proxy")
    public ResponseEntity<?> books(
            @RequestParam("query") String query,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        if (kakaoKey == null || kakaoKey.isBlank()) {
            return ResponseEntity.ok(Map.of("documents", java.util.List.of())); // 키 없으면 빈 결과
        }
        URI uri = UriComponentsBuilder
                .fromUriString("https://dapi.kakao.com/v3/search/book")
                .queryParam("query", query)
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("target", "title")
                .build(true).toUri();

        var headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoKey);
        var req = new org.springframework.http.HttpEntity<>(headers);
        var resp = rt.exchange(uri, org.springframework.http.HttpMethod.GET, req, String.class);
        return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());
    }
}
