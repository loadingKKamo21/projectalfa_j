package com.project.alfa.entities;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "tbl_post_attachments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attachment extends UploadFile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_attachment_id")
    private Long id;            //PK
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;          //게시글
    
    @Column(nullable = false)
    private boolean deleteYn;   //삭제 여부
    
    @Builder
    public Attachment(Post post, String originalFilename, String storeFilename, String storeFilePath, Long fileSize) {
        super(originalFilename, storeFilename, storeFilePath, fileSize);
        setRelationshipWithPost(post);
    }
    
    //==================== 연관관계 메서드 ====================//
    
    private void setRelationshipWithPost(final Post post) {
        this.post = post;
        post.getAttachments().add(this);
    }
    
    //==================== 첨부파일 정보 수정 메서드 ====================//
    
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
