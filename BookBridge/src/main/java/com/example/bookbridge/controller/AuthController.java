package com.example.bookbridge.controller;

import com.example.bookbridge.dto.LoginRequest;
import com.example.bookbridge.dto.SignupRequest;
import com.example.bookbridge.entity.User;
import com.example.bookbridge.repository.UserRepository;
import com.example.bookbridge.service.EmailCodeStore;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailCodeStore codeStore;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          EmailCodeStore codeStore) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.codeStore = codeStore;
    }

    // --- 회원가입 ---
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        if (isBlank(req.getEmail()) || isBlank(req.getPassword())
                || isBlank(req.getUsername()) || isBlank(req.getName())) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "필수 항목(아이디/이메일/비밀번호/이름)을 확인하세요."));
        }
        if (!codeStore.isVerified(req.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "이메일 인증이 필요합니다."));
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "이미 사용 중인 아이디입니다."));
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "이미 가입된 이메일입니다."));
        }

        User u = new User();
        u.setUsername(req.getUsername());
        u.setEmail(req.getEmail().toLowerCase());
        u.setName(req.getName());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setSchool(req.getSchool());
        u.setMajor(req.getMajor());
        u.setBirthdate(req.getBirthdate());
        u.setPhone(req.getPhone());
        u.setRole("USER");
        userRepository.save(u);

        return ResponseEntity.ok(Map.of("ok", true, "message", "회원가입 완료"));
    }

    // --- 로그인 (id/email/username 모두 지원) ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpSession session) {
        // username 우선, 없으면 email(=id/login/account 등으로도 매핑됨)
        final String rawUser = safe(req.getUsername());
        final String rawEmailOrId = safe(req.getEmail());
        final String loginId = !rawUser.isBlank() ? rawUser : rawEmailOrId;

        final String pw = safe(req.getPassword());
        if (loginId.isBlank() || pw.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "아이디/이메일과 비밀번호를 입력하세요."));
        }

        // 람다 안 쓰고 순차 조회
        Optional<User> found = userRepository.findByUsername(loginId);
        if (found.isEmpty()) {
            found = userRepository.findByEmail(loginId.toLowerCase());
        }
        if (found.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "존재하지 않는 계정입니다."));
        }

        User user = found.get();
        if (!passwordEncoder.matches(pw, user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "비밀번호가 올바르지 않습니다."));
        }

        // 세션 저장
        session.setAttribute("uid", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("name", user.getName());
        session.setAttribute("email", user.getEmail());

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "user", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "name", user.getName(),
                        "email", user.getEmail()
                )
        ));
    }

    // --- 내 정보 ---
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Object uid = session.getAttribute("uid");
        if (uid == null) return ResponseEntity.ok(Map.of("ok", true, "user", null));
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "user", Map.of(
                        "id", session.getAttribute("uid"),
                        "username", session.getAttribute("username"),
                        "name", session.getAttribute("name"),
                        "email", session.getAttribute("email")
                )
        ));
    }

    // --- 로그아웃 ---
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("ok", true));
    }

    private static String safe(String s){ return s == null ? "" : s.trim(); }
    private static boolean isBlank(String s){ return s == null || s.isBlank(); }
}
