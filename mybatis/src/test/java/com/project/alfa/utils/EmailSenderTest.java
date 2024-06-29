package com.project.alfa.utils;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class EmailSenderTest {
    
    @RegisterExtension
    static GreenMailExtension greenMailExtension = new GreenMailExtension(new ServerSetup(3025, null, "smtp"))
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("springboot", "secret"))
            .withPerMethodLifecycle(true);
    
    @Autowired
    EmailSender emailSender;
    
    @SneakyThrows({MessagingException.class, IOException.class})
    @Test
    @DisplayName("sendVerificationEmail")
    void sendVerificationEmail() {
        //Given
        String        to         = "receiver@example.com";
        String        authToken  = UUID.randomUUID().toString();
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(5);
        
        //When
        emailSender.sendVerificationEmail(to, authToken, expireTime);
        
        //Then
        greenMailExtension.waitForIncomingEmail(5000, 1);
        MimeMessage[] receivedMessages = greenMailExtension.getReceivedMessages();
        MimeMessage   receivedMessage  = receivedMessages[0];
        
        assertThat(receivedMessages).hasSize(1);
        assertThat(to).isEqualTo(receivedMessage.getAllRecipients()[0].toString());
        assertThat("이메일 인증").isEqualTo(receivedMessage.getSubject());
        assertThat(receivedMessage.getContent().toString().trim())
                .contains("계정 인증을 완료하기 위해 제한 시간 내 다음 링크를 클릭해주세요.")
                .contains("/verify-email?email=" + to + "&authToken=" + authToken);
    }
    
    @SneakyThrows({MessagingException.class, IOException.class})
    @Test
    @DisplayName("sendPasswordResetEmail")
    void sendPasswordResetEmail() {
        //Given
        String to           = "receiver@example.com";
        String tempPassword = "temporaryPassword";
        
        //When
        emailSender.sendPasswordResetEmail(to, tempPassword);
        
        //Then
        greenMailExtension.waitForIncomingEmail(5000, 1);
        MimeMessage[] receivedMessages = greenMailExtension.getReceivedMessages();
        MimeMessage   receivedMessage  = receivedMessages[0];
        
        assertThat(receivedMessages).hasSize(1);
        assertThat(to).isEqualTo(receivedMessage.getAllRecipients()[0].toString());
        assertThat("비밀번호 찾기 결과").isEqualTo(receivedMessage.getSubject());
        assertThat(receivedMessage.getContent().toString().trim())
                .contains("입력하신 정보로 찾은 계정의 임시 비밀번호는 다음과 같습니다.")
                .contains("임시 비밀번호: " + tempPassword)
                .contains("임시 비밀번호로 로그인한 다음 비밀번호를 변경해주세요.");
    }
    
}