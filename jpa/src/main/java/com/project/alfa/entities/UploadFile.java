package com.project.alfa.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class UploadFile extends BaseTimeEntity {
    
    @Column(nullable = false)
    private String originalFilename;    //원본 파일명
    
    @Column(nullable = false)
    private String storeFilename;       //저장 파일명
    
    @Column(nullable = false)
    private String storeFilePath;       //저장 경로
    
    @Column(nullable = false)
    private Long fileSize;              //파일 크기
    
}
