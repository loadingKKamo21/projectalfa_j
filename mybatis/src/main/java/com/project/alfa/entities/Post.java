package com.project.alfa.entities;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Post {
    
    private Long          id;                                   //PK
    private Long          writerId;                             //작성자 FK
    private String        nickname;                             //닉네임
    @Size(min = 1, max = 100)
    private String        title;                                //제목
    private String        content;                              //내용
    private int           viewCount;                            //조회수
    private boolean       noticeYn;                             //공지 여부
    private LocalDateTime createdDate;                          //생성일시
    private LocalDateTime lastModifiedDate;                     //최종 수정일시
    private List<Long>    commentIds    = new ArrayList<>();    //작성 댓글 FK 목록
    private List<Long>    attachmentIds = new ArrayList<>();    //첨부파일 FK 목록
    private boolean       deleteYn;                             //삭제 여부
    
    @Builder
    public Post(Long id, Long writerId, String title, String content, boolean noticeYn) {
        this.id = id;
        this.writerId = writerId;
        this.title = title;
        this.content = content;
        this.noticeYn = noticeYn;
    }
    
}
