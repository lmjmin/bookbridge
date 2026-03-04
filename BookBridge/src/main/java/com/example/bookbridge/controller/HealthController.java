package com.example.bookbridge.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/actuator")
public class HealthController {
    @GetMapping("/health")
    public Map<String,Object> health(){ return Map.of("status","UP"); }
}
