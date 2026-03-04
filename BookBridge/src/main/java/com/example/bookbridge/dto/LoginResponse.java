package com.example.bookbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private boolean ok;
    private String message;

    // 프론트 로컬스토리지에 넣을 간단 사용자 정보
    private Long id;
    private String username;
    private String email;
    private String name;
    private String school;
    private String major;
    private String role;
}
