package com.example.bookbridge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "purchase")
public class Purchase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long listingId;
    private Integer price;
    private String status;
    private LocalDateTime createdAt = LocalDateTime.now();
}
