package com.example.bookbridge.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateListingRequest {
    @NotBlank private String title;
    @NotBlank private String author;
    @NotBlank private String publisher;
    private String isbn;

    @Min(0) private Integer price;

    private String university;     // ← 엔티티에 맞춤
    private String sellerPhone;    // ← 엔티티에 맞춤
    private String conditionText;  // ← 엔티티에 맞춤

    private String imageUrl;       // (옵션) 파일 업로드 붙이면 사용
}
