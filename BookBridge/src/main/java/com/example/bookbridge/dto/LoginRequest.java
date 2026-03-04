package com.example.bookbridge.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginRequest {
    // id/login/account/email 어떤 키로 와도 email 필드로 매핑
    @JsonAlias({"id","login","account","email"})
    private String email;

    // 일부 코드가 username 키를 직접 보낼 수 있어서 대비
    @JsonAlias({"username","user"})
    private String username;

    private String password;
}
