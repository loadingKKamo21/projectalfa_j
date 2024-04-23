package com.project.alfa.repositories;

import com.project.alfa.config.DummyGenerator;
import com.project.alfa.config.TestConfig;
import com.project.alfa.entities.AuthInfo;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Role;
import com.project.alfa.repositories.mybatis.MemberMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestConfig.class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberRepositoryTest {
    
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    MemberMapper     memberMapper;
    @Autowired
    DummyGenerator   dummy;
    
    @Test
    @DisplayName("계정 저장")
    void save() {
        //Given
        Member member = dummy.createMembers(1, false).get(0);
        
        //When
        Member savedMember = memberRepository.save(member);
        Long   id          = savedMember.getId();
        
        //Then
        Member findMember = memberMapper.findById(id);
        
        assertThat(findMember.getUsername()).isEqualTo(savedMember.getUsername());
        assertThat(findMember.getPassword()).isEqualTo(savedMember.getPassword());
        assertThat(findMember.getAuthInfo().isAuth()).isFalse();
        assertThat(findMember.getAuthInfo().getEmailAuthToken())
                .isEqualTo(savedMember.getAuthInfo().getEmailAuthToken());
        assertThat(findMember.getAuthInfo().getEmailAuthExpireTime())
                .isEqualTo(savedMember.getAuthInfo().getEmailAuthExpireTime());
        assertThat(findMember.getNickname()).isEqualTo(savedMember.getNickname());
        assertThat(findMember.getRole()).isEqualTo(savedMember.getRole());
        assertThat(findMember.isDeleteYn()).isFalse();
    }
    
    @Test
    @DisplayName("PK로 조회")
    void findById() {
        //Given
        Member member = dummy.createMembers(1, true).get(0);
        Long   id     = member.getId();
        
        //When
        Member findMember = memberRepository.findById(id).get();
        
        //Then
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember.getPassword()).isEqualTo(member.getPassword());
        assertThat(findMember.getAuthInfo().isAuth()).isFalse();
        assertThat(findMember.getAuthInfo().getEmailAuthToken())
                .isEqualTo(member.getAuthInfo().getEmailAuthToken());
        assertThat(findMember.getAuthInfo().getEmailAuthExpireTime())
                .isEqualTo(member.getAuthInfo().getEmailAuthExpireTime());
        assertThat(findMember.getNickname()).isEqualTo(member.getNickname());
        assertThat(findMember.getRole()).isEqualTo(member.getRole());
        assertThat(findMember.isDeleteYn()).isFalse();
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
        Member member = dummy.createMembers(1, true).get(0);
        Long   id     = member.getId();
        
        //When
        Member findMember = memberRepository.findById(id, false).get();
        
        //Then
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember.getPassword()).isEqualTo(member.getPassword());
        assertThat(findMember.getAuthInfo().isAuth()).isFalse();
        assertThat(findMember.getAuthInfo().getEmailAuthToken())
                .isEqualTo(member.getAuthInfo().getEmailAuthToken());
        assertThat(findMember.getAuthInfo().getEmailAuthExpireTime())
                .isEqualTo(member.getAuthInfo().getEmailAuthExpireTime());
        assertThat(findMember.getNickname()).isEqualTo(member.getNickname());
        assertThat(findMember.getRole()).isEqualTo(member.getRole());
        assertThat(findMember.isDeleteYn()).isFalse();
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
        Member member = dummy.createMembers(1, true).get(0);
        Long   id     = member.getId();
        memberMapper.deleteById(id);
        
        //When
        Optional<Member> deletedMember = memberRepository.findById(id, false);
        
        //Then
        assertThat(deletedMember.isPresent()).isFalse();
    }
    
    @Test
    @DisplayName("아이디로 조회")
    void findByUsername() {
        //Given
        Member member   = dummy.createMembers(1, true).get(0);
        String username = member.getUsername();
        
        //When
        Member findMember = memberRepository.findByUsername(username).get();
        
        //Then
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember.getPassword()).isEqualTo(member.getPassword());
        assertThat(findMember.getAuthInfo().isAuth()).isFalse();
        assertThat(findMember.getAuthInfo().getEmailAuthToken())
                .isEqualTo(member.getAuthInfo().getEmailAuthToken());
        assertThat(findMember.getAuthInfo().getEmailAuthExpireTime())
                .isEqualTo(member.getAuthInfo().getEmailAuthExpireTime());
        assertThat(findMember.getNickname()).isEqualTo(member.getNickname());
        assertThat(findMember.getRole()).isEqualTo(member.getRole());
        assertThat(findMember.isDeleteYn()).isFalse();
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
        Member member   = dummy.createMembers(1, true).get(0);
        String username = member.getUsername();
        
        //When
        Member findMember = memberRepository.findByUsername(username, false).get();
        
        //Then
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember.getPassword()).isEqualTo(member.getPassword());
        assertThat(findMember.getAuthInfo().isAuth()).isFalse();
        assertThat(findMember.getAuthInfo().getEmailAuthToken())
                .isEqualTo(member.getAuthInfo().getEmailAuthToken());
        assertThat(findMember.getAuthInfo().getEmailAuthExpireTime())
                .isEqualTo(member.getAuthInfo().getEmailAuthExpireTime());
        assertThat(findMember.getNickname()).isEqualTo(member.getNickname());
        assertThat(findMember.getRole()).isEqualTo(member.getRole());
        assertThat(findMember.isDeleteYn()).isFalse();
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
        Member member   = dummy.createMembers(1, true).get(0);
        String username = member.getUsername();
        Long   id       = member.getId();
        memberMapper.deleteById(id);
        
        //When
        Optional<Member> deletedMember = memberRepository.findByUsername(username, false);
        
        //Then
        assertThat(deletedMember.isPresent()).isFalse();
    }
    
    @Test
    @DisplayName("모든 계정 조회")
    void findAll() {
        //Given
        int          size    = dummy.generateRandomNumber(1, 20);
        List<Member> members = dummy.createMembers(size, true);
        
        //When
        List<Member> findMembers = memberRepository.findAll();
        
        //Then
        assertThat(findMembers.size()).isEqualTo(size);
        assertThat(findMembers.stream().allMatch(member -> member instanceof Member)).isTrue();
    }
    
    @Test
    @DisplayName("인증 여부로 모든 계정 조회")
    void findAllByAuth() {
        //Given
        Random       random             = new Random();
        int          size               = dummy.generateRandomNumber(1, 20);
        List<Member> members            = dummy.createMembers(size, true);
        int          authenticatedCount = 0;
        for (Member member : members) {
            String username       = member.getUsername();
            String emailAuthToken = member.getAuthInfo().getEmailAuthToken();
            if (random.nextBoolean()) {
                memberMapper.authenticateEmail(username, emailAuthToken, LocalDateTime.now());
                authenticatedCount++;
            }
        }
        
        //When
        List<Member> findMembers = memberRepository.findAllByAuth(true);
        
        //Then
        assertThat(findMembers.size()).isEqualTo(authenticatedCount);
        assertThat(findMembers.stream().allMatch(member -> member instanceof Member)).isTrue();
    }
    
    @Test
    @DisplayName("탈퇴 여부로 모든 계정 조회")
    void findAllByDeleteYn() {
        //Given
        Random       random       = new Random();
        int          size         = dummy.generateRandomNumber(1, 20);
        List<Member> members      = dummy.createMembers(size, true);
        int          deletedCount = 0;
        for (Member member : members) {
            Long id = member.getId();
            if (id % 2 != 0 && !random.nextBoolean()) {
                memberMapper.deleteById(id);
                deletedCount++;
            }
        }
        int undeletedMemberSize = size - deletedCount;
        
        //When
        List<Member> findMembers = memberRepository.findAllByDeleteYn(false);
        
        //Then
        assertThat(findMembers.size()).isEqualTo(undeletedMemberSize);
        assertThat(findMembers.stream().allMatch(member -> member instanceof Member)).isTrue();
    }
    
    @Test
    @DisplayName("이메일 인증")
    void authenticateEmail() {
        //Given
        Member member         = dummy.createMembers(1, true).get(0);
        Long   id             = member.getId();
        String username       = member.getUsername();
        String emailAuthToken = member.getAuthInfo().getEmailAuthToken();
        
        //When
        memberRepository.authenticateEmail(username, emailAuthToken, LocalDateTime.now());
        
        //Then
        Member findMember = memberMapper.findById(id);
        
        assertThat(member.getAuthInfo().isAuth()).isFalse();
        assertThat(findMember.getAuthInfo().isAuth()).isTrue();
    }
    
    @Test
    @DisplayName("이메일 인증, 인증 만료 시간 초과")
    void authenticateEmail_timeout() {
        //Given
        Member        member         = dummy.createMembers(1, true).get(0);
        Long          id             = member.getId();
        String        username       = member.getUsername();
        String        emailAuthToken = member.getAuthInfo().getEmailAuthToken();
        LocalDateTime expireTime     = member.getAuthInfo().getEmailAuthExpireTime();
        
        //When
        memberRepository.authenticateEmail(username, emailAuthToken, expireTime.plusSeconds(1));
        
        //Then
        Member findMember = memberMapper.findById(id);
        
        assertThat(member.getAuthInfo().isAuth()).isFalse();
        assertThat(findMember.getAuthInfo().isAuth()).isFalse();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("정보 수정")
    void update() {
        //Given
        Member member = dummy.createMembers(1, true).get(0);
        Long   id     = member.getId();
        memberMapper.authenticateEmail(member.getUsername(), member.getAuthInfo().getEmailAuthToken(),
                                       LocalDateTime.now());
        
        boolean beforeAuth = memberMapper.findById(id).getAuthInfo().isAuth();
        
        Thread.sleep(1000);
        
        Member param = Member.builder()
                             .id(id)
                             .password("newPassword1!@")
                             .authInfo(AuthInfo.builder()
                                               .emailAuthToken(UUID.randomUUID().toString())
                                               .emailAuthExpireTime(LocalDateTime.now().withNano(0).plusMinutes(5))
                                               .build())
                             .nickname("newNickname")
                             .role(Role.ADMIN)
                             .build();
        
        //When
        memberRepository.update(param);
        
        //Then
        Member findMember = memberMapper.findById(id);
        
        boolean afterAuth = findMember.getAuthInfo().isAuth();
        
        assertThat(findMember.getPassword()).isEqualTo(param.getPassword());
        assertThat(member.getPassword()).isNotEqualTo(param.getPassword());
        assertThat(beforeAuth).isTrue();
        assertThat(afterAuth).isFalse();
        assertThat(findMember.getAuthInfo().getEmailAuthToken())
                .isEqualTo(param.getAuthInfo().getEmailAuthToken());
        assertThat(member.getAuthInfo().getEmailAuthToken())
                .isNotEqualTo(param.getAuthInfo().getEmailAuthToken());
        assertThat(findMember.getAuthInfo().getEmailAuthExpireTime())
                .isEqualTo(param.getAuthInfo().getEmailAuthExpireTime());
        assertThat(member.getAuthInfo().getEmailAuthExpireTime())
                .isNotEqualTo(param.getAuthInfo().getEmailAuthExpireTime());
        assertThat(findMember.getNickname()).isEqualTo(param.getNickname());
        assertThat(member.getNickname()).isNotEqualTo(param.getNickname());
        assertThat(findMember.getRole()).isEqualTo(param.getRole());
        assertThat(member.getRole()).isNotEqualTo(param.getRole());
    }
    
    @Test
    @DisplayName("PK로 엔티티 존재 여부 확인")
    void existsById() {
        //Given
        Member member = dummy.createMembers(1, true).get(0);
        Long   id     = member.getId();
        
        //When
        boolean exists = memberRepository.existsById(id);
        
        //Then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("PK로 엔티티 존재 여부 확인, 없음")
    void existsById_unknown() {
        //Given
        Long id = new Random().nextLong();
        
        //When
        boolean exists = memberRepository.existsById(id);
        
        //Then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("PK, 탈퇴 여부로 엔티티 존재 여부 확인")
    void existsByIdAndDeleteYn() {
        //Given
        Member member = dummy.createMembers(1, true).get(0);
        Long   id     = member.getId();
        
        //When
        boolean exists = memberRepository.existsById(id, false);
        
        //Then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("PK, 탈퇴 여부로 엔티티 존재 여부 확인, 없음")
    void existsByIdAndDeleteYn_unknown() {
        //Given
        Long id = new Random().nextLong();
        
        //When
        boolean exists = memberRepository.existsById(id, false);
        
        //Then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("PK, 탈퇴 여부로 엔티티 존재 여부 확인, 이미 탈퇴한 계정")
    void existsByIdAndDelete_alreadyDeleted() {
        //Given
        Member member = dummy.createMembers(1, true).get(0);
        Long   id     = member.getId();
        memberMapper.deleteById(id);
        
        //When
        boolean exists = memberRepository.existsById(id, false);
        
        //Then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("아이디 사용 확인, 사용 중")
    void existsByUsername_using() {
        //Given
        Member member   = dummy.createMembers(1, true).get(0);
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
        Member member   = dummy.createMembers(1, true).get(0);
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
        Member member   = dummy.createMembers(1, true).get(0);
        String username = member.getUsername();
        Long   id       = member.getId();
        memberMapper.deleteById(id);
        
        //When
        boolean exists = memberRepository.existsByUsername(username, false);
        
        //Then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("닉네임 사용 확인, 사용 중")
    void existsByNickname_using() {
        //Given
        Member member   = dummy.createMembers(1, true).get(0);
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
        Member member   = dummy.createMembers(1, true).get(0);
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
        Member member   = dummy.createMembers(1, true).get(0);
        String nickname = member.getNickname();
        Long   id       = member.getId();
        memberMapper.deleteById(id);
        
        //When
        boolean exists = memberRepository.existsByNickname(nickname, false);
        
        //Then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("계정 탈퇴")
    void deleteById() {
        //Given
        Member member = dummy.createMembers(1, true).get(0);
        Long   id     = member.getId();
        
        //When
        memberRepository.deleteById(id);
        
        //Then
        Member deletedMember = memberMapper.findById(id);
        
        assertThat(deletedMember.isDeleteYn()).isTrue();
    }
    
    @Test
    @DisplayName("계정 정보 영구 삭제")
    void permanentlyDeleteById() {
        //Given
        Member member = dummy.createMembers(1, true).get(0);
        Long   id     = member.getId();
        memberMapper.deleteById(id);
        
        //When
        memberRepository.permanentlyDeleteById(id);
        
        //Then
        Member unknownMember = memberMapper.findById(id);
        
        assertThat(unknownMember).isNull();
    }
    
}
