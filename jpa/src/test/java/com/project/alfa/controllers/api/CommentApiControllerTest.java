package com.project.alfa.controllers.api;

import com.google.gson.Gson;
import com.project.alfa.config.security.TestSecurityConfig;
import com.project.alfa.config.security.WithCustomMockUser;
import com.project.alfa.entities.*;
import com.project.alfa.services.CommentService;
import com.project.alfa.services.dto.CommentRequestDto;
import com.project.alfa.services.dto.CommentResponseDto;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestSecurityConfig.class)
@MockBean(JpaMetamodelMappingContext.class)
@WebMvcTest(CommentApiController.class)
class CommentApiControllerTest {
    
    @MockBean
    CommentService commentService;
    @Autowired
    MockMvc        mockMvc;
    Gson                     gson;
    Page<CommentResponseDto> comments;
    
    @BeforeEach
    void setup() {
        gson = new Gson();
        
        Member writer = Member.builder()
                              .username("user1@mail.com")
                              .password("Password1!@")
                              .authInfo(AuthInfo.builder()
                                                .emailAuthToken(UUID.randomUUID().toString())
                                                .build())
                              .nickname("user1")
                              .role(Role.USER)
                              .build();
        Post post = Post.builder()
                        .writer(writer)
                        .title("Test title")
                        .content("Test content")
                        .noticeYn(false)
                        .build();
        List<CommentResponseDto> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++)
            list.add(new CommentResponseDto(Comment.builder()
                                                   .writer(writer)
                                                   .post(post)
                                                   .content("Test content" + i)
                                                   .build()));
        comments = new PageImpl<>(list);
        
        when(commentService.findAllPageByPost(anyLong(), any(Pageable.class))).thenReturn(comments);
        when(commentService.findAllPageByWriter(anyLong(), any(Pageable.class))).thenReturn(comments);
        when(commentService.create(any(CommentRequestDto.class))).thenReturn(1L);
        when(commentService.read(anyLong())).thenReturn(comments.getContent().get(0));
        doNothing().when(commentService).update(any(CommentRequestDto.class));
        doNothing().when(commentService).delete(anyLong(), anyLong());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("게시글 기준 댓글 목록 페이지")
    void commentsList() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(get("/api/posts/{postId}/comments", 1)
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .param("page", "0")
                                                        .param("size", "10"));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().json(gson.toJson(comments)))
               .andDo(print());
        
        verify(commentService, times(1)).findAllPageByPost(anyLong(), any(Pageable.class));
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("작성자 기준 댓글 목록 페이지")
    @WithCustomMockUser
    void commentsListByWriter() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(get("/api/comments/writer")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .param("page", "0")
                                                        .param("size", "10"));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().json(gson.toJson(comments)))
               .andDo(print());
        
        verify(commentService, times(1)).findAllPageByWriter(anyLong(), any(Pageable.class));
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("댓글 작성 페이지")
    @WithMockUser
    void writeCommentPage() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(get("/api/posts/{postId}/comments/write", 1)
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().json(gson.toJson(new CommentRequestDto())))
               .andDo(print());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("댓글 작성")
    @WithCustomMockUser
    void writeComment() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/posts/{postId}/comments/write", 1)
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .content(gson.toJson(new CommentRequestDto(null,
                                                                                                   1L,
                                                                                                   1L,
                                                                                                   "Test content"))));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().string("Comment created successfully."))
               .andDo(print());
        
        verify(commentService, times(1)).create(any(CommentRequestDto.class));
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("댓글 작성, @Valid 체크")
    @WithMockUser
    void writeComment_validCheck() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/posts/{postId}/comments/write", 1)
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .content(gson.toJson(new CommentRequestDto())));
        
        //Then
        actions.andExpect(status().isBadRequest())
               .andExpect(result ->
                                  assertThat(result.getResolvedException() instanceof MethodArgumentNotValidException))
               .andDo(print());
        
        verify(commentService, never()).create(any(CommentRequestDto.class));
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("댓글 수정 페이지")
    @WithCustomMockUser
    void updateCommentPage() {
        //Given
        
        //When
        Map<String, Object> map = new HashMap<>();
        map.put("comment", comments.getContent().get(0));
        map.put("form", new CommentRequestDto());
        
        ResultActions actions = mockMvc.perform(get("/api/posts/{postId}/comments/write/{commentId}", 1, 1)
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().json(gson.toJson(map)))
               .andDo(print());
        
        verify(commentService, times(1)).read(anyLong());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("댓글 수정")
    @WithCustomMockUser
    void updateComment() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/posts/{postId}/comments/write/{commentId}", 1, 1)
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .content(gson.toJson(new CommentRequestDto(1L,
                                                                                                   1L,
                                                                                                   1L,
                                                                                                   "Test content"))));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().string("Comment updated successfully."))
               .andDo(print());
        
        verify(commentService, times(1)).update(any(CommentRequestDto.class));
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("댓글 수정, @Valid 체크")
    @WithMockUser
    void updateComment_validCheck() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/posts/{postId}/comments/write/{commentId}", 1, 1)
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .content(gson.toJson(new CommentRequestDto())));
        
        //Then
        actions.andExpect(status().isBadRequest())
               .andExpect(result ->
                                  assertThat(result.getResolvedException() instanceof MethodArgumentNotValidException))
               .andDo(print());
        
        verify(commentService, never()).update(any(CommentRequestDto.class));
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("댓글 삭제")
    @WithCustomMockUser
    void deleteComment() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/posts/{postId}/comments/delete", 1)
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .content(gson.toJson(new CommentRequestDto(1L,
                                                                                                   1L,
                                                                                                   1L,
                                                                                                   null))));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().string("Comment deleted successfully."))
               .andDo(print());
        
        verify(commentService, times(1)).delete(anyLong(), anyLong());
    }
    
}