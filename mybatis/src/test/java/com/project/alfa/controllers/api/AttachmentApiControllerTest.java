package com.project.alfa.controllers.api;

import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.project.alfa.config.security.TestSecurityConfig;
import com.project.alfa.entities.Attachment;
import com.project.alfa.services.AttachmentService;
import com.project.alfa.services.dto.AttachmentResponseDto;
import com.project.alfa.utils.FileUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(AttachmentApiController.class)
class AttachmentApiControllerTest {
    
    @MockBean
    AttachmentService attachmentService;
    @MockBean
    FileUtil          fileUtil;
    @Autowired
    MockMvc           mockMvc;
    List<AttachmentResponseDto> attachments;
    Gson                        gson;
    
    @BeforeEach
    void setup() throws IOException {
        gson = new Gson();
        
        attachments = new ArrayList<>();
        for (int i = 1; i <= 10; i++)
            attachments.add(new AttachmentResponseDto(Attachment.builder()
                                                                .id((long) i)
                                                                .postId((long) i)
                                                                .originalFilename("Test originalFilename " + i)
                                                                .storeFilename("Test storeFilename " + i)
                                                                .storeFilePath("Test storeFilePath " + i)
                                                                .fileSize(1000L)
                                                                .build()));
        
        UrlResource          resource    = mock(UrlResource.class);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(UUID.randomUUID().toString().getBytes());
        
        when(attachmentService.findAllFilesByPost(anyLong())).thenReturn(attachments);
        when(attachmentService.findFileById(anyLong())).thenReturn(attachments.get(0));
        when(fileUtil.readAttachmentFileAsResource(any(AttachmentResponseDto.class))).thenReturn(resource);
        when(resource.getInputStream()).thenReturn(inputStream);
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("첨부파일 목록 조회")
    void findAllFilesByPost() {
        //Given
        Random random = new Random();
        Long   postId;
        do {
            postId = random.nextLong();
        } while (postId < 0);
        
        //When
        ResultActions actions = mockMvc.perform(get("/api/posts/{postId}/attachments", postId)
                                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE));
        
        //Then
        actions.andExpect(status().isOk())
               .andExpect(content().json(gson.toJson(attachments)))
               .andDo(print());
        
        verify(attachmentService, times(1)).findAllFilesByPost(anyLong());
    }
    
    @SneakyThrows(Exception.class)
    @Test
    @DisplayName("첨부파일 다운로드")
    void downloadFile() {
        //Given
        Random random = new Random();
        Long   postId;
        Long   fileId;
        do {
            postId = random.nextLong();
            fileId = random.nextLong();
        } while (postId < 0 || fileId < 0);
        
        //When
        ResultActions actions = mockMvc.perform(get("/api/posts/{postId}/attachments/{fileId}/download", postId, fileId)
                                                        .accept(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                                                        .contentType(MediaType.APPLICATION_JSON_VALUE));
        
        //Then
        String filename = URLEncoder.encode(attachments.get(0).getOriginalFilename(), "UTF-8");
        
        actions.andExpect(status().isOk())
               .andExpect(content().contentType("application/octet-stream;charset=UTF-8"))
               .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                                          "attachment; filename=\"" + filename + "\""))
               .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, attachments.get(0).getFileSize()))
               .andDo(print());
        
        verify(attachmentService, times(1)).findFileById(anyLong());
        verify(fileUtil, times(1)).readAttachmentFileAsResource(any(AttachmentResponseDto.class));
    }
    
}