package com.project.alfa.repositories.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;

import java.util.Date;

@Mapper
public interface PersistentTokenMapper {
    
    void createNewToken(PersistentRememberMeToken token);
    
    void updateToken(@Param("series") String series,
                     @Param("tokenValue") String tokenValue,
                     @Param("lastUsed") Date lastUsed);
    
    PersistentRememberMeToken getTokenForSeries(String series);
    
    void removeUserTokens(String username);
    
}
