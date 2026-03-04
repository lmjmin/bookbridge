package com.example.bookbridge.repository;

import com.example.bookbridge.entity.PhoneVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Long> {
    Optional<PhoneVerification> findTopByPhoneAndUsedIsFalseOrderByIdDesc(String phone);
}
