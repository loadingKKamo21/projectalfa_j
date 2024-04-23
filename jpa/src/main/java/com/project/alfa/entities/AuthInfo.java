package com.project.alfa.entities;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthInfo {
    
    private static final Long MAX_EXPIRE_TIME = 5L; //이메일 인증 만료 제한 시간
    
    @Column(nullable = false)
    private boolean       auth;                 //인증 여부
    private LocalDateTime authenticatedTime;    //계정 인증 시각
    private String        emailAuthToken;       //이메일 인증 토큰
    private LocalDateTime emailAuthExpireTime;  //이메일 인증 만료 시간
    @Column(updatable = false, nullable = true)
    private String        oAuthProvider;        //OAuth 2.0 Provider
    @Column(updatable = false, nullable = true)
    private String        oAuthProviderId;      //OAuth 2.0 Provider Id
    
    @Builder
    public AuthInfo(String emailAuthToken, String oAuthProvider, String oAuthProviderId) {
        this.auth = false;
        this.authenticatedTime = null;
        this.emailAuthToken = emailAuthToken;
        this.emailAuthExpireTime = emailAuthToken != null ?
                LocalDateTime.now().withNano(0).plusMinutes(MAX_EXPIRE_TIME) : null;
        this.oAuthProvider = oAuthProvider;
        this.oAuthProviderId = oAuthProviderId;
    }
    
    //==================== 인증 관련 메서드 ====================//
    
    /**
     * 인증 완료
     */
    public void authComplete() {
        auth = true;
        authenticatedTime = LocalDateTime.now();
    }
    
    /**
     * 이메일 인증 토큰 변경
     *
     * @param newEmailAuthToken - 새로운 이메일 인증 토큰
     */
    public void updateEmailAuthToken(final String newEmailAuthToken) {
        auth = false;
        authenticatedTime = null;
        emailAuthToken = newEmailAuthToken;
        emailAuthExpireTime = LocalDateTime.now().withNano(0).plusMinutes(MAX_EXPIRE_TIME);
    }
    
}
