package com.project.alfa.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.project.alfa.aop.annotation.LockAop;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JwtService {
    
    private static final String USERNAME_KEY = "USERNAME";
    
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.issuer}")
    private String issuer;
    @Value("${jwt.token.access-expiration}")
    private long   accessExpiration;
    @Value("${jwt.token.refresh-expiration}")
    private long   refreshExpiration;
    
    private final StringRedisTemplate redisTemplate;
    
    private Algorithm algorithm;
    
    @PostConstruct
    public void postConstruct() {
        algorithm = Algorithm.HMAC256(secret);
    }
    
    /**
     * JWT Access 토큰 생성
     *
     * @param userDetails - 인증 정보
     * @return JWT Access 토큰
     */
    public String generateAccessToken(final UserDetails userDetails) {
        return JWT.create()
                  .withClaim(USERNAME_KEY, userDetails.getUsername())
                  .withExpiresAt(new Date(System.currentTimeMillis() + (1000 * accessExpiration)))
                  .withIssuer(issuer)
                  .sign(algorithm);
    }
    
    /**
     * JWT Refresh 토큰 생성 및 Redis 서버 저장
     *
     * @param userDetails - 인증 정보
     * @return JWT Refresh 토큰
     */
    @LockAop
    public String generateRefreshToken(final UserDetails userDetails) {
        String refreshToken = JWT.create()
                                 .withClaim(USERNAME_KEY, userDetails.getUsername())
                                 .withExpiresAt(new Date(System.currentTimeMillis() + (1000 * refreshExpiration)))
                                 .withIssuer(issuer)
                                 .sign(algorithm);
        redisTemplate.opsForValue().set(userDetails.getUsername(),
                                        refreshToken,
                                        refreshExpiration,
                                        TimeUnit.MILLISECONDS);
        return refreshToken;
    }
    
    /**
     * JWT 토큰으로 아이디 조회
     *
     * @param token - JWT 토큰
     * @return 아이디
     */
    public String getUsernameFromToken(final String token) {
        return JWT.require(algorithm)
                  .withIssuer(issuer)
                  .build()
                  .verify(token)
                  .getClaim(USERNAME_KEY)
                  .asString();
    }
    
    /**
     * JWT 토큰 검증
     *
     * @param token       - JWT 토큰
     * @param userDetails - 인증 정보
     * @return 검증 결과
     */
    public boolean validateToken(final String token, final UserDetails userDetails) {
        return getUsernameFromToken(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
    
    /**
     * JWT Refresh 토큰으로 Access 토큰 갱신
     *
     * @param refreshToken - JWT Refresh 토큰
     * @param userDetails  - 인증 정보
     * @return 신규 JWT Access 토큰
     */
    public String refreshAccessToken(final String refreshToken, final UserDetails userDetails) {
        String username           = getUsernameFromToken(refreshToken);
        String storedRefreshToken = redisTemplate.opsForValue().get(username);
        
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken))
            throw new RuntimeException("Invalid Refresh Token");
        
        return generateAccessToken(userDetails);
    }
    
    /**
     * JWT 토큰 만료 시간 조회
     *
     * @param token - JWT 토큰
     * @return 만료 시간(초)
     */
    public long getExpirationFromToken(final String token) {
        return JWT.require(algorithm).withIssuer(issuer).build().verify(token).getExpiresAt().getTime() / 1000;
    }
    
    /**
     * Redis 서버에서 JWT Refresh 토큰 삭제
     *
     * @param refreshToken - JWT Refresh 토큰
     */
    @LockAop
    public void deleteRefreshToken(final String refreshToken) {
        redisTemplate.delete(getUsernameFromToken(refreshToken));
    }
    
    /**
     * JWT 토큰 만료 여부 확인
     *
     * @param token - JWT 토큰
     * @return 만료 여부
     */
    private boolean isTokenExpired(final String token) {
        return JWT.require(algorithm).withIssuer(issuer).build().verify(token).getExpiresAt().before(new Date());
    }
    
}
