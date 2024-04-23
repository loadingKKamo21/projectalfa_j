package com.project.alfa.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@EnableAsync
@RequiredArgsConstructor
public class EmailSender {
    
    private final JavaMailSender mailSender;
    
    /**
     * 이메일 전송
     *
     * @param email   - 이메일 주소
     * @param subject - 제목
     * @param content - 내용
     */
    @Async
    public void send(final String email, final String subject, final String content) {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        log.info("[{}] ==> Start sending mail", uuid);
        log.info("[{}] To: {}", uuid, email);
        log.info("[{}] Subject: {}", uuid, subject);
        log.info("[{}] Content: {}", uuid, content);
        log.info("[{}] Content.length: {}", uuid, content.length());
        
        SimpleMailMessage smm = new SimpleMailMessage();
        smm.setTo(email);
        smm.setSubject(subject);
        smm.setText(content);
        
        mailSender.send(smm);
        
        log.info("[{}] <== Complete sending mail", uuid);
    }
    
}
