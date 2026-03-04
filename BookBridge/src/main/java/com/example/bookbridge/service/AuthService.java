package com.example.bookbridge.service;

import com.example.bookbridge.dto.LoginRequest;
import com.example.bookbridge.dto.SignupRequest;
import com.example.bookbridge.entity.User;

public interface AuthService {
    User signup(SignupRequest req);
    User login(LoginRequest req);
}
