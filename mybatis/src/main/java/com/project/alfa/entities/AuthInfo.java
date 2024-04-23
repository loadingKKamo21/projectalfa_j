package com.project.alfa.entities;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthInfo {
    
    private boolean       auth;                 //인증 여부
    private LocalDateTime authenticatedTime;    //계정 인증 시각
    private String        emailAuthToken;       //이메일 인증 토큰
    private LocalDateTime emailAuthExpireTime;  //이메일 인증 만료 시간
    private String        oAuthProvider;        //OAuth 2.0 Provider
    private String        oAuthProviderId;      //OAuth 2.0 Provider Id
    
    @Builder
    public AuthInfo(boolean auth, String emailAuthToken, LocalDateTime emailAuthExpireTime, String oAuthProvider,
                    String oAuthProviderId) {
        this.auth = auth;
        this.emailAuthToken = emailAuthToken;
        this.emailAuthExpireTime = emailAuthExpireTime;
        this.oAuthProvider = oAuthProvider;
        this.oAuthProviderId = oAuthProviderId;
    }
    
}
