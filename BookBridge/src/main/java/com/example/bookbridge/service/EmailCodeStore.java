package com.example.bookbridge.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmailCodeStore {

    public record Entry(String code, long exp, boolean used) {}

    private final ConcurrentHashMap<String, Entry> store = new ConcurrentHashMap<>();

    public void save(String email, String code, long ttlMillis) {
        long exp = Instant.now().toEpochMilli() + ttlMillis;
        store.put(email.toLowerCase(), new Entry(code, exp, false));
    }

    public boolean verify(String email, String code) {
        email = email.toLowerCase();
        Entry e = store.get(email);
        if (e == null) return false;
        if (e.used()) return false;
        if (Instant.now().toEpochMilli() > e.exp()) return false;
        if (!e.code().equals(code)) return false;
        store.put(email, new Entry(e.code(), e.exp(), true)); // 사용 처리
        return true;
    }

    public boolean isVerified(String email) {
        Entry e = store.get(email.toLowerCase());
        return e != null && e.used() && Instant.now().toEpochMilli() <= e.exp();
    }
}
