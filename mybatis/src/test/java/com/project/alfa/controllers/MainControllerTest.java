package com.project.alfa.controllers;

import com.project.alfa.config.security.TestSecurityConfig;
import com.project.alfa.services.MemberService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestSecurityConfig.class)
@WebMvcTest(MainController.class)
class MainControllerTest {
    
    @MockBean
    MemberService memberService;
    @Autowired
    MockMvc       mockMvc;
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("메인 페이지")
    void mainPage() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(get("/"));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().string("Main page."))
               .andDo(print());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("이메일 인증")
    void verifyEmail() {
        //Given
        
        //When
        LocalDateTime requestTime = LocalDateTime.now();
        ResultActions actions = mockMvc.perform(get("/verify-email")
                                                        .header("Date", DateTimeFormatter.RFC_1123_DATE_TIME
                                                                .format(requestTime.atOffset(ZoneOffset.UTC)))
                                                        .param("email", "user1@mail")
                                                        .param("authToken", UUID.randomUUID().toString()));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().string("Email verified successfully."))
               .andDo(print());
        
        doNothing().when(memberService).verifyEmailAuth(anyString(), anyString(), eq(requestTime));
    }
    
}