package com.project.alfa.entities;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Attachment extends UploadFile {
    
    private Long id;        //PK
    private Long postId;    //게시글 FK
    boolean deleteYn;       //삭제 여부
    
    @Builder
    public Attachment(Long id, Long postId,
                      String originalFilename, String storeFilename, String storeFilePath, Long fileSize) {
        super(originalFilename, storeFilename, storeFilePath, fileSize);
        this.id = id;
        this.postId = postId;
    }
    
}
