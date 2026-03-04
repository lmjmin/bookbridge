package com.example.bookbridge.service;

import com.example.bookbridge.dto.LoginRequest;
import com.example.bookbridge.dto.SignupRequest;
import com.example.bookbridge.entity.EmailVerification;
import com.example.bookbridge.entity.User;
import com.example.bookbridge.repository.EmailVerificationRepository;
import com.example.bookbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;
    private final EmailVerificationRepository emailRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public User signup(SignupRequest req) {
        String username = safe(req.getUsername());
        String email    = safe(req.getEmail());
        String password = safe(req.getPassword());

        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("필수 값이 비었습니다.");
        }
        if (userRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");
        }
        if (userRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 이메일 인증 확인(최근 코드가 사용됨 + 만료 전)
        EmailVerification ev = emailRepo.findTopByEmailOrderByIdDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일 인증을 먼저 완료하세요."));
        if (ev.isUsed() == false || ev.getExpiresAt() == null || ev.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
        }

        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(encoder.encode(password));
        u.setRole("USER");
        return userRepo.save(u);
    }

    @Override
    public User login(LoginRequest req) {
        String username = safe(req.getUsername());
        String email    = safe(req.getEmail());
        String password = safe(req.getPassword());

        User u = !username.isBlank()
                ? userRepo.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자명입니다."))
                : userRepo.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!encoder.matches(password, u.getPassword()))
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");

        return u;
    }

    private static String safe(String s){ return s == null ? "" : s.trim(); }
}
