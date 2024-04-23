package com.project.alfa.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

import static org.springframework.http.HttpMethod.GET;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final DataSource                   dataSource;
    private final UserDetailsService           userDetailsService;
    private final OAuth2UserService            oAuth2UserService;
    private final AuthenticationProvider       authenticationProvider;
    private final AuthenticationFailureHandler authenticationFailureHandler;
    
    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }
    
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().antMatchers("/css/**");
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //CSRF 비활성화
        http.csrf().disable();
        
        //URL
        http.authorizeRequests()
            .regexMatchers(GET, "/api/posts/(?:\\d+)?$", "/api/posts/\\d+/attachments$",
                           "/api/posts/\\d+/attachments/\\d+/download$").permitAll()
            .regexMatchers(GET,
                           "/api/posts\\??(&?(?:page=\\d+)?)(&?(?:size=\\d+)?)(&?(?:condition=" +
                           "(title|content|titleOrContent|writer)?)?)(&?(?:keyword=.*)?)$")
            .permitAll()
            .regexMatchers(GET, "/api/posts/\\d+/comments\\??(&?(?:page=\\d+)?)(&?(?:size=\\d+)?)$").permitAll()
            .mvcMatchers("/api/members", "/api/members/forgot-password").permitAll()
            .mvcMatchers("/api/members/**", "/logout", "/api/posts/**", "/api/posts/*/comments/**", "/api/comments/**")
            .authenticated()
            .anyRequest().permitAll();
        
        //커스텀 AuthenticationProvider
        http.authenticationProvider(authenticationProvider);
        
        //로그인 설정
        http.httpBasic()
            .and()
            .formLogin()
            .loginPage("/login")
            .loginProcessingUrl("/login-process")
            .defaultSuccessUrl("/", true)
            .failureHandler(authenticationFailureHandler);
        
        //remember-me
        http.rememberMe()
            .rememberMeParameter("remember-me")
            .tokenValiditySeconds(86400 * 14)
            .alwaysRemember(false)
            .userDetailsService(userDetailsService)
            .tokenRepository(tokenRepository());
        
        //로그아웃
        http.logout()
            .logoutUrl("/logout")
            .logoutSuccessUrl("/?logout")
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID", "remember-me");
        
        //OAuth 2.0 로그인
        http.oauth2Login()
            .loginPage("/login")
            .userInfoEndpoint()
            .userService(oAuth2UserService);
        
        return http.build();
    }
    
}
