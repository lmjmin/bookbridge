package com.example.bookbridge.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class EmailVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)     // H2 dev에서는 null 허용, 실서비스에선 unique+not null 권장
    private String email;

    @Column
    private String code;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used;
}
