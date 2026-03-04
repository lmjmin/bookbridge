package com.example.bookbridge.dm.controller;

import com.example.bookbridge.dm.dto.SendMessageRequest;
import com.example.bookbridge.dm.entity.DMMessage;
import com.example.bookbridge.dm.entity.DMThread;
import com.example.bookbridge.dm.service.DMService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 프런트 호환용 DM 엔드포인트
 *  - GET  /api/dm               → 내 쓰레드 목록 (meId 생략 가능)
 *  - GET  /api/dm/thread        → 내 쓰레드 목록 (동일)
 *  - GET  /api/dm/messages      → 쿼리 threadId 로 메시지 목록
 *  - POST /api/dm/send          → {threadId, text|content, meId(optional)}
 *
 * 세션에 uid가 있으면 meId 없이도 동작.
 */
@RestController
@RequestMapping("/api/dm")
public class DMCompatController {

    private final DMService service;

    public DMCompatController(DMService service) { this.service = service; }

    private Long me(HttpSession session, Long fallback){
        Object uid = session.getAttribute("uid");
        if (uid != null) return Long.valueOf(String.valueOf(uid));
        return fallback;
    }

    @GetMapping
    public ResponseEntity<?> myThreads(@RequestParam(value = "meId", required = false) Long meId,
                                       HttpSession session){
        Long id = me(session, meId);
        if (id == null) return ResponseEntity.status(401).body(Map.of("ok", false, "message", "로그인 필요"));
        List<DMThread> list = service.myThreads(id);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/thread")
    public ResponseEntity<?> myThreadsAlias(@RequestParam(value = "meId", required = false) Long meId,
                                            HttpSession session){
        return myThreads(meId, session);
    }

    @GetMapping("/messages")
    public ResponseEntity<?> messages(@RequestParam("threadId") Long threadId){
        List<DMMessage> list = service.listMessages(threadId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody Map<String,Object> body, HttpSession session){
        Long threadId = body.get("threadId")==null? null : Long.valueOf(String.valueOf(body.get("threadId")));
        if (threadId == null) return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "threadId 누락"));

        String text = String.valueOf(body.getOrDefault("text", body.getOrDefault("content","")));
        Long meId = body.get("meId")==null? null : Long.valueOf(String.valueOf(body.get("meId")));
        Long sender = me(session, meId);
        if (sender == null) return ResponseEntity.status(401).body(Map.of("ok", false, "message", "로그인 필요"));

        SendMessageRequest req = new SendMessageRequest();
        req.senderId = sender;
        req.content = text;

        return ResponseEntity.ok(service.sendMessage(threadId, req));
    }
}
