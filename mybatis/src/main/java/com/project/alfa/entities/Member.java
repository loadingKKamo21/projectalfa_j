package com.project.alfa.entities;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Member {
    
    private Long          id;                              //PK
    @Size(min = 5)
    private String        username;                        //아이디(이메일)
    private String        password;                        //비밀번호
    private AuthInfo      authInfo;                        //인증 정보
    @Size(min = 1, max = 20)
    private String        nickname;                        //닉네임
    @Size(max = 100)
    private String        signature;                       //서명
    private Role          role;                            //계정 유형
    private LocalDateTime createdDate;                     //생성일시
    private LocalDateTime lastModifiedDate;                //최종 수정일시
    private List<Long>    postIds    = new ArrayList<>();  //작성 게시글 FK 목록
    private List<Long>    commentIds = new ArrayList<>();  //작성 댓글 FK 목록
    private boolean       deleteYn;                        //탈퇴 여부
    
    @Builder
    public Member(Long id, String username, String password, AuthInfo authInfo, String nickname, String signature,
                  Role role) {
        this.id = id;
        this.username = username == null ? null : username.toLowerCase();
        this.password = password;
        this.authInfo = authInfo;
        this.nickname = nickname;
        this.signature = signature;
        this.role = role;
    }
    
}
