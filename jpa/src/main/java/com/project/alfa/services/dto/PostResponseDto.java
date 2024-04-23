package com.project.alfa.services.dto;

import com.project.alfa.entities.Post;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
public class PostResponseDto implements Serializable {
    
    private final Long          id;
    private final String        writer;
    private final String        title;
    private final String        content;
    private final int           viewCount;
    private final boolean       noticeYn;
    private final int           commentsCount;
    private final int           attachmentsCount;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastModifiedDate;
    
    public PostResponseDto(final Post post) {
        id = post.getId();
        writer = post.getWriter().getNickname();
        title = post.getTitle();
        content = post.getContent();
        viewCount = post.getViewCount();
        noticeYn = post.isNoticeYn();
        commentsCount = post.getCommentsCount();
        attachmentsCount = post.getAttachmentsCount();
        createdDate = post.getCreatedDate();
        lastModifiedDate = post.getLastModifiedDate();
    }
    
}
