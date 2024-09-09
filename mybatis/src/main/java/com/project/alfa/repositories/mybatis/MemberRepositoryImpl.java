package com.project.alfa.repositories.mybatis;

import com.project.alfa.entities.Member;
import com.project.alfa.repositories.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {
    
    private final MemberMapper memberMapper;
    
    /**
     * 계정 저장
     *
     * @param member - 계정 정보
     * @return 계정 정보
     */
    @Override
    public Member save(Member member) {
        memberMapper.save(member);
        return member;
    }
    
    /**
     * 계정 정보 조회
     *
     * @param id - PK
     * @return 계정 정보
     */
    @Override
    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(memberMapper.findById(id));
    }
    
    /**
     * 계정 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 탈퇴 여부
     * @return 계정 정보
     */
    @Override
    public Optional<Member> findById(Long id, boolean deleteYn) {
        return Optional.ofNullable(memberMapper.findByIdAndDeleteYn(id, deleteYn));
    }
    
    /**
     * 계정 정보 조회
     *
     * @param username - 아이디(이메일)
     * @return 계정 정보
     */
    @Override
    public Optional<Member> findByUsername(String username) {
        return Optional.ofNullable(memberMapper.findByUsername(username));
    }
    
    /**
     * 계정 정보 조회
     *
     * @param username - 아이디(이메일)
     * @param deleteYn - 탈퇴 여부
     * @return 계정 정보
     */
    @Override
    public Optional<Member> findByUsername(String username, boolean deleteYn) {
        return Optional.ofNullable(memberMapper.findByUsernameAndDeleteYn(username, deleteYn));
    }
    
    /**
     * 계정 정보 목록 조회
     *
     * @return 계정 정보 목록
     */
    @Override
    public List<Member> findAll() {
        return memberMapper.findAll();
    }
    
    /**
     * 계정 정보 목록 조회
     *
     * @param auth - 인증 여부
     * @return 계정 정보 목록
     */
    @Override
    public List<Member> findAllByAuth(boolean auth) {
        return memberMapper.findAllByAuth(auth);
    }
    
    /**
     * 계정 정보 목록 조회
     *
     * @param deleteYn - 탈퇴 여부
     * @return 계정 정보 목록
     */
    @Override
    public List<Member> findAllByDeleteYn(boolean deleteYn) {
        return memberMapper.findAllByDeleteYn(deleteYn);
    }
    
    /**
     * 이메일 인증 정보로 미인증 계정 인증
     *
     * @param username          - 아이디(이메일)
     * @param emailAuthToken    - 인증 토큰
     * @param authenticatedTime - 인증 시각
     */
    @Override
    public void authenticateEmail(String username, String emailAuthToken, LocalDateTime authenticatedTime) {
        memberMapper.authenticateEmail(username, emailAuthToken, authenticatedTime);
    }
    
    /**
     * OAuth 2.0 인증 정보로 미인증 계정 인증
     *
     * @param username          - 아이디
     * @param provider          - OAuth 2.0 Provider
     * @param providerId        - OAuth 2.0 Provider Id
     * @param authenticatedTime - 인증 시각
     */
    @Override
    public void authenticateOAuth(String username, String provider, String providerId,
                                  LocalDateTime authenticatedTime) {
        memberMapper.authenticateOAuth(username, provider, providerId, authenticatedTime);
    }
    
    /**
     * 계정 정보 수정
     *
     * @param param - 계정 수정 정보
     */
    @Override
    public void update(Member param) {
        memberMapper.update(param);
    }
    
    /**
     * 계정 엔티티 존재 확인
     *
     * @param id - PK
     * @return 존재 여부
     */
    @Override
    public boolean existsById(Long id) {
        return memberMapper.existsById(id);
    }
    
    /**
     * 계정 엔티티 존재 확인
     *
     * @param id       - PK
     * @param deleteYn - 탈퇴 여부
     * @return 존재 여부
     */
    @Override
    public boolean existsById(Long id, boolean deleteYn) {
        return memberMapper.existsByIdAndDeleteYn(id, deleteYn);
    }
    
    /**
     * 아이디(이메일) 사용 확인
     *
     * @param username - 아이디(이메일)
     * @return 사용 여부
     */
    @Override
    public boolean existsByUsername(String username) {
        return memberMapper.existsByUsername(username);
    }
    
    /**
     * 아이디(이메일) 사용 확인
     *
     * @param username - 아이디(이메일)
     * @param deleteYn - 탈퇴 여부
     * @return 사용 여부
     */
    @Override
    public boolean existsByUsername(String username, boolean deleteYn) {
        return memberMapper.existsByUsernameAndDeleteYn(username, deleteYn);
    }
    
    /**
     * 닉네임 사용 확인
     *
     * @param nickname - 닉네임
     * @return 사용 여부
     */
    @Override
    public boolean existsByNickname(String nickname) {
        return memberMapper.existsByNickname(nickname);
    }
    
    /**
     * 닉네임 사용 확인
     *
     * @param nickname - 닉네임
     * @param deleteYn - 탈퇴 여부
     * @return 사용 여부
     */
    @Override
    public boolean existsByNickname(String nickname, boolean deleteYn) {
        return memberMapper.existsByNicknameAndDeleteYn(nickname, deleteYn);
    }
    
    /**
     * 회원 탈퇴
     *
     * @param id - PK
     */
    @Override
    public void deleteById(Long id) {
        memberMapper.deleteById(id);
    }
    
    /**
     * 계정 정보 영구 삭제
     *
     * @param id - PK
     */
    @Override
    public void permanentlyDeleteById(Long id) {
        memberMapper.permanentlyDeleteById(id);
    }
    
    /**
     * 모든 계정 정보 영구 삭제
     */
    @Override
    public void deleteAll() {
        memberMapper.deleteAll();
    }
    
}
