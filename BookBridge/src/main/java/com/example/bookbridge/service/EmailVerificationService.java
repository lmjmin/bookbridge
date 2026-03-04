package com.example.bookbridge.service;

import com.example.bookbridge.entity.EmailVerification;
import com.example.bookbridge.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository repo;

    /** 최근 요청 가져오기 */
    public Optional<EmailVerification> latest(String email) {
        return repo.findTopByEmailOrderByIdDesc(email);
    }

    /** 코드 일치/미사용 검사(만료는 별도 체크) */
    public Optional<EmailVerification> findUnused(String email, String code) {
        return repo.findTopByEmailAndCodeAndUsedFalseOrderByIdDesc(email, code);
    }

    /** 만료 여부 */
    public static boolean isExpired(EmailVerification ev) {
        LocalDateTime exp = ev.getExpiresAt();
        return exp == null || exp.isBefore(LocalDateTime.now());
    }

    /** 코드 소모 처리 */
    @Transactional
    public void consume(EmailVerification ev) {
        ev.setUsed(true);
        repo.save(ev);
    }
}
