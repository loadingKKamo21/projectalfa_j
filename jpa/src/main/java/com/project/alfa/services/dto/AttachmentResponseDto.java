package com.project.alfa.services.dto;

import com.project.alfa.entities.Attachment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AttachmentResponseDto {
    
    private final Long          id;
    private final Long          postId;
    private final String        originalFilename;
    private final String        storeFilename;
    private final Long          fileSize;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastModifiedDate;
    
    public AttachmentResponseDto(Attachment attachment) {
        id = attachment.getId();
        postId = attachment.getPost().getId();
        originalFilename = attachment.getOriginalFilename();
        storeFilename = attachment.getStoreFilename();
        fileSize = attachment.getFileSize();
        createdDate = attachment.getCreatedDate();
        lastModifiedDate = attachment.getLastModifiedDate();
    }
    
}
