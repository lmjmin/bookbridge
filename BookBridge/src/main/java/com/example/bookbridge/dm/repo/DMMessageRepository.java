package com.example.bookbridge.dm.repo;

import com.example.bookbridge.dm.entity.DMMessage;
import com.example.bookbridge.dm.entity.DMThread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DMMessageRepository extends JpaRepository<DMMessage, Long> {
    List<DMMessage> findByThreadOrderByCreatedAtAsc(DMThread thread);
}
