package com.project.alfa.config.security;

import com.project.alfa.entities.Role;
import com.project.alfa.security.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithCustomSecurityContextFactory implements WithSecurityContextFactory<WithCustomMockUser> {
    
    @Override
    public SecurityContext createSecurityContext(WithCustomMockUser annotation) {
        CustomUserDetails userDetails = new CustomUserDetails(annotation.id(),
                                                              annotation.username(),
                                                              annotation.password(),
                                                              annotation.auth(),
                                                              annotation.nickname(),
                                                              findByValue(annotation.role()).getValue());
        
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(userDetails,
                                                                                  annotation.password(),
                                                                                  userDetails.getAuthorities()));
        
        return securityContext;
    }
    
    private Role findByValue(final String value) {
        Role role = null;
        for (Role roleValue : Role.values())
            if (roleValue.getValue().contains(value)) {
                role = roleValue;
                break;
            }
        return role;
    }
    
}
