package com.example.bookbridge.dto;

import java.time.LocalDateTime;

import com.example.bookbridge.entity.BookListing;

public class BookCardDto {
    private Long id;
    private String title;
    private String author;
    private String publisher;
    private String isbn;
    private Integer price;
    private String imageUrl;
    private String conditionText;
    private String university;
    private LocalDateTime createdAt;

    public BookCardDto() {}

    public static BookCardDto from(BookListing b) {
        BookCardDto d = new BookCardDto();
        d.id = b.getId();
        d.title = b.getTitle();
        d.author = b.getAuthor();
        d.publisher = b.getPublisher();
        d.isbn = b.getIsbn();
        d.price = b.getPrice();
        d.imageUrl = b.getImageUrl();
        d.conditionText = b.getConditionText();
        d.university = b.getUniversity();
        d.createdAt = b.getCreatedAt();
        return d;
    }

    // === getters ===
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getPublisher() { return publisher; }
    public String getIsbn() { return isbn; }
    public Integer getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getConditionText() { return conditionText; }
    public String getUniversity() { return university; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
