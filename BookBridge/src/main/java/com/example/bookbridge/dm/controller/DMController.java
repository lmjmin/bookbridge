package com.example.bookbridge.dm.controller;

import com.example.bookbridge.dm.dto.CreateThreadRequest;
import com.example.bookbridge.dm.dto.SendMessageRequest;
import com.example.bookbridge.dm.entity.DMMessage;
import com.example.bookbridge.dm.entity.DMThread;
import com.example.bookbridge.dm.service.DMService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dm")
public class DMController {
    private final DMService service;
    public DMController(DMService service) { this.service = service; }

    @PostMapping("/threads")
    public Map<String, Object> createOrGetThread(@RequestBody CreateThreadRequest req) {
        DMThread th = service.createOrGetThread(req);
        return Map.of("threadId", th.getId());
    }

    @GetMapping("/threads")
    public List<DMThread> myThreads(@RequestParam Long meId) {
        return service.myThreads(meId);
    }

    @GetMapping("/threads/{id}/messages")
    public List<DMMessage> list(@PathVariable Long id) {
        return service.listMessages(id);
    }

    @PostMapping("/threads/{id}/messages")
    public DMMessage send(@PathVariable Long id, @RequestBody SendMessageRequest req) {
        return service.sendMessage(id, req);
    }
}
