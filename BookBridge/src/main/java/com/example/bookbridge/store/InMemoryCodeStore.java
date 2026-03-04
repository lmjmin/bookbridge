package com.example.bookbridge.store;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component // ★ 스프링 빈 등록
public class InMemoryCodeStore {

    private static class Item {
        final String code;
        final Instant expireAt;
        Item(String code, Instant expireAt){ this.code = code; this.expireAt = expireAt; }
    }
    private final Map<String, Item> map = new ConcurrentHashMap<>();

    public void save(String email, String code, Duration ttl){
        map.put(email.toLowerCase(), new Item(code, Instant.now().plus(ttl)));
    }
    public boolean verify(String email, String code){
        String key = email.toLowerCase();
        Item it = map.get(key);
        if(it==null) return false;
        if(Instant.now().isAfter(it.expireAt)){ map.remove(key); return false; }
        boolean ok = it.code.equals(code);
        if (ok) map.remove(key); // 1회성
        return ok;
    }
}
