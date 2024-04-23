package com.project.alfa.services.dto;

import com.project.alfa.entities.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponseDto {
    
    private final Long          id;
    private final String        writer;
    private final String        content;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastModifiedDate;
    
    public CommentResponseDto(final Comment comment) {
        id = comment.getId();
        writer = comment.getWriter().getNickname();
        content = comment.getContent();
        createdDate = comment.getCreatedDate();
        lastModifiedDate = comment.getLastModifiedDate();
    }
    
}
