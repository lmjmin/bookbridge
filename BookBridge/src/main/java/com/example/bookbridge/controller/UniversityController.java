package com.example.bookbridge.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/universities", produces = MediaType.APPLICATION_JSON_VALUE)
public class UniversityController {

    private List<String> all;

    private synchronized void ensureLoaded() {
        if (all != null) return;
        try {
            var res = new ClassPathResource("universities_kr.txt");
            try (var br = new BufferedReader(new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8))) {
                all = Collections.unmodifiableList(
                    br.lines().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList())
                );
            }
        } catch (Exception e) {
            all = List.of();
        }
    }

    @GetMapping
    public ResponseEntity<?> search(@RequestParam(defaultValue = "") String q) {
        ensureLoaded();
        String qq = q == null ? "" : q.trim().toLowerCase(Locale.KOREAN);
        if (qq.isEmpty()) return ResponseEntity.ok(List.of());

        var list = all.stream()
                .filter(u -> u.toLowerCase(Locale.KOREAN).contains(qq))
                .limit(20)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
