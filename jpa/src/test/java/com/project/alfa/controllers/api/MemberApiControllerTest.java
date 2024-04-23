package com.project.alfa.controllers.api;

import com.google.gson.Gson;
import com.project.alfa.config.security.TestSecurityConfig;
import com.project.alfa.config.security.WithCustomMockUser;
import com.project.alfa.entities.AuthInfo;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Role;
import com.project.alfa.services.MemberService;
import com.project.alfa.services.dto.MemberInfoResponseDto;
import com.project.alfa.services.dto.MemberJoinRequestDto;
import com.project.alfa.services.dto.MemberUpdateRequestDto;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestSecurityConfig.class)
@MockBean(JpaMetamodelMappingContext.class)
@WebMvcTest(MemberApiController.class)
class MemberApiControllerTest {
    
    @MockBean
    MemberService memberService;
    @Autowired
    MockMvc       mockMvc;
    Gson   gson;
    Member member;
    
    @BeforeEach
    void setup() {
        gson = new Gson();
        
        member = Member.builder()
                       .username("user1@mail.com")
                       .password("Password1!@")
                       .authInfo(AuthInfo.builder()
                                         .emailAuthToken(UUID.randomUUID().toString())
                                         .build())
                       .nickname("user1")
                       .role(Role.USER)
                       .build();
        
        when(memberService.join(any(MemberJoinRequestDto.class))).thenReturn(1L);
        when(memberService.findByUsername(anyString())).thenReturn(new MemberInfoResponseDto(member));
        doNothing().when(memberService).update(any(MemberUpdateRequestDto.class));
        doNothing().when(memberService).delete(anyLong(), anyString());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("회원 가입 페이지")
    void joinPage() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(get("/api/members")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().json(gson.toJson(new MemberJoinRequestDto())))
               .andDo(print());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("회원 가입")
    void join() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/members")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .content(gson.toJson(new MemberJoinRequestDto("user1@mail.com",
                                                                                                      "Password1!@",
                                                                                                      "Password1!@",
                                                                                                      "user1"))));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().string("Member joined successfully."))
               .andDo(print());
        
        verify(memberService, times(1)).join(any(MemberJoinRequestDto.class));
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("회원 가입, @Valid 체크")
    void join_validCheck() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/members")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .content(gson.toJson(new MemberJoinRequestDto())));
        
        //Then
        actions.andExpect(status().isBadRequest())
               .andExpect(
                       result -> assertThat(result.getResolvedException() instanceof MethodArgumentNotValidException))
               .andDo(print());
        
        verify(memberService, never()).join(any(MemberJoinRequestDto.class));
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("비밀번호 찾기")
    void forgotPassword() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/members/forgot-password")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .content("user1@mail.com"));
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().string("Successfully sending of \"Find password\" email."))
               .andDo(print());
        
        verify(memberService, times(1)).findPassword(anyString());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("비밀번호 찾기, 올바르지 않은 아이디(이메일)")
    void forgotPassword_invalidUsername() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/members/forgot-password")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .content(UUID.randomUUID().toString()));
        
        //Then
        actions.andExpect(status().isBadRequest())
               .andDo(print());
        
        verify(memberService, never()).findPassword(anyString());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("프로필 조회 페이지")
    @WithMockUser
    void profilePage() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(get("/api/members/profile")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().json(gson.toJson(new MemberInfoResponseDto(member))))
               .andDo(print());
        
        verify(memberService, times(1)).findByUsername(anyString());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("프로필 수정 페이지")
    @WithMockUser
    void profileUpdatePage() {
        //Given
        
        //When
        Map<String, Object> map = new HashMap<>();
        map.put("member", new MemberInfoResponseDto(member));
        map.put("form", new MemberUpdateRequestDto());
        
        ResultActions actions = mockMvc.perform(get("/api/members/profile-update")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().json(gson.toJson(map)))
               .andDo(print());
        
        verify(memberService, times(1)).findByUsername(anyString());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("프로필 수정")
    @WithCustomMockUser
    void profileUpdate() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/members/profile-update")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .content(gson.toJson(new MemberUpdateRequestDto(1L,
                                                                                                        "Password1!@",
                                                                                                        "user2",
                                                                                                        "Signature",
                                                                                                        "Password2!@",
                                                                                                        "Password2!@"))));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().string("Member updated successfully."))
               .andDo(print());
        
        verify(memberService, times(1)).update(any(MemberUpdateRequestDto.class));
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("프로필 수정, @Valid 체크")
    @WithCustomMockUser
    void profileUpdate_validCheck() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/members/profile-update")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .content(gson.toJson(new MemberUpdateRequestDto())));
        
        //Then
        actions.andExpect(status().isBadRequest())
               .andExpect(
                       result -> assertThat(result.getResolvedException() instanceof MethodArgumentNotValidException))
               .andDo(print());
        
        verify(memberService, never()).update(any(MemberUpdateRequestDto.class));
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("프로필 수정, UserDetails ID != DTO ID")
    @WithCustomMockUser(id = 2L)
    void profileUpdate_invalidId() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/members/profile-update")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .content(gson.toJson(new MemberUpdateRequestDto(1L,
                                                                                                        "Password1!@",
                                                                                                        "user2",
                                                                                                        "Signature",
                                                                                                        "Password2!@",
                                                                                                        "Password2!@"))));
        
        //Then
        actions.andExpect(status().isUnauthorized())
               .andExpect(content().string("Member update denied."))
               .andDo(print());
        
        verify(memberService, never()).update(any(MemberUpdateRequestDto.class));
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("회원 탈퇴")
    @WithCustomMockUser
    void deleteAccount() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/members/delete")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .content(gson.toJson(new MemberUpdateRequestDto(1L,
                                                                                                        "Password1!@",
                                                                                                        null,
                                                                                                        null,
                                                                                                        null,
                                                                                                        null))));
        
        //Then
        actions.andExpect(status().isFound())
               .andExpect(content().string("Member deleted successfully."))
               .andDo(print());
        
        verify(memberService, times(1)).delete(anyLong(), anyString());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("회원 탈퇴, UserDetails ID != DTO ID")
    @WithCustomMockUser(id = 2L)
    void deleteAccount_invalidId() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/members/delete")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .content(gson.toJson(new MemberUpdateRequestDto(1L,
                                                                                                        "Password1!@",
                                                                                                        null,
                                                                                                        null,
                                                                                                        null,
                                                                                                        null))));
        
        //Then
        actions.andExpect(status().isUnauthorized())
               .andExpect(content().string("Member delete denied."))
               .andDo(print());
        
        verify(memberService, never()).delete(anyLong(), anyString());
    }
    
}