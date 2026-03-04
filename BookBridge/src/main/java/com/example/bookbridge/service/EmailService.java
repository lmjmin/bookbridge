package com.example.bookbridge.service;

import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /** application-*.properties/yml 에서 주입 (없으면 계정 그대로) */
    @Value("${app.mail.from-email:}")
    private String fromEmail;

    @Value("${app.mail.from-name:BookBridge}")
    private String fromName;

    public boolean sendCode(String to, String code) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            if (fromEmail != null && !fromEmail.isBlank()) {
                // 예: "BookBridge <alswn6546@naver.com>"
                msg.setFrom(new InternetAddress(fromEmail, fromName).toString());
            } else {
                // fromEmail 미설정 시, 메일서버 계정 기본값 사용
            }
            msg.setSubject("[BookBridge] 이메일 인증 코드");
            msg.setText("인증 코드: " + code + "\n\n5분 이내에 입력해주세요.");
            mailSender.send(msg);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
