package com.example.bookbridge.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 루트 진입은 정적파일(/s/first.html)로 리다이렉트 */
@Controller
public class HomeController {

    @GetMapping({"/", "/index", "/index.html"})
    public String root() {
        return "redirect:/s/first.html";
    }
}
