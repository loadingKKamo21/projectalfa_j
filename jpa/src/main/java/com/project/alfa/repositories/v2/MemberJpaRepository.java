package com.project.alfa.repositories.v2;

import com.project.alfa.entities.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    
    Optional<Member> findByIdAndDeleteYn(Long id, boolean deleteYn);
    
    Optional<Member> findByUsername(String username);
    
    Optional<Member> findByUsernameAndDeleteYn(String username, boolean deleteYn);
    
    List<Member> findAllByAuthInfo_Auth(boolean auth);
    
    List<Member> findAllByDeleteYn(boolean deleteYn);
    
    Optional<Member> findByUsernameAndAuthInfo_EmailAuthTokenAndAuthInfo_EmailAuthExpireTimeGreaterThanEqualAndDeleteYnFalse(
            String username, String emailAuthToken, LocalDateTime authenticatedTime);
    
    Optional<Member> findByUsernameAndAuthInfo_oAuthProviderAndAuthInfo_oAuthProviderIdAndDeleteYnFalse(String username,
                                                                                                        String oAuthProvider,
                                                                                                        String oAuthProviderId);
    
    boolean existsByUsername(String username);
    
    boolean existsByUsernameAndDeleteYn(String username, boolean deleteYn);
    
    boolean existsByNickname(String nickname);
    
    boolean existsByNicknameAndDeleteYn(String nickname, boolean deleteYn);
    
}
