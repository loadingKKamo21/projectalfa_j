package com.project.alfa.entities;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;                                    //PK
    
    @Column(nullable = false, updatable = false, unique = true)
    @Size(min = 5)
    private String username;                            //아이디(이메일)
    
    @Column(nullable = false)
    private String password;                            //비밀번호
    
    @Embedded
    private AuthInfo authInfo;                          //인증 정보
    
    @Column(nullable = false, unique = true)
    @Size(min = 1, max = 20)
    private String nickname;                            //닉네임
    
    @Size(max = 100)
    private String signature;                           //서명
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;                                  //계정 유형
    
    @Column(nullable = false)
    private boolean deleteYn;                           //탈퇴 여부
    
    @OneToMany(mappedBy = "writer")
    private List<Post> posts = new ArrayList<>();       //작성 게시글 목록
    
    @OneToMany(mappedBy = "writer")
    private List<Comment> comments = new ArrayList<>(); //작성 댓글 목록
    
    @Builder
    public Member(String username, String password, AuthInfo authInfo, String nickname, Role role) {
        this.username = username.toLowerCase();
        this.password = password;
        this.authInfo = authInfo;
        this.nickname = nickname;
        this.role = role == null ? Role.USER : role;
        this.deleteYn = false;
    }
    
    @PrePersist
    @PreUpdate
    private void ensureUsernameLowercase() {
        if (username != null && !username.trim().isEmpty())
            username = username.toLowerCase();
    }
    
    //==================== 계정 정보 수정 메서드 ====================//
    
    /**
     * 비밀번호 변경
     *
     * @param newPassword - 새로운 비밀번호
     */
    public void updatePassword(final String newPassword) {
        if ((newPassword != null || !newPassword.trim().isEmpty()) && !password.equals(newPassword))
            password = newPassword;
    }
    
    /**
     * 계정 인증
     */
    public void authenticate() {
        authInfo.authComplete();
    }
    
    /**
     * 이메일 인증 토큰 업데이트
     *
     * @param newEmailAuthToken - 새로운 이메일 인증 토큰
     */
    public void updateEmailAuthToken(final String newEmailAuthToken) {
        if ((newEmailAuthToken != null || !newEmailAuthToken.trim().isEmpty()) &&
            !authInfo.getEmailAuthToken().equals(newEmailAuthToken))
            authInfo.updateEmailAuthToken(newEmailAuthToken);
    }
    
    /**
     * 닉네임 변경
     *
     * @param newNickname - 새로운 닉네임
     */
    public void updateNickname(final String newNickname) {
        if ((newNickname != null || !newNickname.trim().isEmpty()) && !nickname.equals(newNickname))
            nickname = newNickname;
    }
    
    /**
     * 서명 변경
     *
     * @param newSignature - 새로운 서명
     */
    public void updateSignature(final String newSignature) {
        if (signature == null) {
            if (newSignature != null && !newSignature.trim().isEmpty())
                signature = newSignature;
        } else if (!signature.equals(newSignature))
            signature = newSignature;
    }
    
    /**
     * 계정 유형 변경
     *
     * @param newRole - 새로운 계정 유형
     */
    public void updateRole(final Role newRole) {
        if (newRole != null && role != newRole)
            role = newRole;
    }
    
    /**
     * 탈퇴 여부 변경
     *
     * @param newDeleteYn - 새로운 탈퇴 여부
     */
    public void isDelete(final boolean newDeleteYn) {
        if (deleteYn != newDeleteYn)
            deleteYn = newDeleteYn;
    }
    
    //==================== 작성 게시글/댓글 개수 조회 메서드 ====================//
    
    /**
     * 작성 게시글 개수 조회
     *
     * @return 작성 게시글 개수
     */
    public int getPostsCount() {
        return (int) posts.stream().filter(post -> !post.isDeleteYn()).count();
    }
    
    /**
     * 작성 댓글 개수 조회
     *
     * @return 작성 댓글 개수
     */
    public int getCommentsCount() {
        return (int) comments.stream().filter(comment -> !comment.isDeleteYn()).count();
    }
    
}
