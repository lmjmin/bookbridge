package com.example.bookbridge.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmailCodeDtos {

    public record SendRequest(@NotBlank @Email String email) {}

    public record VerifyRequest(@NotBlank @Email String email,
                                @NotBlank String code) {}

    public record ApiResponse(boolean ok, String message) {}
}
