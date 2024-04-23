package com.project.alfa.services.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PostRequestDto {
    
    private Long id;                                       //PK
    private Long writerId;                                 //작성자 FK
    
    @NotBlank(message = "제목을 입력하세요.")
    private String title;                                  //제목
    
    @NotBlank(message = "내용을 입력하세요.")
    private String content;                                //내용
    
    private boolean noticeYn;                              //공지 여부
    
    private List<MultipartFile> files = new ArrayList<>(); //첨부파일 목록
    
    private List<Long> removeFileIds = new ArrayList<>();  //삭제할 첨부파일 PK 목록
    
    public PostRequestDto(Long id, Long writerId, String title, String content, boolean noticeYn) {
        this.id = id;
        this.writerId = writerId;
        this.title = title;
        this.content = content;
        this.noticeYn = noticeYn;
    }
    
}
