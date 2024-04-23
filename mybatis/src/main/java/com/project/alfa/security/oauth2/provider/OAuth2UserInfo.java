package com.project.alfa.security.oauth2.provider;

public interface OAuth2UserInfo {
    
    String getProvider();
    
    String getProviderId();
    
    String getEmail();
    
}
