package com.project.alfa.config.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCustomSecurityContextFactory.class)
public @interface WithCustomMockUser {
    
    long id() default 1L;
    
    String username() default "user1@mail.com";
    
    String password() default "Password1!@";
    
    boolean auth() default true;
    
    String nickname() default "user1";
    
    String role() default "ROLE_USER";
    
}
