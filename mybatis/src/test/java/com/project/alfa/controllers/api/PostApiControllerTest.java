package com.project.alfa.controllers.api;

import com.google.gson.Gson;
import com.project.alfa.config.security.TestSecurityConfig;
import com.project.alfa.config.security.WithCustomMockUser;
import com.project.alfa.entities.Post;
import com.project.alfa.repositories.dto.SearchParam;
import com.project.alfa.services.AttachmentService;
import com.project.alfa.services.PostService;
import com.project.alfa.services.dto.AttachmentResponseDto;
import com.project.alfa.services.dto.PostRequestDto;
import com.project.alfa.services.dto.PostResponseDto;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestSecurityConfig.class)
@WebMvcTest(PostApiController.class)
class PostApiControllerTest {
    
    @MockBean
    PostService       postService;
    @MockBean
    AttachmentService attachmentService;
    @Autowired
    MockMvc           mockMvc;
    Gson                  gson;
    List<PostResponseDto> posts;
    
    @BeforeEach
    void setup() {
        gson = new Gson();
        
        posts = new ArrayList<>();
        for (int i = 1; i <= 10; i++)
            posts.add(new PostResponseDto(Post.builder()
                                              .id((long) i)
                                              .writerId((long) i)
                                              .title("Test title " + i)
                                              .content("Test content " + i)
                                              .noticeYn(false)
                                              .build()));
        
        when(postService.findAllPage(any(SearchParam.class), any(Pageable.class))).thenReturn(posts);
        when(postService.findAllPageByWriter(anyLong(), any(Pageable.class))).thenReturn(posts);
        when(postService.read(anyLong())).thenReturn(posts.get(0));
        when(postService.readWithCaching(anyLong(), anyString(), anyString())).thenReturn(posts.get(0));
        when(postService.create(any(PostRequestDto.class))).thenReturn(1L);
        doNothing().when(postService).addViewCountWithCaching(anyLong(), anyString(), anyString());
        doNothing().when(postService).update(any(PostRequestDto.class));
        doNothing().when(postService).delete(anyLong(), anyLong());
        
        when(attachmentService.saveAllFiles(anyLong(), anyList())).thenReturn(new ArrayList<>());
        doNothing().when(attachmentService).deleteAllFilesByIds(anyList(), anyLong());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("게시글 목록 페이지")
    void postsList() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(get("/api/posts")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .param("page", "0")
                                                        .param("size", "10"));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().json(gson.toJson(posts)))
               .andDo(print());
        
        verify(postService, times(1)).findAllPage(any(SearchParam.class), any(Pageable.class));
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("게시글 목록 페이지, 검색 조건 추가")
    void postsListWithSearch() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(get("/api/posts")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .param("page", "0")
                                                        .param("size", "10")
                                                        .param("condition", "title")
                                                        .param("keyword", "Test search"));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().json(gson.toJson(posts)))
               .andDo(print());
        
        verify(postService, times(1)).findAllPage(any(SearchParam.class), any(Pageable.class));
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("작성자 기준 게시글 목록 페이지")
    @WithCustomMockUser
    void postsListByWriter() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(get("/api/posts/writer")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().json(gson.toJson(posts)))
               .andDo(print());
        
        verify(postService, times(1)).findAllPageByWriter(anyLong(), any(Pageable.class));
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("게시글 상세 조회 페이지")
    void readPostPage() {
        //Given
        
        //When
        Map<String, Object> map = new HashMap<>();
        map.put("post", posts.get(0));
        map.put("files", new ArrayList<AttachmentResponseDto>());
        
        ResultActions actions = mockMvc.perform(get("/api/posts/{postId}", 1)
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().json(gson.toJson(map)))
               .andDo(print());
        
        verify(postService, times(1)).readWithCaching(anyLong(), anyString(), anyString());
        verify(attachmentService, times(1)).findAllFilesByPost(anyLong());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("게시글 작성 페이지")
    @WithMockUser
    void writePostPage() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(get("/api/posts/write")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().json(gson.toJson(new PostRequestDto())))
               .andDo(print());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("게시글 작성")
    @WithCustomMockUser
    void writePost() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/posts/write")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                                                        .param("writerId", "1")
                                                        .param("title", "Test title")
                                                        .param("content", "Test content")
                                                        .param("noticeYn", "false"));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().string("Post created successfully."))
               .andDo(print());
        
        verify(postService, times(1)).create(any(PostRequestDto.class));
        verify(attachmentService, times(1)).saveAllFiles(anyLong(), anyList());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("게시글 작성, @Valid 체크")
    @WithMockUser
    void writePost_validCheck() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/posts/write")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE));
        
        //Then
        actions.andExpect(status().isBadRequest())
               .andExpect(result ->
                                  assertThat(result.getResolvedException() instanceof MethodArgumentNotValidException))
               .andDo(print());
        
        verify(postService, never()).create(any(PostRequestDto.class));
        verify(attachmentService, never()).saveAllFiles(anyLong(), anyList());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("게시글 수정 페이지")
    @WithMockUser
    void updatePostPage() {
        //Given
        
        //When
        Map<String, Object> map = new HashMap<>();
        map.put("post", posts.get(0));
        map.put("form", new PostRequestDto());
        map.put("files", new ArrayList<AttachmentResponseDto>());
        
        ResultActions actions = mockMvc.perform(get("/api/posts/write/{postId}", 1)
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().json(gson.toJson(map)))
               .andDo(print());
        
        verify(postService, times(1)).read(anyLong());
        verify(attachmentService, times(1)).findAllFilesByPost(anyLong());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("게시글 수정")
    @WithCustomMockUser
    void updatePost() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/posts/write/{postId}", 1)
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                                                        .param("id", "1")
                                                        .param("writerId", "1")
                                                        .param("title", "Test title")
                                                        .param("content", "Test content")
                                                        .param("noticeYn", "false"));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().string("Post updated successfully."))
               .andDo(print());
        
        verify(postService, times(1)).update(any(PostRequestDto.class));
        verify(attachmentService, times(1)).deleteAllFilesByIds(anyList(), anyLong());
        verify(attachmentService, times(1)).saveAllFiles(anyLong(), anyList());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("게시글 수정, @Valid 체크")
    @WithMockUser
    void updatePost_validCheck() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/posts/write/{postId}", 1)
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE));
        
        //Then
        actions.andExpect(status().isBadRequest())
               .andExpect(result ->
                                  assertThat(result.getResolvedException() instanceof MethodArgumentNotValidException))
               .andDo(print());
        
        verify(postService, never()).update(any(PostRequestDto.class));
        verify(attachmentService, never()).deleteAllFilesByIds(anyList(), anyLong());
        verify(attachmentService, never()).saveAllFiles(anyLong(), anyList());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("게시글 삭제")
    @WithCustomMockUser
    void deletePost() {
        //Given
        
        //When
        ResultActions actions = mockMvc.perform(post("/api/posts/delete")
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                        .content(gson.toJson(new PostRequestDto(1L,
                                                                                                1L,
                                                                                                null,
                                                                                                null,
                                                                                                false))));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().string("Post deleted successfully."))
               .andDo(print());
        
        verify(postService, times(1)).delete(anyLong(), anyLong());
        verify(attachmentService, times(1)).deleteAllFilesByIds(anyList(), anyLong());
    }
    
}