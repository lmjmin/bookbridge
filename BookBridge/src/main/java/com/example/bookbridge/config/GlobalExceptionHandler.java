package com.example.bookbridge.config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handle415(Exception e){
        return ResponseEntity.status(415).body(Map.of("ok", false, "message", "unsupported media type"));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> handle404(NoHandlerFoundException e) {
        return ResponseEntity.status(404)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("ok", false, "message", "not found"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handle403(Exception e) {
        return ResponseEntity.status(403)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("ok", false, "message", "forbidden"));
    }

    @ExceptionHandler(Exception.class)
    public Object handleAny(Exception e, HttpServletRequest req){
        // API 요청만 로그 (정적/페이지 루프 방지)
        if (req.getRequestURI() != null && req.getRequestURI().startsWith("/api/")) {
            log.error("Unhandled @ {} {} ", req.getMethod(), req.getRequestURI(), e);
        }
        String accept = req.getHeader("Accept");
        String xrw    = req.getHeader("X-Requested-With");
        boolean wantsJson = (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE))
                || "XMLHttpRequest".equalsIgnoreCase(xrw)
                || req.getRequestURI().startsWith("/api/");

        if (wantsJson) {
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("ok", false, "message", "server error"));
        }
        // ✅ forward 금지, redirect로 종료
        return "redirect:/s/first.html";
    }
}
