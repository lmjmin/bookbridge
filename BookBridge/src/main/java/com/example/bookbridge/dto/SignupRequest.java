package com.example.bookbridge.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SignupRequest {
    private String email;
    private String password;
    private String username;
    private String name;
    private String school;
    private String major;
    private String phone;       // 옵션
    private String birthdate;   // 옵션
    private String code;        // 옵션(이메일 인증코드 사용 시)
}
