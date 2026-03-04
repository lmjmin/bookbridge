package com.example.bookbridge.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UniversityService {

    private final List<String> names = new ArrayList<>();

    // 앱 시작 시 universities_kr.txt 읽어 메모리에 적재
    @PostConstruct
    public void load() {
        try (var in = new ClassPathResource("universities_kr.txt").getInputStream();
             var br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String s = line.trim();
                if (!s.isEmpty()) names.add(s);
            }
        } catch (Exception e) {
            // 비상용 기본값 (파일 누락 대비)
            names.addAll(List.of("동명대학교","부산대학교","경북대학교","서울대학교"));
        }
    }

    // 부분일치 검색 (앞부분 매칭 우선)
    public List<Map<String,String>> search(String q, int limit) {
        if (q == null || q.isBlank()) return List.of();
        String needle = q.trim().toLowerCase(Locale.ROOT);

        return names.stream()
                .sorted(Comparator.comparingInt(name -> {
                    String low = name.toLowerCase(Locale.ROOT);
                    int idx = low.indexOf(needle);
                    return idx < 0 ? 9999 : idx; // 앞에 나올수록 우선
                }))
                .filter(name -> name.toLowerCase(Locale.ROOT).contains(needle))
                .limit(Math.max(1, Math.min(20, limit)))
                .map(name -> Map.of("name", name))
                .collect(Collectors.toList());
    }
}
