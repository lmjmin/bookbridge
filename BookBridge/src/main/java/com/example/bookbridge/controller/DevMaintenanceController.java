package com.example.bookbridge.controller;

import com.example.bookbridge.repository.BookListingRepository;
import com.example.bookbridge.service.EmailService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController

@RequestMapping("/dev")

public class DevMaintenanceController {
    

    private final EmailService emailService;
    private final BookListingRepository books;

    @PersistenceContext private EntityManager em;

    public DevMaintenanceController(EmailService emailService, BookListingRepository books){
        this.emailService = emailService; this.books = books;
    }

    @PostMapping("/mail/test")
    public ResponseEntity<?> sendTest(@RequestParam String to){
        boolean ok = emailService.sendCode(to, "123456");
        return ResponseEntity.ok(Map.of("ok", ok));
    }

    @PostMapping("/purge")
    public ResponseEntity<?> purgeAll(){
        // 매우 주의! 테스트 환경에서만
        em.createNativeQuery("DELETE FROM dm_message").executeUpdate();
        em.createNativeQuery("DELETE FROM dm_thread").executeUpdate();
        em.createNativeQuery("DELETE FROM book_listing").executeUpdate();
        em.createNativeQuery("DELETE FROM email_verification").executeUpdate();
        return ResponseEntity.ok(Map.of("ok", true));
    }
    
}
