package com.project.alfa.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@EnableAsync
@RequiredArgsConstructor
public class EmailSender {
    
    @Value("${email.from}")
    private String fromAddress;
    @Value("${app.frontend.url}")
    private String url;
    
    private final JavaMailSender mailSender;
    
    /**
     * 인증 메일 전송
     *
     * @param email      - 메일 주소
     * @param authToken  - 인증 토큰
     * @param expireTime - 인증 만료 제한 시간
     */
    @Async
    public void sendVerificationEmail(final String email, final String authToken, final LocalDateTime expireTime) {
        SimpleMailMessage smm = createMailMessage(email);
        smm.setSubject("이메일 인증");
        smm.setText("계정 인증을 완료하기 위해 제한 시간 내 다음 링크를 클릭해주세요.\n" +
                    "인증 만료 제한 시간: " + expireTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")) +
                    "\n" +
                    url + "/verify-email?email=" + email + "&authToken=" + authToken);
        
        mailSender.send(smm);
    }
    
    /**
     * 비밀번호 찾기 결과 메일 전송
     *
     * @param email        - 메일 주소
     * @param tempPassword - 임시 비밀번호
     */
    @Async
    public void sendPasswordResetEmail(final String email, final String tempPassword) {
        SimpleMailMessage smm = createMailMessage(email);
        smm.setSubject("비밀번호 찾기 결과");
        smm.setText("입력하신 정보로 찾은 계정의 임시 비밀번호는 다음과 같습니다.\n" +
                    "임시 비밀번호: " + tempPassword + "\n" +
                    "임시 비밀번호로 로그인한 다음 비밀번호를 변경해주세요.");
        
        mailSender.send(smm);
    }
    
    /**
     * 메일 객체(SimpleMailMessage) 생성
     *
     * @param email - 메일 주소
     * @return 메일 객체
     */
    private SimpleMailMessage createMailMessage(final String email) {
        SimpleMailMessage smm = new SimpleMailMessage();
        smm.setFrom(fromAddress);
        smm.setTo(email);
        return smm;
    }
    
}
