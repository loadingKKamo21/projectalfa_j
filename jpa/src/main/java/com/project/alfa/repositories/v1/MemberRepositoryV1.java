package com.project.alfa.repositories.v1;

import com.project.alfa.entities.Member;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class MemberRepositoryV1 {
    
    @PersistenceContext
    private EntityManager em;
    
    /**
     * 계정 저장
     *
     * @param member - 계정 정보
     * @return 계정 정보
     */
    public Member save(final Member member) {
        em.persist(member);
        return member;
    }
    
    /**
     * 계정 정보 조회
     *
     * @param id - PK
     * @return 계정 정보
     */
    public Optional<Member> findById(final Long id) {
        return Optional.ofNullable(em.createQuery("SELECT m FROM Member m WHERE m.id = :id", Member.class)
                                     .setParameter("id", id)
                                     .getResultList().stream().findFirst().orElse(null));
    }
    
    /**
     * 계정 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 탈퇴 여부
     * @return 계정 정보
     */
    public Optional<Member> findById(final Long id, final boolean deleteYn) {
        return Optional.ofNullable(
                em.createQuery("SELECT m FROM Member m WHERE m.id = :id AND m.deleteYn = :deleteYn", Member.class)
                  .setParameter("id", id)
                  .setParameter("deleteYn", deleteYn)
                  .getResultList().stream().findFirst().orElse(null)
        );
    }
    
    /**
     * 계정 정보 조회
     *
     * @param username - 아이디(이메일)
     * @return 계정 정보
     */
    public Optional<Member> findByUsername(final String username) {
        return Optional.ofNullable(em.createQuery("SELECT m FROM Member m WHERE m.username = :username", Member.class)
                                     .setParameter("username", username)
                                     .getResultList().stream().findFirst().orElse(null));
    }
    
    /**
     * 계정 정보 조회
     *
     * @param username - 아이디(이메일)
     * @param deleteYn - 탈퇴 여부
     * @return 계정 정보
     */
    public Optional<Member> findByUsername(final String username, final boolean deleteYn) {
        return Optional.ofNullable(
                em.createQuery("SELECT m FROM Member m WHERE m.username = :username AND m.deleteYn = :deleteYn",
                               Member.class)
                  .setParameter("username", username)
                  .setParameter("deleteYn", deleteYn)
                  .getResultList().stream().findFirst().orElse(null)
        );
    }
    
    /**
     * 계정 정보 목록 조회
     *
     * @return 계정 정보 목록
     */
    public List<Member> findAll() {
        return em.createQuery("SELECT m FROM Member m", Member.class).getResultList();
    }
    
    /**
     * 계정 정보 목록 조회
     *
     * @param auth - 인증 여부
     * @return 계정 정보 목록
     */
    public List<Member> findAllByAuth(final boolean auth) {
        return em.createQuery("SELECT m FROM Member m WHERE m.authInfo.auth = :auth", Member.class)
                 .setParameter("auth", auth)
                 .getResultList();
    }
    
    /**
     * 계정 정보 목록 조회
     *
     * @param deleteYn - 탈퇴 여부
     * @return 계정 정보 목록
     */
    public List<Member> findAllByDeleteYn(final boolean deleteYn) {
        return em.createQuery("SELECT m FROM Member m WHERE m.deleteYn = :deleteYn", Member.class)
                 .setParameter("deleteYn", deleteYn)
                 .getResultList();
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
        return Optional.ofNullable(
                em.createQuery("SELECT m FROM Member m WHERE m.username = :username AND m.authInfo.emailAuthToken = :emailAuthToken AND m.authInfo.emailAuthExpireTime >= :authenticatedTime AND m.deleteYn = false",
                               Member.class)
                  .setParameter("username", username)
                  .setParameter("emailAuthToken", emailAuthToken)
                  .setParameter("authenticatedTime", authenticatedTime)
                  .getResultList().stream().findFirst().orElse(null)
        );
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
        return Optional.ofNullable(
                em.createQuery("SELECT m FROM Member m WHERE m.username = :username AND m.authInfo.oAuthProvider = :provider AND m.authInfo.oAuthProviderId = :providerId AND m.deleteYn = false",
                               Member.class)
                  .setParameter("username", username)
                  .setParameter("provider", provider)
                  .setParameter("providerId", providerId)
                  .getResultList().stream().findFirst().orElse(null)
        );
    }
    
    /**
     * 아이디(이메일) 사용 확인
     *
     * @param username - 아이디(이메일)
     * @return 사용 여부
     */
    public boolean existsByUsername(final String username) {
        return em.createQuery("SELECT CASE WHEN (COUNT(m) > 0) THEN TRUE ELSE FALSE END FROM Member m WHERE m.username = :username",
                              Boolean.class)
                 .setParameter("username", username).getSingleResult();
    }
    
    /**
     * 아이디(이메일) 사용 확인
     *
     * @param username - 아이디(이메일)
     * @param deleteYn - 탈퇴 여부
     * @return 사용 여부
     */
    public boolean existsByUsername(final String username, final boolean deleteYn) {
        return em.createQuery("SELECT CASE WHEN (COUNT(m) > 0) THEN TRUE ELSE FALSE END FROM Member m WHERE m.username = :username AND m.deleteYn = :deleteYn",
                              Boolean.class)
                 .setParameter("username", username)
                 .setParameter("deleteYn", deleteYn)
                 .getSingleResult();
    }
    
    /**
     * 닉네임 사용 확인
     *
     * @param nickname - 닉네임
     * @return 사용 여부
     */
    public boolean existsByNickname(final String nickname) {
        return em.createQuery("SELECT CASE WHEN (COUNT(m) > 0) THEN TRUE ELSE FALSE END FROM Member m WHERE m.nickname = :nickname",
                              Boolean.class)
                 .setParameter("nickname", nickname).getSingleResult();
    }
    
    /**
     * 닉네임 사용 확인
     *
     * @param nickname - 닉네임
     * @param deleteYn - 탈퇴 여부
     * @return 사용 여부
     */
    public boolean existsByNickname(final String nickname, final boolean deleteYn) {
        return em.createQuery("SELECT CASE WHEN (COUNT(m) > 0) THEN TRUE ELSE FALSE END FROM Member m WHERE m.nickname = :nickname AND m.deleteYn = :deleteYn",
                              Boolean.class)
                 .setParameter("nickname", nickname)
                 .setParameter("deleteYn", deleteYn)
                 .getSingleResult();
    }
    
    /**
     * 계정 정보 영구 삭제
     *
     * @param member - 계정 정보
     */
    public void delete(final Member member) {
        em.remove(em.find(Member.class, member.getId()));
    }
    
    /**
     * 계정 정보 영구 삭제
     *
     * @param id - PK
     */
    public void deleteById(final Long id) {
        em.remove(em.find(Member.class, id));
    }
    
}
