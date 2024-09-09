package com.project.alfa.repositories.mybatis;

import com.project.alfa.entities.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MemberMapper {
    
    void save(Member param);
    
    Member findById(Long id);
    
    Member findByIdAndDeleteYn(@Param("id") Long id, @Param("deleteYn") boolean deleteYn);
    
    Member findByUsername(String username);
    
    Member findByUsernameAndDeleteYn(@Param("username") String username, @Param("deleteYn") boolean deleteYn);
    
    List<Member> findAll();
    
    List<Member> findAllByAuth(boolean auth);
    
    List<Member> findAllByDeleteYn(boolean deleteYn);
    
    void update(Member param);
    
    void authenticateEmail(@Param("username") String username,
                           @Param("emailAuthToken") String emailAuthToken,
                           @Param("authenticatedTime") LocalDateTime authenticatedTime);
    
    void authenticateOAuth(@Param("username") String username,
                           @Param("provider") String provider,
                           @Param("providerId") String providerId,
                           @Param("authenticatedTime") LocalDateTime authenticatedTime);
    
    boolean existsById(Long id);
    
    boolean existsByIdAndDeleteYn(@Param("id") Long id, @Param("deleteYn") boolean deleteYn);
    
    boolean existsByUsername(String username);
    
    boolean existsByUsernameAndDeleteYn(@Param("username") String username, @Param("deleteYn") boolean deleteYn);
    
    boolean existsByNickname(String nickname);
    
    boolean existsByNicknameAndDeleteYn(@Param("nickname") String nickname, @Param("deleteYn") boolean deleteYn);
    
    void deleteById(Long id);
    
    void permanentlyDeleteById(Long id);
    
    void deleteAll();
    
}
