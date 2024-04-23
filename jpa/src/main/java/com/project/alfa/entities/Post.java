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
@Table(name = "tbl_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;                                            //PK
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member writer;                                      //작성자
    
    @Column(nullable = false)
    @Size(min = 1, max = 100)
    private String title;                                       //제목
    
    @Lob
    private String content;                                     //내용
    
    @Column(nullable = false)
    private int viewCount;                                      //조회수
    
    @Column(nullable = false)
    private boolean noticeYn;                                   //공지 여부
    
    @Column(nullable = false)
    private boolean deleteYn;                                   //삭제 여부
    
    @OneToMany(mappedBy = "post")
    private List<Comment> comments = new ArrayList<>();         //댓글 목록
    
    @OneToMany(mappedBy = "post")
    private List<Attachment> attachments = new ArrayList<>();   //첨부파일 목록
    
    @Builder
    public Post(Member writer, String title, String content, boolean noticeYn) {
        setRelationshipWithMember(writer);
        this.title = title;
        this.content = content;
        this.viewCount = 0;
        this.noticeYn = writer.getRole() == Role.ADMIN && noticeYn;
        this.deleteYn = false;
    }
    
    //==================== 연관관계 메서드 ====================//
    
    private void setRelationshipWithMember(final Member member) {
        writer = member;
        member.getPosts().add(this);
    }
    
    //==================== 게시글 정보 수정 메서드 ====================//
    
    /**
     * 제목 변경
     *
     * @param newTitle - 새로운 제목
     */
    public void updateTitle(final String newTitle) {
        if ((newTitle != null || !newTitle.trim().isEmpty()) && !title.equals(newTitle))
            title = newTitle;
    }
    
    /**
     * 내용 변경
     *
     * @param newContent - 새로운 내용
     */
    public void updateContent(final String newContent) {
        if (content == null) {
            if (newContent != null && !newContent.trim().isEmpty())
                content = newContent;
        } else if (!content.equals(newContent))
            content = newContent;
    }
    
    /**
     * 공지 여부 변경
     *
     * @param newNoticeYn - 새로운 공지 여부
     */
    public void updateNoticeYn(final boolean newNoticeYn) {
        if (writer.getRole() == Role.ADMIN && noticeYn != newNoticeYn)
            noticeYn = newNoticeYn;
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
    
    //==================== 조회수 증가 메서드 ====================//
    
    /**
     * 조회수 증가
     */
    public void addViewCount() {
        viewCount += 1;
    }
    
    //==================== 댓글/첨부파일 개수 조회 메서드 ====================//
    
    /**
     * 댓글 개수 조회
     *
     * @return 댓글 개수
     */
    public int getCommentsCount() {
        return (int) comments.stream().filter(comment -> !comment.isDeleteYn()).count();
    }
    
    /**
     * 첨부파일 개수 조회
     *
     * @return 첨부파일 개수
     */
    public int getAttachmentsCount() {
        return (int) attachments.stream().filter(attachment -> !attachment.isDeleteYn()).count();
    }
    
}
