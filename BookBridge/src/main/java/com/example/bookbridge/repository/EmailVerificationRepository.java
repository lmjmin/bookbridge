package com.example.bookbridge.repository;

import com.example.bookbridge.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findTopByEmailOrderByIdDesc(String email);

    // 컨트롤러/서비스에서 사용하는 정확 일치 조회(미사용/유효기간 내)
    Optional<EmailVerification> findTopByEmailAndCodeAndUsedFalseOrderByIdDesc(String email, String code);

    // 만료 체크까지 한 번에 처리하고 싶을 때 사용 가능한 네이티브(선택)
    @Query(value = """
        SELECT * FROM email_verification
        WHERE email = :email
          AND code  = :code
          AND used  = FALSE
          AND (expires_at IS NULL OR expires_at > :now)
        ORDER BY id DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<EmailVerification> findValid(@Param("email") String email,
                                          @Param("code") String code,
                                          @Param("now")  LocalDateTime now);
}
