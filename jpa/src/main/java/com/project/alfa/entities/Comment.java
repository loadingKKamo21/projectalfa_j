package com.project.alfa.entities;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name = "tbl_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;            //PK
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member writer;      //작성자
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;          //게시글
    
    @Column(nullable = false)
    @Size(min = 1, max = 100)
    private String content;     //내용
    
    @Column(nullable = false)
    private boolean deleteYn;   //삭제 여부
    
    @Builder
    public Comment(Member writer, Post post, String content) {
        setRelationshipWithMember(writer);
        setRelationshipWithPost(post);
        this.content = content;
        this.deleteYn = false;
    }
    
    //==================== 연관관계 메서드 ====================//
    
    private void setRelationshipWithMember(final Member member) {
        writer = member;
        member.getComments().add(this);
    }
    
    private void setRelationshipWithPost(final Post post) {
        this.post = post;
        post.getComments().add(this);
    }
    
    //==================== 댓글 정보 수정 메서드 ====================//
    
    /**
     * 내용 변경
     *
     * @param newContent - 새로운 내용
     */
    public void updateContent(final String newContent) {
        if ((newContent != null || !newContent.trim().isEmpty()) && !content.equals(newContent))
            content = newContent;
    }
    
    /**
     * 삭제 여부 변경
     *
     * @param newDeleteYn - 새로운 삭제 여부
     */
    public void isDelete(final boolean newDeleteYn) {
        if (deleteYn != newDeleteYn)
            deleteYn = newDeleteYn;
    }
    
}
