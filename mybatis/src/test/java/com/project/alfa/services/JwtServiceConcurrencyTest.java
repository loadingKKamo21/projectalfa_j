package com.project.alfa.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.project.alfa.config.redis.EmbeddedRedisConfig;
import com.project.alfa.config.security.WithCustomMockUser;
import com.project.alfa.security.CustomUserDetails;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Slf4j
@Import(EmbeddedRedisConfig.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JwtServiceConcurrencyTest {
    
    static final int    THREAD_COUNT = 100;
    static final String USERNAME_KEY = "USERNAME";
    @Value("${jwt.secret}")
    String secret;
    @Value("${jwt.issuer}")
    String issuer;
    @Autowired
    JwtService          jwtService;
    @Autowired
    StringRedisTemplate redisTemplate;
    Algorithm algorithm;
    
    @BeforeEach
    void setup() {
        algorithm = Algorithm.HMAC256(secret);
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("JWT Refresh 토큰 생성, 멀티 스레드 락 적용")
    @WithCustomMockUser
    void generateRefreshToken_multiThreads() {
        //Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT);
        
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                                                                                 .getAuthentication()
                                                                                 .getPrincipal();
        
        AtomicReference<String> refreshTokenRef = new AtomicReference<>();
        
        //When
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    refreshTokenRef.set(jwtService.generateRefreshToken(userDetails));
                } catch (RuntimeException e) {
                    log.info("[{}-thread] 락 획득 실패: {}", threadIndex, e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        
        String refreshToken = refreshTokenRef.get();
        
        //Then
        String username = JWT.require(algorithm)
                             .withIssuer(issuer)
                             .build()
                             .verify(refreshToken)
                             .getClaim(USERNAME_KEY)
                             .asString();
        String storedRefreshToken = redisTemplate.opsForValue().get(userDetails.getUsername());
        
        assertThat(userDetails.getUsername()).isEqualTo(username);
        assertThat(refreshToken).isEqualTo(storedRefreshToken);
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("Redis 서버에서 JWT Refresh 토큰 삭제, 멀티 스레드 락 적용")
    @WithCustomMockUser
    void deleteRefreshToken_multiThreads() {
        //Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT);
        
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                                                                                 .getAuthentication()
                                                                                 .getPrincipal();
        String refreshToken             = jwtService.generateRefreshToken(userDetails);
        String beforeStoredRefreshToken = redisTemplate.opsForValue().get(userDetails.getUsername());
        if (!refreshToken.equals(beforeStoredRefreshToken))
            fail("Invalid Value");
        
        //When
        for (int i = 0; i < THREAD_COUNT; i++)
            executorService.execute(() -> {
                try {
                    jwtService.deleteRefreshToken(refreshToken);
                } finally {
                    countDownLatch.countDown();
                }
            });
        countDownLatch.await();
        executorService.shutdown();
        
        //Then
        String afterStoredRefreshToken = redisTemplate.opsForValue().get(userDetails.getUsername());
        
        assertThat(afterStoredRefreshToken).isNull();
    }
    
}