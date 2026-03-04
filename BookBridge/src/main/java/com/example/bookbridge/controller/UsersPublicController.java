package com.example.bookbridge.controller;

import com.example.bookbridge.entity.User;
import com.example.bookbridge.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UsersPublicController {
    private final UserRepository users;
    public UsersPublicController(UserRepository users){ this.users = users; }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id){
        return users.findById(id)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(publicInfo(u)))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("ok",false,"message","not found")));
    }

    @GetMapping("/lookup")
    public ResponseEntity<?> getByEmail(@RequestParam String email){
        return users.findByEmail(email)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(publicInfo(u)))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("ok",false,"message","not found")));
    }

    private Map<String,Object> publicInfo(User u){
        return Map.of(
                "id", u.getId(),
                "email", u.getEmail(),
                "name", u.getName(),
                "school", u.getSchool(),
                "major", u.getMajor(),
                "phone", u.getPhone()
        );
    }
}
