package com.example.bookbridge.repository;

import com.example.bookbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // 🔽 추가
    Optional<User> findByPhone(String phone);  // 🔥 이거
}
