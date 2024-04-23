package com.project.alfa.security;

import com.project.alfa.entities.Member;
import com.project.alfa.repositories.v1.MemberRepositoryV1;
import com.project.alfa.repositories.v2.MemberRepositoryV2;
import com.project.alfa.repositories.v3.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final MemberRepositoryV1 memberRepository;
    //private final MemberRepositoryV2 memberRepository;
    //private final MemberRepositoryV3 memberRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username.toLowerCase(), false)
                                        .orElseThrow(() -> new UsernameNotFoundException(
                                                "Could not found 'Member' by username: " + username.toLowerCase()));
        return new CustomUserDetails(member.getId(),
                                     member.getUsername(),
                                     member.getPassword(),
                                     member.getAuthInfo().isAuth(),
                                     member.getNickname(),
                                     member.getRole().getValue());
    }
    
}
