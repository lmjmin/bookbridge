package com.example.bookbridge.dm.service;

import com.example.bookbridge.dm.dto.CreateThreadRequest;
import com.example.bookbridge.dm.dto.SendMessageRequest;
import com.example.bookbridge.dm.entity.DMMessage;
import com.example.bookbridge.dm.entity.DMThread;
import com.example.bookbridge.dm.repo.DMMessageRepository;
import com.example.bookbridge.dm.repo.DMThreadRepository;
import com.example.bookbridge.entity.User;
import com.example.bookbridge.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @Transactional
public class DMService {
    private final DMThreadRepository threadRepo;
    private final DMMessageRepository messageRepo;
    private final UserRepository userRepo;

    public DMService(DMThreadRepository t, DMMessageRepository m, UserRepository u) {
        this.threadRepo = t; this.messageRepo = m; this.userRepo = u;
    }

    public DMThread createOrGetThread(CreateThreadRequest req) {
        User a = userRepo.findById(req.userAId).orElseThrow();
        User b = userRepo.findById(req.userBId).orElseThrow();

        return threadRepo.findByUserAAndUserB(a, b)
                .or(() -> threadRepo.findByUserBAndUserA(a, b))
                .orElseGet(() -> {
                    DMThread th = new DMThread();
                    th.setUserA(a); th.setUserB(b);
                    return threadRepo.save(th);
                });
    }

    public List<DMThread> myThreads(Long meId) {
        User me = userRepo.findById(meId).orElseThrow();
        return threadRepo.findByUserAOrUserB(me, me);
    }

    public List<DMMessage> listMessages(Long threadId) {
        DMThread th = threadRepo.findById(threadId).orElseThrow();
        return messageRepo.findByThreadOrderByCreatedAtAsc(th);
    }

    public DMMessage sendMessage(Long threadId, SendMessageRequest req) {
        DMThread th = threadRepo.findById(threadId).orElseThrow();
        User sender = userRepo.findById(req.senderId).orElseThrow();

        DMMessage msg = new DMMessage();
        msg.setThread(th);
        msg.setSender(sender);
        msg.setContent(req.content);
        return messageRepo.save(msg);
    }
}
