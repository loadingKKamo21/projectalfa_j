package com.project.alfa.config.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.http.HttpMethod.GET;

@TestConfiguration("SecurityConfig")
public class TestSecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        
        http.authorizeRequests()
            .regexMatchers(GET, "/api/posts/(?:\\d+)?$", "/api/posts/\\d+/attachments$", "/api/posts/\\d+/attachments/\\d+/download$").permitAll()
            .regexMatchers(GET, "/api/posts\\??(&?(?:page=\\d+)?)(&?(?:size=\\d+)?)(&?(?:condition=(title|content|titleOrContent|writer)?)?)(&?(?:keyword=.*)?)$").permitAll()
            .regexMatchers(GET, "/api/posts/\\d+/comments\\??(&?(?:page=\\d+)?)(&?(?:size=\\d+)?)$").permitAll()
            .mvcMatchers("/api/members", "/api/members/forgot-password").permitAll()
            .mvcMatchers("/api/members/**", "/logout", "/api/posts/**", "/api/posts/*/comments/**", "/api/comments/**").authenticated()
            .anyRequest().permitAll();
        
        return http.build();
    }
    
}
