package com.project.alfa.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.project.alfa.repositories.mybatis.MyBatisTokenRepositoryImpl;
import com.project.alfa.repositories.mybatis.PersistentTokenMapper;
import com.project.alfa.security.jwt.entrypoint.JwtAuthenticationEntryPoint;
import com.project.alfa.security.jwt.filter.JwtAuthenticationFilter;
import com.project.alfa.security.jwt.filter.JwtRequestFilter;
import com.project.alfa.services.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

import static org.springframework.http.HttpMethod.GET;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final UserDetailsService           userDetailsService;
    private final OAuth2UserService            oAuth2UserService;
    private final AuthenticationManager        authenticationManager;
    private final AuthenticationProvider       authenticationProvider;
    private final AuthenticationFailureHandler authenticationFailureHandler;
    private final PersistentTokenMapper        persistentTokenMapper;
    private final JwtAuthenticationEntryPoint  jwtAuthenticationEntryPoint;
    private final JwtService                   jwtService;
    
    @Bean
    public PersistentTokenRepository tokenRepository() {
        return new MyBatisTokenRepositoryImpl(persistentTokenMapper);
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
            .regexMatchers(GET, "/api/posts/(?:\\d+)?$", "/api/posts/\\d+/attachments$", "/api/posts/\\d+/attachments/\\d+/download$").permitAll()
            .regexMatchers(GET, "/api/posts\\??(&?(?:page=\\d+)?)(&?(?:size=\\d+)?)(&?(?:condition=(title|content|titleOrContent|writer)?)?)(&?(?:keyword=.*)?)$").permitAll()
            .regexMatchers(GET, "/api/posts/\\d+/comments\\??(&?(?:page=\\d+)?)(&?(?:size=\\d+)?)$").permitAll()
            .mvcMatchers("/api/members", "/api/members/forgot-password").permitAll()
            .mvcMatchers("/api/members/**", "/logout", "/api/posts/**", "/api/posts/*/comments/**", "/api/comments/**", "/api/auth/refresh").authenticated()
            .anyRequest().permitAll();
        
        //커스텀 AuthenticationProvider
        http.authenticationProvider(authenticationProvider);
        
        //HTTP Form 로그인 설정
//        http.httpBasic()
//            .and()
//            .formLogin()
//            .loginPage("/login")
//            .loginProcessingUrl("/login-process")
//            .defaultSuccessUrl("/", true)
//            .failureHandler(authenticationFailureHandler);
        http.httpBasic().disable().formLogin().disable();
        
        //세션 비활성화
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        
        //JWT 인증 필터
        JwtRequestFilter jwtRequestFilter = new JwtRequestFilter(userDetailsService, jwtService);
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager,
                                                                                      jwtService);
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        //JWT 인증 엔트리포인트
        http.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint);
        
        //remember-me 설정
//        http.rememberMe()
//            .rememberMeParameter("remember-me")
//            .tokenValiditySeconds(86400 * 14)
//            .alwaysRemember(false)
//            .userDetailsService(userDetailsService)
//            .tokenRepository(tokenRepository());
        http.rememberMe().disable();
        
        //로그아웃
        http.logout()
            .logoutUrl("/logout")
            .logoutSuccessUrl("/?logout")
            .logoutSuccessHandler((request, response, authentication) -> {
                if (request.getSession() != null)
                    request.getSession().invalidate();
            
                SecurityContextHolder.clearContext();
            
                String refreshToken = getRefreshToken(request);
                if (refreshToken != null) {
                    jwtService.deleteRefreshToken(refreshToken);
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("Logout successful.");
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid Refresh Token.");
                }
                response.getWriter().flush();

//                Cookie jSessionIdCookie = new Cookie("JSESSIONID", null);
//                jSessionIdCookie.setPath("/");
//                jSessionIdCookie.setHttpOnly(true);
//                jSessionIdCookie.setMaxAge(0);
//                response.addCookie(jSessionIdCookie);
//
//                Cookie rememberMeCookie = new Cookie("remember-me", null);
//                rememberMeCookie.setPath("/");
//                rememberMeCookie.setHttpOnly(true);
//                rememberMeCookie.setMaxAge(0);
//                response.addCookie(rememberMeCookie);
            })
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
    
    /**
     * JWT Refresh 토큰 추출
     *
     * @param request
     * @return JWT Refresh 토큰
     */
    private String getRefreshToken(HttpServletRequest request) {
        //1. 쿠키에서 RefreshToken 추출
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies)
                if (cookie.getName().equals("refreshToken"))
                    return cookie.getValue();
        
        //2. 헤더에서 RefreshToken 추출
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Refresh "))
            return header.substring(8);
        
        //3. JSON에서 RefreshToken 추출
        try {
            StringBuilder sb   = new StringBuilder();
            Gson          gson = new Gson();
            
            BufferedReader reader = request.getReader();
            String         line;
            while ((line = reader.readLine()) != null)
                sb.append(line);
            Map<String, String> body = gson.fromJson(sb.toString(), new TypeToken<Map<String, String>>() {}.getType());
            if (body != null)
                return body.get("refreshToken");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        
        return null;
    }
    
}
