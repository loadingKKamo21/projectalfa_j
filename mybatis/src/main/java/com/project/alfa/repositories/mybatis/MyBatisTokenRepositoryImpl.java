package com.project.alfa.repositories.mybatis;

import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import java.util.Date;

@RequiredArgsConstructor
public class MyBatisTokenRepositoryImpl implements PersistentTokenRepository {
    
    private final PersistentTokenMapper persistentTokenMapper;
    
    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        persistentTokenMapper.createNewToken(token);
    }
    
    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        persistentTokenMapper.updateToken(series, tokenValue, lastUsed);
    }
    
    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        return persistentTokenMapper.getTokenForSeries(seriesId);
    }
    
    @Override
    public void removeUserTokens(String username) {
        persistentTokenMapper.removeUserTokens(username);
    }
    
}
