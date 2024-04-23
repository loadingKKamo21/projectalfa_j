package com.project.alfa.security.oauth2;

import com.project.alfa.entities.AuthInfo;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Role;
import com.project.alfa.error.exception.EntityNotFoundException;
import com.project.alfa.repositories.v1.MemberRepositoryV1;
import com.project.alfa.security.CustomUserDetails;
import com.project.alfa.security.oauth2.provider.GoogleUserInfo;
import com.project.alfa.security.oauth2.provider.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.project.alfa.utils.RandomGenerator.randomPassword;
import static com.project.alfa.utils.RandomGenerator.randomString;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private final MemberRepositoryV1 memberRepository;
    //private final MemberRepositoryV2 memberRepository;
    //private final MemberRepositoryV3 memberRepository;
    private final PasswordEncoder    passwordEncoder;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String         clientName     = userRequest.getClientRegistration().getClientName();
        OAuth2UserInfo oAuth2UserInfo = null;
        
        //OAuth 2.0 Provider 구분
        switch (clientName) {
            case "Google":
                oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
                break;
            default:
                break;
        }
        
        String username = oAuth2UserInfo.getProvider() + "_" + oAuth2UserInfo.getProviderId();
        
        Member member = null;
        if (!memberRepository.existsByUsername(username, false)) {
            String nickname;
            do {
                nickname = oAuth2User.getAttribute("name") + "_" + randomString(10);
            } while (memberRepository.existsByNickname(nickname));
            
            member = Member.builder()
                           .username(username.toLowerCase())
                           .password(passwordEncoder.encode(randomPassword(20)))
                           .authInfo(AuthInfo.builder()
                                             .oAuthProvider(oAuth2UserInfo.getProvider())
                                             .oAuthProviderId(oAuth2UserInfo.getProviderId())
                                             .build())
                           .nickname(nickname)
                           .role(Role.USER)
                           .build();
            
            memberRepository.save(member);
            memberRepository.authenticateOAuth(member.getUsername(),
                                               member.getAuthInfo().getOAuthProvider(),
                                               member.getAuthInfo().getOAuthProviderId()).get().authenticate();
        } else
            member = memberRepository.findByUsername(username.toLowerCase(), false)
                                     .orElseThrow(() -> new EntityNotFoundException(
                                             "Could not found 'Member' by username: " + username));
        
        return new CustomUserDetails(member.getId(),
                                     member.getUsername(),
                                     member.getPassword(),
                                     member.getAuthInfo().isAuth(),
                                     member.getNickname(),
                                     member.getRole().getValue(),
                                     oAuth2User.getAttributes());
    }
    
}
