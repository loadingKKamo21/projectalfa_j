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
    private final int           commentCount;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastModifiedDate;
    
    public PostResponseDto(final Post post) {
        id = post.getId();
        writer = post.getNickname();
        title = post.getTitle();
        content = post.getContent();
        viewCount = post.getViewCount();
        noticeYn = post.isNoticeYn();
        commentCount = post.getCommentIds().size();
        createdDate = post.getCreatedDate();
        lastModifiedDate = post.getLastModifiedDate();
    }
    
}
