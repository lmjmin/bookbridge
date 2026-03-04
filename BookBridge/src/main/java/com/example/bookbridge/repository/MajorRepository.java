package com.example.bookbridge.repository;

import com.example.bookbridge.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MajorRepository extends JpaRepository<Major, Integer> {
    Optional<Major> findByName(String name);
}
