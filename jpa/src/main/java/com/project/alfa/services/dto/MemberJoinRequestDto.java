package com.project.alfa.services.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberJoinRequestDto {
    
    @NotBlank
    @Email
    @Pattern(message = "올바른 형태의 이메일 주소를 입력하세요.", regexp = RegEx.EMAIL_REGEX)
    private String username;
    
    @NotBlank
    @Pattern(message = "영문 대/소문자, 숫자, 특수문자 각 최소 1개 이상 포함, 8~32자", regexp = RegEx.PASSWORD_REGEX)
    private String password;
    
    @NotBlank
    private String repeatPassword;
    
    @NotBlank
    @Pattern(message = "영문, 숫자, 한글, 1~20자", regexp = RegEx.NICKNAME_REGEX)
    private String nickname;
    
}
