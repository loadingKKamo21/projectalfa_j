package com.project.alfa.entities;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class UploadFile {
    
    String        originalFilename; //원본 파일명
    String        storeFilename;    //저장 파일명
    String        storeFilePath;    //저장 경로
    Long          fileSize;         //파일 크기
    LocalDateTime createdDate;      //생성일시
    LocalDateTime lastModifiedDate; //최종 수정일시
    
    protected UploadFile(String originalFilename, String storeFilename, String storeFilePath, Long fileSize) {
        this.originalFilename = originalFilename;
        this.storeFilename = storeFilename;
        this.storeFilePath = storeFilePath;
        this.fileSize = fileSize;
    }
    
}
