package com.project.alfa.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.project.alfa.config.redis.EmbeddedRedisConfig;
import com.project.alfa.config.security.WithCustomMockUser;
import com.project.alfa.security.CustomUserDetails;
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

import java.util.Date;

import static org.assertj.core.api.Assertions.*;

@Import(EmbeddedRedisConfig.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JwtServiceTest {
    
    static final String USERNAME_KEY = "USERNAME";
    @Value("${jwt.secret}")
    String secret;
    @Value("${jwt.issuer}")
    String issuer;
    @Value("${jwt.token.access-expiration}")
    private long accessExpiration;
    @Value("${jwt.token.refresh-expiration}")
    private long refreshExpiration;
    @Autowired
    JwtService          jwtService;
    @Autowired
    StringRedisTemplate redisTemplate;
    Algorithm algorithm;
    
    @BeforeEach
    void setup() {
        algorithm = Algorithm.HMAC256(secret);
    }
    
    @Test
    @DisplayName("JWT Access 토큰 생성")
    @WithCustomMockUser
    void generateAccessToken() {
        //Given
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                                                                                 .getAuthentication()
                                                                                 .getPrincipal();
        
        //When
        String accessToken = jwtService.generateAccessToken(userDetails);
        
        //Then
        String username = JWT.require(algorithm)
                             .withIssuer(issuer)
                             .build()
                             .verify(accessToken)
                             .getClaim(USERNAME_KEY)
                             .asString();
        
        assertThat(userDetails.getUsername()).isEqualTo(username);
    }
    
    @Test
    @DisplayName("JWT Refresh 토큰 생성")
    @WithCustomMockUser
    void generateRefreshToken() {
        //Given
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                                                                                 .getAuthentication()
                                                                                 .getPrincipal();
        
        //When
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        
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
    
    @Test
    @DisplayName("JWT 토큰으로 아이디 조회")
    @WithCustomMockUser
    void getUsernameFromToken() {
        //Given
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                                                                                 .getAuthentication()
                                                                                 .getPrincipal();
        String token = JWT.create()
                          .withClaim(USERNAME_KEY, userDetails.getUsername())
                          .withExpiresAt(new Date(System.currentTimeMillis() + (1000 * accessExpiration)))
                          .withIssuer(issuer)
                          .sign(algorithm);
        
        //When
        String usernameFromToken = jwtService.getUsernameFromToken(token);
        
        //Then
        assertThat(userDetails.getUsername()).isEqualTo(usernameFromToken);
    }
    
    @Test
    @DisplayName("JWT 토큰 검증")
    @WithCustomMockUser
    void validateToken() {
        //Given
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                                                                                 .getAuthentication()
                                                                                 .getPrincipal();
        String token = JWT.create()
                          .withClaim(USERNAME_KEY, userDetails.getUsername())
                          .withExpiresAt(new Date(System.currentTimeMillis() + (1000 * accessExpiration)))
                          .withIssuer(issuer)
                          .sign(algorithm);
        
        //When
        boolean result = jwtService.validateToken(token, userDetails);
        
        //Then
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("JWT 토큰 검증, 알 수 없는 토큰")
    @WithCustomMockUser
    void validateToken_unknownToken() {
        //Given
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                                                                                 .getAuthentication()
                                                                                 .getPrincipal();
        String unknownToken = JWT.create()
                                 .withClaim(USERNAME_KEY, "user2@mail.com")
                                 .withExpiresAt(new Date(System.currentTimeMillis() + (1000 * accessExpiration)))
                                 .withIssuer(issuer)
                                 .sign(algorithm);
        
        //When
        boolean result = jwtService.validateToken(unknownToken, userDetails);
        
        //Then
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("JWT Access 토큰 Refresh")
    @WithCustomMockUser
    void refreshAccessToken() {
        //Given
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                                                                                 .getAuthentication()
                                                                                 .getPrincipal();
        String refreshToken       = jwtService.generateRefreshToken(userDetails);
        String storedRefreshToken = redisTemplate.opsForValue().get(userDetails.getUsername());
        if (!refreshToken.equals(storedRefreshToken))
            fail("Invalid Value");
        
        //When
        String accessToken = jwtService.refreshAccessToken(refreshToken, userDetails);
        
        //Then
        assertThat(accessToken).isNotNull();
    }
    
    @Test
    @DisplayName("JWT Access 토큰 Refresh, 알 수 없는 토큰")
    @WithCustomMockUser
    void refreshAccessToken_unknownToken() {
        //Given
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                                                                                 .getAuthentication()
                                                                                 .getPrincipal();
        String refreshToken       = jwtService.generateRefreshToken(userDetails);
        String storedRefreshToken = redisTemplate.opsForValue().get(userDetails.getUsername());
        if (!refreshToken.equals(storedRefreshToken))
            fail("Invalid Value");
        String unknownToken = JWT.create()
                                 .withClaim(USERNAME_KEY, "user2@mail.com")
                                 .withExpiresAt(new Date(System.currentTimeMillis() + (1000 * refreshExpiration)))
                                 .withIssuer(issuer)
                                 .sign(algorithm);
        
        
        //When
        
        //Then
        assertThatThrownBy(() -> jwtService.refreshAccessToken(unknownToken, userDetails))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid Refresh Token");
    }
    
    @Test
    @DisplayName("JWT 토큰 만료 시간 조회")
    void getExpirationFromToken() {
        //Given
        long expirationMillis = System.currentTimeMillis() + (1000 * accessExpiration);
        String token = JWT.create()
                          .withClaim(USERNAME_KEY, "user1@mail.com")
                          .withExpiresAt(new Date(expirationMillis))
                          .withIssuer(issuer)
                          .sign(algorithm);
        long expiration = expirationMillis / 1000;
        
        //When
        long expirationFromToken = jwtService.getExpirationFromToken(token);
        
        //Then
        assertThat(expirationFromToken).isEqualTo(expiration);
    }
    
    @Test
    @DisplayName("Redis 서버에서 JWT Refresh 토큰 삭제")
    @WithCustomMockUser
    void deleteRefreshToken() {
        //Given
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                                                                                 .getAuthentication()
                                                                                 .getPrincipal();
        String refreshToken             = jwtService.generateRefreshToken(userDetails);
        String beforeStoredRefreshToken = redisTemplate.opsForValue().get(userDetails.getUsername());
        if (!refreshToken.equals(beforeStoredRefreshToken))
            fail("Invalid Value");
        
        //When
        jwtService.deleteRefreshToken(refreshToken);
        
        //Then
        String afterStoredRefreshToken = redisTemplate.opsForValue().get(userDetails.getUsername());
        
        assertThat(afterStoredRefreshToken).isNull();
    }
    
}