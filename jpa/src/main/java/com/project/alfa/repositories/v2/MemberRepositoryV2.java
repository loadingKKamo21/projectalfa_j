package com.project.alfa.repositories.v2;

import com.project.alfa.entities.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryV2 {
    
    private final MemberJpaRepository memberJpaRepository;
    
    /**
     * 계정 저장
     *
     * @param member - 계정 정보
     * @return 계정 정보
     */
    public Member save(final Member member) {
        return memberJpaRepository.save(member);
    }
    
    /**
     * 계정 정보 조회
     *
     * @param id - PK
     * @return 계정 정보
     */
    public Optional<Member> findById(final Long id) {
        return memberJpaRepository.findById(id);
    }
    
    /**
     * 계정 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 탈퇴 여부
     * @return 계정 정보
     */
    public Optional<Member> findById(final Long id, final boolean deleteYn) {
        return memberJpaRepository.findByIdAndDeleteYn(id, deleteYn);
    }
    
    /**
     * 계정 정보 조회
     *
     * @param username - 아이디(이메일)
     * @return 계정 정보
     */
    public Optional<Member> findByUsername(final String username) {
        return memberJpaRepository.findByUsername(username);
    }
    
    /**
     * 계정 정보 조회
     *
     * @param username - 아이디(이메일)
     * @param deleteYn - 탈퇴 여부
     * @return 계정 정보
     */
    public Optional<Member> findByUsername(final String username, final boolean deleteYn) {
        return memberJpaRepository.findByUsernameAndDeleteYn(username, deleteYn);
    }
    
    /**
     * 계정 정보 목록 조회
     *
     * @return 계정 정보 목록
     */
    public List<Member> findAll() {
        return memberJpaRepository.findAll();
    }
    
    /**
     * 계정 정보 목록 조회
     *
     * @param auth - 인증 여부
     * @return 계정 정보 목록
     */
    public List<Member> findAllByAuth(final boolean auth) {
        return memberJpaRepository.findAllByAuthInfo_Auth(auth);
    }
    
    /**
     * 계정 정보 목록 조회
     *
     * @param deleteYn - 탈퇴 여부
     * @return 계정 정보 목록
     */
    public List<Member> findAllByDeleteYn(final boolean deleteYn) {
        return memberJpaRepository.findAllByDeleteYn(deleteYn);
    }
    
    /**
     * 이메일 인증 정보로 미인증 계정 정보 조회
     *
     * @param username          - 아이디(이메일)
     * @param emailAuthToken    - 인증 토큰
     * @param authenticatedTime - 인증 시각
     * @return 계정 정보
     */
    public Optional<Member> authenticateEmail(final String username,
                                              final String emailAuthToken,
                                              final LocalDateTime authenticatedTime) {
        return memberJpaRepository.findByUsernameAndAuthInfo_EmailAuthTokenAndAuthInfo_EmailAuthExpireTimeGreaterThanEqualAndDeleteYnFalse(
                username, emailAuthToken, authenticatedTime);
    }
    
    /**
     * OAuth 2.0 인증 정보로 미인증 계정 정보 조회
     *
     * @param username   - 아이디
     * @param provider   - OAuth 2.0 Provider
     * @param providerId - OAuth 2.0 Provider Id
     * @return 계정 정보
     */
    public Optional<Member> authenticateOAuth(final String username,
                                              final String provider,
                                              final String providerId) {
        return memberJpaRepository.findByUsernameAndAuthInfo_oAuthProviderAndAuthInfo_oAuthProviderIdAndDeleteYnFalse(
                username, provider, providerId);
    }
    
    /**
     * 아이디(이메일) 사용 확인
     *
     * @param username - 아이디(이메일)
     * @return 사용 여부
     */
    public boolean existsByUsername(final String username) {
        return memberJpaRepository.existsByUsername(username);
    }
    
    /**
     * 아이디(이메일) 사용 확인
     *
     * @param username - 아이디(이메일)
     * @param deleteYn - 탈퇴 여부
     * @return 사용 여부
     */
    public boolean existsByUsername(final String username, final boolean deleteYn) {
        return memberJpaRepository.existsByUsernameAndDeleteYn(username, deleteYn);
    }
    
    /**
     * 닉네임 사용 확인
     *
     * @param nickname - 닉네임
     * @return 사용 여부
     */
    public boolean existsByNickname(final String nickname) {
        return memberJpaRepository.existsByNickname(nickname);
    }
    
    /**
     * 닉네임 사용 확인
     *
     * @param nickname - 닉네임
     * @param deleteYn - 탈퇴 여부
     * @return 사용 여부
     */
    public boolean existsByNickname(final String nickname, final boolean deleteYn) {
        return memberJpaRepository.existsByNicknameAndDeleteYn(nickname, deleteYn);
    }
    
    /**
     * 계정 정보 영구 삭제
     *
     * @param member - 계정 정보
     */
    public void delete(final Member member) {
        memberJpaRepository.delete(member);
    }
    
    /**
     * 계정 정보 영구 삭제
     *
     * @param id - PK
     */
    public void deleteById(final Long id) {
        memberJpaRepository.deleteById(id);
    }
    
}
