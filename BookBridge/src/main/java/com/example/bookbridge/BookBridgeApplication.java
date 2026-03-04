package com.example.bookbridge;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookBridgeApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BookBridgeApplication.class);
        app.setBannerMode(Banner.Mode.OFF); // 배너 출력 제거(아주 약간이나마 가벼움)
        app.run(args);
    }
}
