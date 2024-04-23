package com.project.alfa.services.dto;

public class RegEx {
    
    //Regular Expression by RFC 5322 for Email Validation
    static final String EMAIL_REGEX     = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
    //비밀번호: 영문 대/소문자, 숫자, 특수문자 각 최소 1개 이상 포함, 8~32자
    static final String PASSWORD_REGEX  = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[`~!@#$%^&*()-_=+[{]}|;:'\",<.>/?]).{8,32}$";
    //닉네임: 영문, 숫자, 한글 사용, 1~20자
    static final String NICKNAME_REGEX  = "^[a-zA-Z0-9가-힣]{1,20}$";
    static final String SIGNATURE_REGEX = "^.{0,100}$";
    
}
