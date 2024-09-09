package com.project.alfa.services;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import com.project.alfa.services.dto.MemberUpdateRequestDto;
import com.project.alfa.config.DummyGenerator;
import com.project.alfa.config.TestConfig;
import com.project.alfa.entities.Member;
import com.project.alfa.repositories.v1.MemberRepositoryV1;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
    PlatformTransactionManager transactionManager;
    @Autowired
    MemberService              memberService;
    @Autowired
    MemberRepositoryV1         memberRepository;
    //@Autowired
    //MemberRepositoryV2 memberRepository;
    //@Autowired
    //MemberRepositoryV3 memberRepository;
    @Autowired
    PasswordEncoder            passwordEncoder;
    @PersistenceContext
    EntityManager              em;
    @Autowired
    DummyGenerator             dummy;
    
    @AfterEach
    void clear() {
        em.flush();
        em.clear();
    }
    
    @BeforeEach
    void setup() {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(() -> {
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.execute(status -> {
                try {
                    memberRepository.deleteAll();
                } catch (Exception e) {
                    status.setRollbackOnly();
                    throw e;
                }
                return null;
            });
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
        
        AtomicReference<Member> memberRef = new AtomicReference<>();
        
        executorService.execute(() -> {
            TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                Member member = dummy.createMembers(1).get(0);
                em.persist(member);
                transactionManager.commit(transactionStatus);
                memberRef.set(member);
            } catch (Exception e) {
                transactionManager.rollback(transactionStatus);
                throw e;
            } finally {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await(1, TimeUnit.SECONDS);
        
        Member  member = memberRef.get();
        Long    id     = member.getId();
        boolean before = member.getAuthInfo().isAuth();
        
        //When
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    memberService.verifyEmailAuth(member.getUsername(),
                                                  member.getAuthInfo().getEmailAuthToken(),
                                                  LocalDateTime.now());
                } catch (RuntimeException e) {
                    log.info("[{}-thread] 락 획득 실패: {}", threadIndex, e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        clear();
        
        //Then
        boolean after = em.find(Member.class, id).getAuthInfo().isAuth();
        
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
        
        AtomicReference<Member> memberRef = new AtomicReference<>();
        
        executorService.execute(() -> {
            TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                Member member = dummy.createMembers(1).get(0);
                em.persist(member);
                member.authenticate();  //이메일 인증
                transactionManager.commit(transactionStatus);
                memberRef.set(member);
            } catch (Exception e) {
                transactionManager.rollback(transactionStatus);
                throw e;
            } finally {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await(1, TimeUnit.SECONDS);
        
        Member member = memberRef.get();
        Long   id     = member.getId();
        
        AtomicInteger mailCountRef = new AtomicInteger(THREAD_COUNT);
        
        //When
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    memberService.findPassword(member.getUsername());
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
        clear();
        
        int mailCount = mailCountRef.get();
        
        //Then
        greenMailExtension.waitForIncomingEmail(5000, mailCount);
        Member        findMember       = em.find(Member.class, id);
        MimeMessage[] receivedMessages = greenMailExtension.getReceivedMessages();
        
        //비밀번호 찾기를 시도하면 20자리 임시 비밀번호로 변경되고, 메일로 전송됨
        assertThat(findMember.getPassword()).isNotEqualTo(passwordEncoder.encode(member.getPassword()));
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
        
        AtomicReference<Member> memberRef = new AtomicReference<>();
        
        executorService.execute(() -> {
            TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                Member member = dummy.createMembers(1).get(0);
                em.persist(member);
                member.authenticate();  //이메일 인증
                transactionManager.commit(transactionStatus);
                memberRef.set(member);
            } catch (Exception e) {
                transactionManager.rollback(transactionStatus);
                throw e;
            } finally {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await(1, TimeUnit.SECONDS);
        
        Member member          = memberRef.get();
        Long   id              = member.getId();
        String beforeNickname  = member.getNickname();
        String beforeSignature = member.getSignature();
        
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
        clear();
        
        //Then
        Member afterMember = em.find(Member.class, id);
        
        assertThat(passwordEncoder.matches(dto.getNewPassword(), afterMember.getPassword())).isTrue();
        assertThat(passwordEncoder.matches(dto.getPassword(), afterMember.getPassword())).isFalse();
        assertThat(afterMember.getNickname()).isEqualTo(dto.getNickname());
        assertThat(beforeNickname).isNotEqualTo(dto.getNickname());
        assertThat(afterMember.getSignature()).isEqualTo(dto.getSignature());
        assertThat(beforeSignature).isNotEqualTo(dto.getSignature());
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
            TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                Member member = dummy.createMembers(1).get(0);
                em.persist(member);
                transactionManager.commit(transactionStatus);
                idRef.set(member.getId());
            } catch (Exception e) {
                transactionManager.rollback(transactionStatus);
                throw e;
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
        clear();
        
        //Then
        assertThat(em.find(Member.class, id).isDeleteYn()).isTrue();
    }
    
}
