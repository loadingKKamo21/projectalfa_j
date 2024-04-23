package com.project.alfa.repositories.v3;

import com.project.alfa.entities.Member;
import com.project.alfa.repositories.v3.querydsl.MemberRepositoryV3Custom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepositoryV3 extends JpaRepository<Member, Long>, MemberRepositoryV3Custom {
    
    Optional<Member> findById(Long id);
    
    @Query("SELECT m FROM Member m WHERE m.id = :id AND m.deleteYn = :deleteYn")
    Optional<Member> findById(@Param("id") Long id, @Param("deleteYn") boolean deleteYn);
    
    Optional<Member> findByUsername(String username);
    
    @Query("SELECT m FROM Member m WHERE m.username = :username AND m.deleteYn = :deleteYn")
    Optional<Member> findByUsername(@Param("username") String username, @Param("deleteYn") boolean deleteYn);
    
    @Query("SELECT m FROM Member m WHERE m.authInfo.auth = :auth")
    List<Member> findAllByAuth(@Param("auth") boolean auth);
    
    List<Member> findAllByDeleteYn(boolean deleteYn);
    
    @Query("SELECT m FROM Member m WHERE m.username = :username AND m.authInfo.emailAuthToken = :emailAuthToken AND m.authInfo.emailAuthExpireTime >= :authenticatedTime AND m.deleteYn = false")
    Optional<Member> authenticateEmail(@Param("username") String username,
                                       @Param("emailAuthToken") String emailAuthToken,
                                       @Param("authenticatedTime") LocalDateTime authenticatedTime);
    
    @Query("SELECT m FROM Member m WHERE m.username = :username AND m.authInfo.oAuthProvider = :provider AND m.authInfo.oAuthProviderId = :providerId AND m.deleteYn = false")
    Optional<Member> authenticateOAuth(@Param("username") String username,
                                       @Param("provider") String provider,
                                       @Param("providerId") String providerId);
    
    boolean existsByUsername(String username);
    
    @Query("SELECT CASE WHEN (COUNT(m) > 0) THEN TRUE ELSE FALSE END FROM Member m WHERE m.username = :username AND m.deleteYn = :deleteYn")
    boolean existsByUsername(@Param("username") String username, @Param("deleteYn") boolean deleteYn);
    
    boolean existsByNickname(String nickname);
    
    @Query("SELECT CASE WHEN (COUNT(m) > 0) THEN TRUE ELSE FALSE END FROM Member m WHERE m.nickname = :nickname AND m.deleteYn = :deleteYn")
    boolean existsByNickname(@Param("nickname") String nickname, @Param("deleteYn") boolean deleteYn);
    
}
