package com.example.bookbridge.dm.repo;

import com.example.bookbridge.dm.entity.DMThread;
import com.example.bookbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DMThreadRepository extends JpaRepository<DMThread, Long> {
    List<DMThread> findByUserAOrUserB(User a, User b);
    Optional<DMThread> findByUserAAndUserB(User a, User b);
    Optional<DMThread> findByUserBAndUserA(User b, User a);
}
