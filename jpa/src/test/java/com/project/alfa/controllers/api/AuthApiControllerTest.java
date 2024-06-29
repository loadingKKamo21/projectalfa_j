package com.project.alfa.controllers.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.Gson;
import com.project.alfa.config.security.TestSecurityConfig;
import com.project.alfa.config.security.WithCustomMockUser;
import com.project.alfa.services.JwtService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.servlet.http.Cookie;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@MockBean(JpaMetamodelMappingContext.class)
@WebMvcTest(AuthApiController.class)
class AuthApiControllerTest {
    
    @MockBean
    JwtService jwtService;
    @Autowired
    MockMvc    mockMvc;
    Gson   gson;
    String responseRefreshToken;
    String responseAccessToken;
    
    @BeforeEach
    void setup() {
        gson = new Gson();
        
        long refreshExpirationMillis = System.currentTimeMillis() + (1000 * 604800);
        responseRefreshToken = JWT.create()
                                  .withClaim("USERNAME", "user1@mail.com")
                                  .withExpiresAt(new Date(refreshExpirationMillis))
                                  .withIssuer("issuer")
                                  .sign(Algorithm.HMAC256("secret"));
        
        
        responseAccessToken = JWT.create()
                                 .withClaim("USERNAME", "user1@mail.com")
                                 .withExpiresAt(new Date(System.currentTimeMillis() + (1000 * 1800)))
                                 .withIssuer("issuer")
                                 .sign(Algorithm.HMAC256("secret"));
        
        when(jwtService.refreshAccessToken(anyString(), any(UserDetails.class))).thenReturn(responseAccessToken);
        when(jwtService.generateRefreshToken(any(UserDetails.class))).thenReturn(responseRefreshToken);
        when(jwtService.getExpirationFromToken(anyString())).thenReturn(refreshExpirationMillis / 1000);
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("JWT Access 토큰 Refresh, 쿠키 사용")
    @WithCustomMockUser
    void refreshTokenByCookie() {
        //Given
        String requestRefreshToken = JWT.create()
                                        .withClaim("USERNAME", "user1@mail.com")
                                        .withExpiresAt(new Date(System.currentTimeMillis() + (1000 * 604800)))
                                        .withIssuer("issuer")
                                        .sign(Algorithm.HMAC256("secret"));
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/auth/refresh")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .cookie(new Cookie("refreshToken", requestRefreshToken)));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(header().string("Authorization", "Bearer " + responseAccessToken))
               .andExpect(cookie().value("refreshToken", responseRefreshToken))
               .andExpect(content().string("Access Token refresh complete."))
               .andDo(print());
        
        verify(jwtService, times(1)).refreshAccessToken(anyString(), any(UserDetails.class));
        verify(jwtService, times(1)).generateRefreshToken(any(UserDetails.class));
        verify(jwtService, times(1)).getExpirationFromToken(anyString());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("JWT Access 토큰 Refresh, 헤더 사용")
    @WithCustomMockUser
    void refreshTokenByHeader() {
        //Given
        String requestRefreshToken = JWT.create()
                                        .withClaim("USERNAME", "user1@mail.com")
                                        .withExpiresAt(new Date(System.currentTimeMillis() + (1000 * 604800)))
                                        .withIssuer("issuer")
                                        .sign(Algorithm.HMAC256("secret"));
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/auth/refresh")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .header("Authorization", "Refresh " + requestRefreshToken));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(header().string("Authorization", "Bearer " + responseAccessToken))
               .andExpect(cookie().value("refreshToken", responseRefreshToken))
               .andExpect(content().string("Access Token refresh complete."))
               .andDo(print());
        
        verify(jwtService, times(1)).refreshAccessToken(anyString(), any(UserDetails.class));
        verify(jwtService, times(1)).generateRefreshToken(any(UserDetails.class));
        verify(jwtService, times(1)).getExpirationFromToken(anyString());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("JWT Access 토큰 Refresh, JSON 사용")
    @WithCustomMockUser
    void refreshTokenByJson() {
        //Given
        String requestRefreshToken = JWT.create()
                                        .withClaim("USERNAME", "user1@mail.com")
                                        .withExpiresAt(new Date(System.currentTimeMillis() + (1000 * 604800)))
                                        .withIssuer("issuer")
                                        .sign(Algorithm.HMAC256("secret"));
        Map<String, String> body = new HashMap<>();
        body.put("refreshToken", requestRefreshToken);
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/auth/refresh")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .content(gson.toJson(body)));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(header().string("Authorization", "Bearer " + responseAccessToken))
               .andExpect(cookie().value("refreshToken", responseRefreshToken))
               .andExpect(content().string("Access Token refresh complete."))
               .andDo(print());
        
        verify(jwtService, times(1)).refreshAccessToken(anyString(), any(UserDetails.class));
        verify(jwtService, times(1)).generateRefreshToken(any(UserDetails.class));
        verify(jwtService, times(1)).getExpirationFromToken(anyString());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("JWT Access 토큰 Refresh, 토큰 없이 요청")
    @WithCustomMockUser
    void refreshToken_noToken() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/auth/refresh")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE));
        
        //Then
        actions.andExpect(status().isBadRequest())
               .andExpect(content().string("Refresh Token is missing."))
               .andDo(print());
        
        verify(jwtService, never()).refreshAccessToken(anyString(), any(UserDetails.class));
        verify(jwtService, never()).generateRefreshToken(any(UserDetails.class));
        verify(jwtService, never()).getExpirationFromToken(anyString());
    }
    
}
