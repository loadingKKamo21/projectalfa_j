package com.project.alfa.services.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequestDto {
    
    private Long id;        //PK
    private Long writerId;  //작성자 FK
    private Long postId;    //게시글 FK
    
    @NotBlank(message = "내용을 입력하세요.")
    private String content; //내용
    
}
