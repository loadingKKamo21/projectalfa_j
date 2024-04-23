package com.project.alfa.services.dto;

import com.project.alfa.entities.Member;
import com.project.alfa.entities.Role;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MemberInfoResponseDto {
    
    private final Long          id;
    private final String        username;
    private final String        nickname;
    private final String        signature;
    private final Role          role;
    private final int           postCount;
    private final int           commentCount;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastModifiedDate;
    
    public MemberInfoResponseDto(final Member member) {
        id = member.getId();
        username = member.getUsername();
        nickname = member.getNickname();
        signature = member.getSignature();
        role = member.getRole();
        postCount = member.getPostIds().size();
        commentCount = member.getCommentIds().size();
        createdDate = member.getCreatedDate();
        lastModifiedDate = member.getLastModifiedDate();
    }
    
}
