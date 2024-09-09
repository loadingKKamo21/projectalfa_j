package com.project.alfa.services;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import com.project.alfa.config.DummyGenerator;
import com.project.alfa.config.TestConfig;
import com.project.alfa.entities.Member;
import com.project.alfa.repositories.MemberRepository;
import com.project.alfa.repositories.mybatis.MemberMapper;
import com.project.alfa.services.dto.MemberUpdateRequestDto;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Import(TestConfig.class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberServiceConcurrencyTest {
    
    static final int THREAD_COUNT = 100;
    
    @RegisterExtension
    static GreenMailExtension greenMailExtension = new GreenMailExtension(new ServerSetup(3025, null, "smtp"))
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("springboot", "secret"))
            .withPerMethodLifecycle(true);
    
    @Autowired
    MemberService    memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PasswordEncoder  passwordEncoder;
    @Autowired
    MemberMapper     memberMapper;
    @Autowired
    DummyGenerator   dummy;
    
    @BeforeEach
    void setup() {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(() -> {
            memberRepository.deleteAll();
        });
        executorService.shutdown();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("이메일 인증, 멀티 스레드 락 적용")
    void verifyEmailAuth_multiThreads() {
        //Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT + 1);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT + 1);
        
        AtomicLong              idRef             = new AtomicLong();
        AtomicBoolean           beforeRef         = new AtomicBoolean();
        AtomicReference<String> usernameRef       = new AtomicReference<>();
        AtomicReference<String> emailAuthTokenRef = new AtomicReference<>();
        
        executorService.execute(() -> {
            try {
                Member member = dummy.createMembers(1, true).get(0);
                idRef.set(member.getId());
                beforeRef.set(memberMapper.findById(member.getId()).getAuthInfo().isAuth());
                usernameRef.set(member.getUsername());
                emailAuthTokenRef.set(member.getAuthInfo().getEmailAuthToken());
            } finally {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await(1, TimeUnit.SECONDS);
        
        Long    id             = idRef.get();
        boolean before         = beforeRef.get();
        String  username       = usernameRef.get();
        String  emailAuthToken = emailAuthTokenRef.get();
        
        //When
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    memberService.verifyEmailAuth(username, emailAuthToken, LocalDateTime.now());
                } catch (RuntimeException e) {
                    log.info("[{}-thread] 락 획득 실패: {}", threadIndex, e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        
        //Then
        boolean after = memberMapper.findById(id).getAuthInfo().isAuth();
        
        assertThat(before).isFalse();
        assertThat(after).isTrue();
    }
    
    @SneakyThrows({MessagingException.class, InterruptedException.class})
    @Test
    @DisplayName("비밀번호 찾기, 멀티 스레드 락 적용")
    void findPassword_multiThreads() {
        //Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT + 1);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT + 1);
        
        AtomicLong              idRef       = new AtomicLong();
        AtomicReference<String> usernameRef = new AtomicReference<>();
        AtomicReference<String> passwordRef = new AtomicReference<>();
        
        executorService.execute(() -> {
            try {
                Member member = dummy.createMembers(1, true).get(0);
                idRef.set(member.getId());
                usernameRef.set(member.getUsername());
                passwordRef.set(member.getPassword());
                memberMapper.authenticateEmail(member.getUsername(),
                                               member.getAuthInfo().getEmailAuthToken(),
                                               LocalDateTime.now());    //이메일 인증
            } finally {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await(1, TimeUnit.SECONDS);
        
        Long   id       = idRef.get();
        String username = usernameRef.get();
        String password = passwordRef.get();
        
        AtomicInteger mailCountRef = new AtomicInteger(THREAD_COUNT);
        
        //When
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    memberService.findPassword(username);
                } catch (RuntimeException e) {
                    mailCountRef.decrementAndGet();
                    log.info("[{}-thread] 락 획득 실패: {}", threadIndex, e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        
        int mailCount = mailCountRef.get();
        
        //Then
        greenMailExtension.waitForIncomingEmail(5000, mailCount);
        Member        findMember       = memberMapper.findById(id);
        MimeMessage[] receivedMessages = greenMailExtension.getReceivedMessages();
        
        //비밀번호 찾기를 시도하면 20자리 임시 비밀번호로 변경되고, 메일로 전송됨
        assertThat(findMember.getPassword()).isNotEqualTo(passwordEncoder.encode(password));
        assertThat(passwordEncoder.matches("Password1!@", findMember.getPassword())).isFalse();
        
        assertThat(receivedMessages).hasSize(mailCount);
        assertThat(findMember.getUsername())
                .isEqualTo(receivedMessages[0].getRecipients(Message.RecipientType.TO)[0].toString());
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("정보 수정, 멀티 스레드 락 적용")
    void update_multiThreads() {
        //Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT + 1);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT + 1);
        
        AtomicLong              idRef           = new AtomicLong();
        AtomicReference<Member> beforeMemberRef = new AtomicReference<>();
        
        executorService.execute(() -> {
            try {
                Member member = dummy.createMembers(1, true).get(0);
                idRef.set(member.getId());
                memberMapper.authenticateEmail(member.getUsername(),
                                               member.getAuthInfo().getEmailAuthToken(),
                                               LocalDateTime.now());    //이메일 인증
                beforeMemberRef.set(memberMapper.findById(member.getId()));
            } finally {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await(1, TimeUnit.SECONDS);
        
        Long   id           = idRef.get();
        Member beforeMember = beforeMemberRef.get();
        
        MemberUpdateRequestDto dto = new MemberUpdateRequestDto(id, "Password1!@", "user2", "Signature",
                                                                "Password2!@", "Password2!@");
        
        //When
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    memberService.update(dto);
                } catch (RuntimeException e) {
                    log.info("[{}-thread] 락 획득 실패: {}", threadIndex, e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        
        //Then
        Member afterMember = memberMapper.findById(id);
        
        assertThat(passwordEncoder.matches(dto.getNewPassword(), afterMember.getPassword())).isTrue();
        assertThat(passwordEncoder.matches(dto.getPassword(), afterMember.getPassword())).isFalse();
        assertThat(afterMember.getNickname()).isEqualTo(dto.getNickname());
        assertThat(beforeMember.getNickname()).isNotEqualTo(dto.getNickname());
        assertThat(afterMember.getSignature()).isEqualTo(dto.getSignature());
        assertThat(beforeMember.getSignature()).isNotEqualTo(dto.getSignature());
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("회원 탈퇴, 멀티 스레드 락 적용")
    void delete_multiThreads() {
        //Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT + 1);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT + 1);
        
        AtomicLong idRef = new AtomicLong();
        
        executorService.execute(() -> {
            try {
                Member member = dummy.createMembers(1, true).get(0);
                idRef.set(member.getId());
            } finally {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await(1, TimeUnit.SECONDS);
        
        Long   id       = idRef.get();
        String password = "Password1!@";
        
        //When
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    memberService.delete(id, password);
                } catch (RuntimeException e) {
                    log.info("[{}-thread] 락 획득 실패: {}", threadIndex, e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        
        //Then
        assertThat(memberMapper.findByIdAndDeleteYn(id, false)).isNull();
    }
    
}
