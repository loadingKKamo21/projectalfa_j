package com.project.alfa.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String message;
        
        if (exception instanceof BadCredentialsException)
            message = "Invalid username or password";
        else if (exception instanceof UsernameNotFoundException)
            message = "Account do not exist";
        else if (exception instanceof LockedException)
            message = "Account do not complete authentication";
        else if (exception instanceof InternalAuthenticationServiceException)
            message = "The request could not be processed due to an internal error";
        else if (exception instanceof AuthenticationCredentialsNotFoundException)
            message = "Authentication request denied";
        else
            message = "Login failed for unknown reason";

//        response.sendRedirect("/login?error=" + URLEncoder.encode(message, "UTF-8"));
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(message);
    }
    
}
