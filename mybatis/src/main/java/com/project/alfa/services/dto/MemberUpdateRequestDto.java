package com.project.alfa.services.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberUpdateRequestDto {
    
    @NotNull
    private Long id;
    
    @NotBlank
    @Pattern(message = "영문 대/소문자, 숫자, 특수문자 각 최소 1개 이상 포함, 8~32자", regexp = RegEx.PASSWORD_REGEX)
    private String password;
    
    @Pattern(message = "영문, 숫자, 한글, 1~20자", regexp = RegEx.NICKNAME_REGEX)
    private String nickname;
    
    @Pattern(message = "최대 100자", regexp = RegEx.SIGNATURE_REGEX)
    private String signature;
    
    @Pattern(message = "영문 대/소문자, 숫자, 특수문자 각 최소 1개 이상 포함, 8~32자", regexp = RegEx.PASSWORD_REGEX)
    private String newPassword;
    private String repeatNewPassword;
    
}
