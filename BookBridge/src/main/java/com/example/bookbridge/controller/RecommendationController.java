package com.example.bookbridge.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class RecommendationController {

    private static final Map<String, List<String>> SEED = new HashMap<>();

    static {
        SEED.put("판타지", Arrays.asList("마법", "서사", "모험", "청소년 소설"));
        SEED.put("로맨스", Arrays.asList("연애", "로맨스 소설", "감성", "치유"));
        SEED.put("스릴러", Arrays.asList("미스터리", "추리", "범죄", "서스펜스"));
        SEED.put("SF", Arrays.asList("공상과학", "우주", "디스토피아", "시간여행"));
        SEED.put("자기계발", Arrays.asList("동기부여", "시간관리", "습관", "생산성"));
        SEED.put("경제", Arrays.asList("투자", "주식", "부동산", "거시경제"));
        SEED.put("IT", Arrays.asList("프로그래밍", "알고리즘", "컴퓨터공학", "데이터"));
        SEED.put("자바", Arrays.asList("Spring", "JPA", "백엔드", "객체지향"));
        SEED.put("파이썬", Arrays.asList("데이터분석", "머신러닝", "딥러닝", "자동화"));
        SEED.put("해리포터", Arrays.asList("J.K. 롤링", "호그와트", "판타지", "청소년 소설"));
    }

    @GetMapping(value = "/recommend", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Map<String, Object>> recommend(@RequestParam("q") String query) {
        String norm = query == null ? "" : query.trim();
        if (norm.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "query", "",
                    "relatedKeywords", List.of(),
                    "strategy", "empty-query"
            ));
        }

        Set<String> related = new LinkedHashSet<>();
        for (Map.Entry<String, List<String>> e : SEED.entrySet()) {
            String key = e.getKey();
            if (norm.contains(key) || key.contains(norm)) {
                related.addAll(e.getValue());
                related.add(key + " 추천");
            }
        }
        for (String t : List.of("학과", "전공", "초보", "입문", "실전", "핵심", "베스트", "심화")) {
            if (norm.length() >= 2) {
                related.add(norm + " " + t);
            }
        }

        List<String> top = new ArrayList<>(related);
        if (top.size() > 8) top = top.subList(0, 8);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("query", norm);
        payload.put("relatedKeywords", top);
        payload.put("strategy", top.isEmpty() ? "fallback-tokens" : "seed-keywords");
        return ResponseEntity.ok(payload);
    }
}
