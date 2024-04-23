package com.project.alfa.entities;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Comment {
    
    private Long          id;               //PK
    private Long          writerId;         //작성자 FK
    private Long          postId;           //게시글 FK
    private String        nickname;         //닉네임
    @Size(min = 1, max = 100)
    private String        content;          //내용
    private LocalDateTime createdDate;      //생성일시
    private LocalDateTime lastModifiedDate; //최종 수정일시
    private boolean       deleteYn;         //삭제 여부
    
    @Builder
    public Comment(Long id, Long writerId, Long postId, String content) {
        this.id = id;
        this.writerId = writerId;
        this.postId = postId;
        this.content = content;
    }
    
}
