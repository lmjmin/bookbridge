package com.example.bookbridge.controller;

import com.example.bookbridge.service.EmailCodeStore;
import com.example.bookbridge.service.EmailService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailCodeController {

    private final EmailService emailService;
    private final EmailCodeStore store;

    public EmailCodeController(EmailService emailService, EmailCodeStore store) {
        this.emailService = emailService;
        this.store = store;
    }

    @PostMapping({"/send", "/request-code"})
    public Map<String, Object> request(@RequestBody Map<String, String> body) {
        String email = (body.getOrDefault("email","")+"").trim().toLowerCase();
        if (email.isBlank()) return Map.of("ok", false, "message", "이메일이 비었습니다.");

        String code = String.format("%06d", (int)(Math.random()*1_000_000));
        boolean sent = emailService.sendCode(email, code);
        if (!sent) return Map.of("ok", false, "message", "이메일 전송 실패");

        store.save(email, code, 5 * 60 * 1000); // 5분
        return Map.of("ok", true, "message", "코드를 전송했습니다.");
    }

    @PostMapping({"/verify", "/verify-code"})
    public Map<String, Object> verify(@RequestBody Map<String, String> body) {
        String email = (body.getOrDefault("email","")+"").trim().toLowerCase();
        String code  = (body.getOrDefault("code","")+"").trim();

        if (store.verify(email, code)) {
            return Map.of("ok", true, "message", "인증 완료");
        }
        return Map.of("ok", false, "message", "코드가 유효하지 않거나 만료되었습니다.");
    }
}
