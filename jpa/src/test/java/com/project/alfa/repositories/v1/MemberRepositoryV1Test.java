package com.project.alfa.repositories.v1;

import com.project.alfa.config.DummyGenerator;
import com.project.alfa.config.TestConfig;
import com.project.alfa.entities.AuthInfo;
import com.project.alfa.entities.Member;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@Import(TestConfig.class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberRepositoryV1Test {
    
    @Autowired
    MemberRepositoryV1 memberRepository;
    @PersistenceContext
    EntityManager      em;
    @Autowired
    PasswordEncoder    passwordEncoder;
    @Autowired
    DummyGenerator     dummy;
    
    @AfterEach
    void clear() {
        em.flush();
        em.clear();
    }
    
    @Test
    @DisplayName("계정 저장")
    void save() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        
        //When
        Long id = memberRepository.save(member).getId();
        
        //Then
        Member findMember = em.find(Member.class, id);
        
        assertThat(findMember).isEqualTo(member);
    }
    
    @Test
    @DisplayName("PK로 조회")
    void findById() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        Long id = member.getId();
        
        //When
        Member findMember = memberRepository.findById(id).get();
        
        //Then
        assertThat(findMember).isEqualTo(member);
    }
    
    @Test
    @DisplayName("PK로 조회, 존재하지 않는 PK")
    void findById_unknown() {
        //Given
        Long id = new Random().nextLong();
        
        //When
        Optional<Member> unknownMember = memberRepository.findById(id);
        
        //Then
        assertThat(unknownMember.isPresent()).isFalse();
    }
    
    @Test
    @DisplayName("PK, 탈퇴 여부로 조회")
    void findByIdAndDeleteYn() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        Long id = member.getId();
        
        //When
        Member findMember = memberRepository.findById(id, false).get();
        
        //Then
        assertThat(findMember).isEqualTo(member);
    }
    
    @Test
    @DisplayName("PK, 탈퇴 여부로 조회, 존재하지 않는 PK")
    void findByIdAndDeleteYn_unknown() {
        //Given
        Long id = new Random().nextLong();
        
        //When
        Optional<Member> unknownMember = memberRepository.findById(id, false);
        
        //Then
        assertThat(unknownMember.isPresent()).isFalse();
    }
    
    @Test
    @DisplayName("PK, 탈퇴 여부로 조회, 이미 탈퇴한 계정")
    void findByIdAndDeleteYn_alreadyDeleted() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        Long id = member.getId();
        member.isDelete(true);
        
        //When
        Optional<Member> deletedMember = memberRepository.findById(id, false);
        
        //Then
        assertThat(deletedMember.isPresent()).isFalse();
    }
    
    @Test
    @DisplayName("아이디로 조회")
    void findByUsername() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        String username = member.getUsername();
        
        //When
        Member findMember = memberRepository.findByUsername(username).get();
        
        //Then
        assertThat(findMember).isEqualTo(member);
    }
    
    @Test
    @DisplayName("아이디로 조회, 존재하지 않는 아이디")
    void findByUsername_unknown() {
        //Given
        String username = "user1@mail.com";
        
        //When
        Optional<Member> unknownMember = memberRepository.findByUsername(username);
        
        //Then
        assertThat(unknownMember.isPresent()).isFalse();
    }
    
    @Test
    @DisplayName("아이디, 탈퇴 여부로 조회")
    void findByUsernameAndDeleteYn() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        String username = member.getUsername();
        
        //When
        Member findMember = memberRepository.findByUsername(username, false).get();
        
        //Then
        assertThat(findMember).isEqualTo(member);
    }
    
    @Test
    @DisplayName("아이디, 탈퇴 여부로 조회, 존재하지 않는 아이디")
    void findByUsernameAndDeleteYn_unknown() {
        //Given
        String username = "user1@mail.com";
        
        //When
        Optional<Member> unknownMember = memberRepository.findByUsername(username, false);
        
        //Then
        assertThat(unknownMember.isPresent()).isFalse();
    }
    
    @Test
    @DisplayName("아이디, 탈퇴 여부로 조회, 이미 탈퇴한 계정")
    void findByUsernameAndDeleteYn_alreadyDeleted() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        String username = member.getUsername();
        member.isDelete(true);
        
        //When
        Optional<Member> deletedMember = memberRepository.findByUsername(username, false);
        
        //Then
        assertThat(deletedMember.isPresent()).isFalse();
    }
    
    @Test
    @DisplayName("모든 계정 조회")
    void findAll() {
        //Given
        int          total   = dummy.generateRandomNumber(1, 20);
        List<Member> members = dummy.createMembers(total);
        for (Member member : members)
            em.persist(member);
        
        //When
        List<Member> findMembers = memberRepository.findAll();
        
        //Then
        members = members.stream().sorted(comparing(Member::getId)).collect(toList());
        findMembers = findMembers.stream().sorted(comparing(Member::getId)).collect(toList());
        
        assertThat(findMembers.size()).isEqualTo(total);
        for (int i = 0; i < total; i++)
            assertThat(findMembers.get(i)).isEqualTo(members.get(i));
    }
    
    @Test
    @DisplayName("인증 여부로 모든 계정 조회")
    void findAllByAuth() {
        //Given
        int          total              = dummy.generateRandomNumber(1, 20);
        List<Member> members            = dummy.createMembers(total);
        Random       random             = new Random();
        int          authenticatedCount = 0;
        for (Member member : members) {
            em.persist(member);
            if (random.nextBoolean() && authenticatedCount < total) {
                member.authenticate();
                authenticatedCount++;
            }
        }
        
        //When
        List<Member> findMembers = memberRepository.findAllByAuth(true);
        
        //Then
        members = members.stream().filter(member -> member.getAuthInfo().isAuth()).sorted(comparing(Member::getId))
                         .collect(toList());
        findMembers = findMembers.stream().sorted(comparing(Member::getId)).collect(toList());
        
        assertThat(findMembers.size()).isEqualTo(authenticatedCount);
        for (int i = 0; i < authenticatedCount; i++)
            assertThat(findMembers.get(i)).isEqualTo(members.get(i));
    }
    
    @Test
    @DisplayName("탈퇴 여부로 모든 계정 조회")
    void findAllByDeleteYn() {
        //Given
        int          total        = dummy.generateRandomNumber(1, 20);
        List<Member> members      = dummy.createMembers(total);
        Random       random       = new Random();
        int          deletedCount = 0;
        for (Member member : members) {
            em.persist(member);
            if (random.nextBoolean() && deletedCount < total) {
                member.isDelete(true);
                deletedCount++;
            }
        }
        
        //When
        List<Member> findMembers = memberRepository.findAllByDeleteYn(false);
        
        //Then
        members = members.stream().filter(member -> !member.isDeleteYn()).sorted(comparing(Member::getId))
                         .collect(toList());
        findMembers = findMembers.stream().sorted(comparing(Member::getId)).collect(toList());
        
        assertThat(findMembers.size()).isEqualTo(total - deletedCount);
        for (int i = 0; i < total - deletedCount; i++)
            assertThat(findMembers.get(i)).isEqualTo(members.get(i));
    }
    
    @Test
    @DisplayName("이메일 인증 정보로 대상 계정 조회")
    void authenticateEmail() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        String username       = member.getUsername();
        String emailAuthToken = member.getAuthInfo().getEmailAuthToken();
        
        //When
        Member findMember = memberRepository.authenticateEmail(username, emailAuthToken, LocalDateTime.now()).get();
        
        //Then
        assertThat(findMember).isEqualTo(member);
    }
    
    @Test
    @DisplayName("이메일 인증 정보로 대상 계정 조회, 인증 만료 시간 초과")
    void authenticateEmail_timeout() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        String        username            = member.getUsername();
        String        emailAuthToken      = member.getAuthInfo().getEmailAuthToken();
        LocalDateTime emailAuthExpireTime = member.getAuthInfo().getEmailAuthExpireTime();
        
        //When
        Optional<Member> findMember = memberRepository.authenticateEmail(username,
                                                                         emailAuthToken,
                                                                         emailAuthExpireTime.plusSeconds(1));
        
        //Then
        assertThat(findMember.isPresent()).isFalse();
    }
    
    @Test
    @DisplayName("OAuth 2.0 인증 정보로 대상 계정 조회")
    void authenticateOAuth() {
        //Given
        Member member = Member.builder()
                              .username("user1@mail.com")
                              .password(passwordEncoder.encode("Password1!@"))
                              .authInfo(AuthInfo.builder()
                                                .oAuthProvider("google")
                                                .oAuthProviderId(UUID.randomUUID().toString())
                                                .build())
                              .nickname("user1")
                              .build();
        em.persist(member);
        String username        = member.getUsername();
        String oAuthProvider   = member.getAuthInfo().getOAuthProvider();
        String oAuthProviderId = member.getAuthInfo().getOAuthProviderId();
        
        //When
        Member findMember = memberRepository.authenticateOAuth(username, oAuthProvider, oAuthProviderId).get();
        
        //Then
        assertThat(findMember).isEqualTo(member);
    }
    
    @Test
    @DisplayName("정보 수정")
    void update() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        Long id = member.getId();
        clear();
        
        //When
        String newPassword = "Password2!@";
        String newEmailAuthToken;
        do {
            newEmailAuthToken = UUID.randomUUID().toString();
        } while (member.getAuthInfo().getEmailAuthToken().equals(newEmailAuthToken));
        String newNickname  = "user2";
        String newSignature = "Signature";
        
        Member findMember = memberRepository.findById(id).get();
        findMember.updatePassword(passwordEncoder.encode(newPassword));
        findMember.updateEmailAuthToken(newEmailAuthToken);
        findMember.updateNickname(newNickname);
        findMember.updateSignature(newSignature);
        clear();
        
        //Then
        Member updatedMember = em.find(Member.class, id);
        
        assertThat(passwordEncoder.matches(newPassword, member.getPassword())).isFalse();
        assertThat(passwordEncoder.matches(newPassword, updatedMember.getPassword())).isTrue();
        assertThat(member.getAuthInfo().getEmailAuthToken())
                .isNotEqualTo(updatedMember.getAuthInfo().getEmailAuthToken());
        assertThat(newEmailAuthToken).isEqualTo(updatedMember.getAuthInfo().getEmailAuthToken());
        assertThat(member.getSignature()).isNotEqualTo(updatedMember.getSignature());
        assertThat(newSignature).isEqualTo(updatedMember.getSignature());
    }
    
    @Test
    @DisplayName("아이디 사용 확인, 사용 중")
    void existsByUsername_using() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        String username = member.getUsername();
        
        //When
        boolean exists = memberRepository.existsByUsername(username);
        
        //Then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("아이디 사용 확인, 미사용")
    void existsByUsername_unused() {
        //Given
        String username = "user1@mail.com";
        
        //When
        boolean exists = memberRepository.existsByUsername(username);
        
        //Then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("아이디, 탈퇴 여부로 사용 확인, 사용 중")
    void existsByUsernameAndDeleteYn_using() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        String username = member.getUsername();
        
        //When
        boolean exists = memberRepository.existsByUsername(username, false);
        
        //Then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("아이디, 탈퇴 여부로 사용 확인, 미사용")
    void existsByUsernameAndDeleteYn_unused() {
        //Given
        String username = "user1@mail.com";
        
        //When
        boolean exists = memberRepository.existsByUsername(username, false);
        
        //Then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("아이디, 탈퇴 여부로 사용 확인, 이미 탈퇴한 계정")
    void existsByUsernameAndDeleteYn_alreadyDeleted() {
        //Given
        Member member   = dummy.createMembers(1).get(0);
        String username = member.getUsername();
        member.isDelete(true);
        
        //When
        boolean exists = memberRepository.existsByUsername(username, false);
        
        //Then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("닉네임 사용 확인, 사용 중")
    void existsByNickname_using() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        String nickname = member.getNickname();
        
        //When
        boolean exists = memberRepository.existsByNickname(nickname);
        
        //Then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("닉네임 사용 확인, 미사용")
    void existsByNickname_unused() {
        //Given
        String nickname = "user1";
        
        //When
        boolean exists = memberRepository.existsByNickname(nickname);
        
        //Then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("닉네임, 탈퇴 여부로 사용 확인, 사용 중")
    void existsByNicknameAndDeleteYn_using() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        String nickname = member.getNickname();
        
        //When
        boolean exists = memberRepository.existsByNickname(nickname, false);
        
        //Then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("닉네임, 탈퇴 여부로 사용 확인, 미사용")
    void existsByNicknameAndDeleteYn_unused() {
        //Given
        String nickname = "user1";
        
        //When
        boolean exists = memberRepository.existsByNickname(nickname, false);
        
        //Then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("닉네임, 탈퇴 여부로 사용 확인, 이미 탈퇴한 계정")
    void existsByNicknameAndDeleteYn_alreadyDeleted() {
        //Given
        Member member   = dummy.createMembers(1).get(0);
        String nickname = member.getNickname();
        member.isDelete(true);
        
        //When
        boolean exists = memberRepository.existsByNickname(nickname, false);
        
        //Then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("엔티티로 계정 정보 영구 삭제")
    void delete() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        Long id = member.getId();
        
        //When
        memberRepository.delete(member);
        
        //Then
        Member deletedMember = em.find(Member.class, id);
        
        assertThat(deletedMember).isNull();
    }
    
    @Test
    @DisplayName("PK로 계정 정보 영구 삭제")
    void deleteById() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        Long id = member.getId();
        
        //When
        memberRepository.deleteById(id);
        
        //Then
        Member deletedMember = em.find(Member.class, id);
        
        assertThat(deletedMember).isNull();
    }
    
}