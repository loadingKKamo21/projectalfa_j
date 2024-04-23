package com.project.alfa.controllers.api;

import com.google.gson.Gson;
import com.project.alfa.services.AttachmentService;
import com.project.alfa.services.dto.AttachmentResponseDto;
import com.project.alfa.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@RestController
@RequestMapping(value = "/api/posts/{postId}/attachments",
                consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AttachmentApiController {
    
    private final AttachmentService attachmentService;
    private final FileUtil          fileUtil;
    
    /**
     * GET: 첨부파일 목록 조회
     *
     * @param postId - 게시글 FK
     * @return
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> findAllFilesByPost(@PathVariable final Long postId) {
        return ResponseEntity.ok(new Gson().toJson(attachmentService.findAllFilesByPost(postId)));
    }
    
    /**
     * GET: 첨부파일 다운로드
     *
     * @param postId - 게시글 FK
     * @param fileId - 첨부파일 PK
     * @return
     */
    @GetMapping(value = "/{fileId}/download",
                produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> downloadFile(@PathVariable final Long postId, @PathVariable final Long fileId) {
        AttachmentResponseDto file     = attachmentService.findFileById(fileId);
        Resource              resource = fileUtil.readAttachmentFileAsResource(file);
        
        try {
            String      filename    = URLEncoder.encode(file.getOriginalFilename(), "UTF-8");
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
            httpHeaders.setContentLength(file.getFileSize());
            
            return new ResponseEntity<>(resource, httpHeaders, HttpStatus.OK);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Filename encoding failed: " + file.getOriginalFilename(), e);
        }
    }
    
}
