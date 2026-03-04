package com.example.bookbridge.repository;

import com.example.bookbridge.entity.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    List<UserActivity> findTop50ByUserIdOrderByViewedAtDesc(String userId);
}
