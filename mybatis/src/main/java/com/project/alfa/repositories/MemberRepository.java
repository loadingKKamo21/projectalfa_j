package com.project.alfa.repositories;

import com.project.alfa.entities.Member;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    
    Member save(Member member);
    
    Optional<Member> findById(Long id);
    
    Optional<Member> findById(Long id, boolean deleteYn);
    
    Optional<Member> findByUsername(String username);
    
    Optional<Member> findByUsername(String username, boolean deleteYn);
    
    List<Member> findAll();
    
    List<Member> findAllByAuth(boolean auth);
    
    List<Member> findAllByDeleteYn(boolean deleteYn);
    
    void authenticateEmail(String username, String emailAuthToken, LocalDateTime authenticatedTime);
    
    void authenticateOAuth(String username, String provider, String providerId, LocalDateTime authenticatedTime);
    
    void update(Member param);
    
    boolean existsById(Long id);
    
    boolean existsById(Long id, boolean deleteYn);
    
    boolean existsByUsername(String username);
    
    boolean existsByUsername(String username, boolean deleteYn);
    
    boolean existsByNickname(String nickname);
    
    boolean existsByNickname(String nickname, boolean deleteYn);
    
    void deleteById(Long id);
    
    void permanentlyDeleteById(Long id);
    
    void deleteAll();
    
}
