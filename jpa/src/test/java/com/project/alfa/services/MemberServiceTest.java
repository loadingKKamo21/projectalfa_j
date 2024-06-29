package com.project.alfa.services;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import com.project.alfa.config.DummyGenerator;
import com.project.alfa.config.TestConfig;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Role;
import com.project.alfa.error.exception.EntityNotFoundException;
import com.project.alfa.error.exception.ErrorCode;
import com.project.alfa.error.exception.InvalidValueException;
import com.project.alfa.services.dto.MemberInfoResponseDto;
import com.project.alfa.services.dto.MemberJoinRequestDto;
import com.project.alfa.services.dto.MemberUpdateRequestDto;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestConfig.class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberServiceTest {
    
    @RegisterExtension
    static GreenMailExtension greenMailExtension = new GreenMailExtension(new ServerSetup(3025, null, "smtp"))
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("springboot", "secret"))
            .withPerMethodLifecycle(true);
    
    @Autowired
    MemberService   memberService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @PersistenceContext
    EntityManager   em;
    @Autowired
    DummyGenerator  dummy;
    
    @AfterEach
    void clear() {
        em.flush();
        em.clear();
    }
    
    @SneakyThrows(MessagingException.class)
    @Test
    @DisplayName("회원 가입")
    void join() {
        //Given
        MemberJoinRequestDto dto = new MemberJoinRequestDto("user1@mail.com", "Password1!@", "Password1!@", "user1");
        
        //When
        Long id = memberService.join(dto);
        clear();
        
        //Then
        greenMailExtension.waitForIncomingEmail(5000, 1);
        Member        findMember       = em.find(Member.class, id);
        MimeMessage[] receivedMessages = greenMailExtension.getReceivedMessages();
        
        assertThat(dto.getUsername().toLowerCase()).isEqualTo(findMember.getUsername());
        assertThat(passwordEncoder.matches(dto.getPassword(), findMember.getPassword())).isTrue();
        assertThat(dto.getNickname()).isEqualTo(findMember.getNickname());
        assertThat(findMember.getRole()).isEqualTo(Role.USER);
        
        assertThat(receivedMessages).hasSize(1);
        assertThat(findMember.getUsername())
                .isEqualTo(receivedMessages[0].getRecipients(Message.RecipientType.TO)[0].toString());
    }
    
    @Test
    @DisplayName("회원 가입, 비밀번호 확인 불일치")
    void join_wrongPassword() {
        //Given
        MemberJoinRequestDto dto = new MemberJoinRequestDto("user1@mail.com", "Password1!@", "Password2!@", "user1");
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> memberService.join(dto))
                .isInstanceOf(InvalidValueException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_DO_NOT_MATCH)
                .hasMessage("Invalid input value, Password do not match.");
    }
    
    @Test
    @DisplayName("회원 가입, 아이디 중복")
    void join_duplicateUsername() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        String username = member.getUsername();
        
        MemberJoinRequestDto dto = new MemberJoinRequestDto(username, "Password2!@", "Password2!@", "user2");
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> memberService.join(dto))
                .isInstanceOf(InvalidValueException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USERNAME_DUPLICATION)
                .hasMessage("Invalid input value: " + username);
    }
    
    @Test
    @DisplayName("회원 가입, 닉네임 중복")
    void join_duplicateNickname() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        String nickname = member.getNickname();
        
        MemberJoinRequestDto dto = new MemberJoinRequestDto("user2@mail.com", "Password2!@", "Password2!@", nickname);
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> memberService.join(dto))
                .isInstanceOf(InvalidValueException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NICKNAME_DUPLICATION)
                .hasMessage("Invalid input value: " + nickname);
    }
    
    @Test
    @DisplayName("이메일 인증")
    void verifyEmailAuth() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        Long    id     = member.getId();
        boolean before = member.getAuthInfo().isAuth();
        
        //When
        memberService.verifyEmailAuth(member.getUsername(),
                                      member.getAuthInfo().getEmailAuthToken(),
                                      LocalDateTime.now());
        clear();
        
        //Then
        boolean after = em.find(Member.class, id).getAuthInfo().isAuth();
        
        assertThat(before).isFalse();
        assertThat(after).isTrue();
    }
    
    @Test
    @DisplayName("이메일 인증, 존재하지 않는 계정")
    void verifyEmailAuth_unknown() {
        //Given
        String username  = "user1@mail.com";
        String authToken = UUID.randomUUID().toString();
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> memberService.verifyEmailAuth(username, authToken, LocalDateTime.now()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Member' by username: " + username);
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("이메일 인증, 이미 인증된 계정")
    void verifyEmailAuth_completed() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        Long          id         = member.getId();
        String        authToken  = member.getAuthInfo().getEmailAuthToken();
        LocalDateTime expireTime = member.getAuthInfo().getEmailAuthExpireTime();
        member.authenticate();  ////이메일 인증
        
        //When
        Thread.sleep(1000);
        
        memberService.verifyEmailAuth(member.getUsername(),
                                      member.getAuthInfo().getEmailAuthToken(),
                                      LocalDateTime.now());
        clear();
        
        //Then
        Member findMember = em.find(Member.class, id);
        
        //이미 인증된 계정의 경우 인증 토큰, 인증 만료 제한 시간은 변경되지 않음
        assertThat(findMember.getAuthInfo().getEmailAuthToken()).isEqualTo(authToken);
        assertThat(findMember.getAuthInfo().getEmailAuthExpireTime()).isEqualTo(expireTime);
    }
    
    @SneakyThrows({InterruptedException.class, MessagingException.class})
    @Test
    @DisplayName("이메일 인증, 잘못된 토큰")
    void verifyEmailAuth_wrongAuthToken() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        Long          id         = member.getId();
        String        authToken  = member.getAuthInfo().getEmailAuthToken();
        LocalDateTime expireTime = member.getAuthInfo().getEmailAuthExpireTime();
        
        String otherAuthToken;
        do {
            otherAuthToken = UUID.randomUUID().toString();
        } while (authToken.equals(otherAuthToken));
        
        //When
        Thread.sleep(1000);
        
        memberService.verifyEmailAuth(member.getUsername(), otherAuthToken, LocalDateTime.now());
        clear();
        
        //Then
        greenMailExtension.waitForIncomingEmail(5000, 1);
        Member        findMember       = em.find(Member.class, id);
        MimeMessage[] receivedMessages = greenMailExtension.getReceivedMessages();
        
        //잘못된 토큰으로 인증을 시도한 경우 새로운 토큰 및 만료 제한 시간으로 인증 메일이 재전송됨
        assertThat(findMember.getAuthInfo().isAuth()).isFalse();
        assertThat(findMember.getAuthInfo().getEmailAuthToken()).isNotEqualTo(authToken);
        assertThat(findMember.getAuthInfo().getEmailAuthExpireTime()).isNotEqualTo(expireTime);
        
        assertThat(receivedMessages).hasSize(1);
        assertThat(findMember.getUsername())
                .isEqualTo(receivedMessages[0].getRecipients(Message.RecipientType.TO)[0].toString());
    }
    
    @SneakyThrows({InterruptedException.class, MessagingException.class})
    @Test
    @DisplayName("이메일 인증, 만료 제한 시간 초과")
    void verifyEmailAuth_timeout() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        Long          id         = member.getId();
        String        authToken  = member.getAuthInfo().getEmailAuthToken();
        LocalDateTime expireTime = member.getAuthInfo().getEmailAuthExpireTime();
        
        //When
        Thread.sleep(1000);
        
        memberService.verifyEmailAuth(member.getUsername(),
                                      member.getAuthInfo().getEmailAuthToken(),
                                      expireTime.plusSeconds(1));
        clear();
        
        //Then
        greenMailExtension.waitForIncomingEmail(5000, 1);
        Member        findMember       = em.find(Member.class, id);
        MimeMessage[] receivedMessages = greenMailExtension.getReceivedMessages();
        
        //인증 만료 제한 시간 초과 후 시도한 경우 새로운 토큰 및 만료 제한 시간으로 인증 메일이 재전송됨
        assertThat(findMember.getAuthInfo().isAuth()).isFalse();
        assertThat(findMember.getAuthInfo().getEmailAuthToken()).isNotEqualTo(authToken);
        assertThat(findMember.getAuthInfo().getEmailAuthExpireTime()).isNotEqualTo(expireTime);
        
        assertThat(receivedMessages).hasSize(1);
        assertThat(findMember.getUsername())
                .isEqualTo(receivedMessages[0].getRecipients(Message.RecipientType.TO)[0].toString());
    }
    
    @SneakyThrows(MessagingException.class)
    @Test
    @DisplayName("비밀번호 찾기")
    void findPassword() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        Long id = member.getId();
        member.authenticate();  //이메일 인증
        
        //When
        memberService.findPassword(member.getUsername());
        clear();
        
        //Then
        greenMailExtension.waitForIncomingEmail(5000, 1);
        Member        findMember       = em.find(Member.class, id);
        MimeMessage[] receivedMessages = greenMailExtension.getReceivedMessages();
        
        //비밀번호 찾기를 시도하면 20자리 임시 비밀번호로 변경되고, 메일로 전송됨
        assertThat(findMember.getPassword()).isNotEqualTo(passwordEncoder.encode(member.getPassword()));
        assertThat(passwordEncoder.matches("Password1!@", findMember.getPassword())).isFalse();
        
        assertThat(receivedMessages).hasSize(1);
        assertThat(findMember.getUsername())
                .isEqualTo(receivedMessages[0].getRecipients(Message.RecipientType.TO)[0].toString());
    }
    
    @SneakyThrows({InterruptedException.class, MessagingException.class})
    @Test
    @DisplayName("비밀번호 찾기, 미인증 상태")
    void findPassword_unauth() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        Long          id         = member.getId();
        String        authToken  = member.getAuthInfo().getEmailAuthToken();
        LocalDateTime expireTime = member.getAuthInfo().getEmailAuthExpireTime();
        
        //When
        Thread.sleep(1000);
        clear();
        
        //Then
        assertThatThrownBy(() -> memberService.findPassword(member.getUsername()))
                .isInstanceOf(InvalidValueException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_NOT_COMPLETED)
                .hasMessage("Email is not verified.");
        
        greenMailExtension.waitForIncomingEmail(5000, 1);
        Member        findMember       = em.find(Member.class, id);
        MimeMessage[] receivedMessages = greenMailExtension.getReceivedMessages();
        
        //인증되지 않은 계정에 비밀번호 찾기를 시도하면 임시 비밀번호는 발급되지 않고 인증 메일이 전송됨
        assertThat(findMember.getAuthInfo().isAuth()).isFalse();
        assertThat(findMember.getAuthInfo().getEmailAuthToken()).isNotEqualTo(authToken);
        assertThat(findMember.getAuthInfo().getEmailAuthExpireTime()).isNotEqualTo(expireTime);
        assertThat(passwordEncoder.matches("Password1!@", findMember.getPassword())).isTrue();  //임시 비밀번호로 변경되지 않음
        
        assertThat(receivedMessages).hasSize(1);
        assertThat(findMember.getUsername())
                .isEqualTo(receivedMessages[0].getRecipients(Message.RecipientType.TO)[0].toString());
    }
    
    @Test
    @DisplayName("PK로 조회")
    void findById() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        Long id = member.getId();
        
        //When
        MemberInfoResponseDto dto = memberService.findById(id);
        clear();
        
        //Then
        Member findMember = em.find(Member.class, id);
        
        assertThat(findMember.getUsername()).isEqualTo(dto.getUsername());
        assertThat(findMember.getNickname()).isEqualTo(dto.getNickname());
        assertThat(findMember.getSignature()).isEqualTo(dto.getSignature());
        assertThat(findMember.getRole()).isEqualTo(dto.getRole());
        assertThat(findMember.getCreatedDate()).isEqualTo(dto.getCreatedDate());
        assertThat(findMember.getLastModifiedDate()).isEqualTo(dto.getLastModifiedDate());
    }
    
    @Test
    @DisplayName("PK로 조회, 존재하지 않는 PK")
    void findById_unknown() {
        //Given
        Long id = new Random().nextLong();
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> memberService.findById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Member' by id: " + id);
    }
    
    @Test
    @DisplayName("아이디로 조회")
    void findByUsername() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        Long   id       = member.getId();
        String username = member.getUsername();
        
        //When
        MemberInfoResponseDto dto = memberService.findByUsername(username);
        clear();
        
        //Then
        Member findMember = em.find(Member.class, id);
        
        assertThat(findMember.getUsername()).isEqualTo(dto.getUsername());
        assertThat(findMember.getNickname()).isEqualTo(dto.getNickname());
        assertThat(findMember.getSignature()).isEqualTo(dto.getSignature());
        assertThat(findMember.getRole()).isEqualTo(dto.getRole());
        assertThat(findMember.getCreatedDate()).isEqualTo(dto.getCreatedDate());
        assertThat(findMember.getLastModifiedDate()).isEqualTo(dto.getLastModifiedDate());
    }
    
    @Test
    @DisplayName("아이디로 조회, 존재하지 않는 아이디")
    void findByUsername_unknown() {
        //Given
        String username = "user1@mail.com";
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> memberService.findByUsername(username))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Member' by username: " + username);
    }
    
    @Test
    @DisplayName("정보 수정")
    void update() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        Long id = member.getId();
        member.authenticate();  //이메일 인증
        String beforeNickname  = member.getNickname();
        String beforeSignature = member.getSignature();
        
        MemberUpdateRequestDto dto = new MemberUpdateRequestDto(id, "Password1!@", "user2", "Signature",
                                                                "Password2!@", "Password2!@");
        
        //When
        memberService.update(dto);
        clear();
        
        //Then
        Member afterMember = em.find(Member.class, id);
        
        assertThat(passwordEncoder.matches(dto.getNewPassword(), afterMember.getPassword())).isTrue();
        assertThat(passwordEncoder.matches(dto.getPassword(), afterMember.getPassword())).isFalse();
        assertThat(afterMember.getNickname()).isEqualTo(dto.getNickname());
        assertThat(beforeNickname).isNotEqualTo(dto.getNickname());
        assertThat(afterMember.getSignature()).isEqualTo(dto.getSignature());
        assertThat(beforeSignature).isNotEqualTo(dto.getSignature());
    }
    
    @SneakyThrows({InterruptedException.class, MessagingException.class})
    @Test
    @DisplayName("정보 수정, 미인증 상태")
    void update_unauth() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        Long          id              = member.getId();
        String        authToken       = member.getAuthInfo().getEmailAuthToken();
        LocalDateTime expireTime      = member.getAuthInfo().getEmailAuthExpireTime();
        String        beforeNickname  = member.getNickname();
        String        beforeSignature = member.getSignature();
        
        MemberUpdateRequestDto dto = new MemberUpdateRequestDto(id, "Password1!@", "user2", "Signature",
                                                                "Password2!@", "Password2!@");
        
        //When
        Thread.sleep(1000);
        clear();
        
        //Then
        assertThatThrownBy(() -> memberService.update(dto))
                .isInstanceOf(InvalidValueException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_NOT_COMPLETED)
                .hasMessage("Email is not verified.");
        
        greenMailExtension.waitForIncomingEmail(5000, 1);
        Member        afterMember      = em.find(Member.class, id);
        MimeMessage[] receivedMessages = greenMailExtension.getReceivedMessages();
        
        //인증되지 않은 계정에 정보 수정을 시도하면 정보는 수정되지 않고 인증 메일이 전송됨
        assertThat(afterMember.getAuthInfo().isAuth()).isFalse();
        assertThat(afterMember.getAuthInfo().getEmailAuthToken()).isNotEqualTo(authToken);
        assertThat(afterMember.getAuthInfo().getEmailAuthExpireTime()).isNotEqualTo(expireTime);
        
        assertThat(passwordEncoder.matches(dto.getNewPassword(), afterMember.getPassword())).isFalse();
        assertThat(passwordEncoder.matches(dto.getPassword(), afterMember.getPassword())).isTrue();
        assertThat(afterMember.getNickname()).isNotEqualTo(dto.getNickname());
        assertThat(beforeNickname).isEqualTo(afterMember.getNickname());
        assertThat(afterMember.getSignature()).isNotEqualTo(dto.getSignature());
        assertThat(beforeSignature).isEqualTo(afterMember.getSignature());
        
        assertThat(receivedMessages).hasSize(1);
        assertThat(afterMember.getUsername())
                .isEqualTo(receivedMessages[0].getRecipients(Message.RecipientType.TO)[0].toString());
    }
    
    @Test
    @DisplayName("정보 수정, 비밀번호 불일치")
    void update_wrongPassword() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        Long id = member.getId();
        member.authenticate();  //이메일 인증
        String beforeNickname  = member.getNickname();
        String beforeSignature = member.getSignature();
        
        MemberUpdateRequestDto dto = new MemberUpdateRequestDto(id, "Password2!@", "user2", "Signature",
                                                                "Password2!@", "Password2!@");
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> memberService.update(dto))
                .isInstanceOf(InvalidValueException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_DO_NOT_MATCH)
                .hasMessage("Invalid input value, Password do not match.");
        
        Member afterMember = em.find(Member.class, id);
        
        assertThat(passwordEncoder.matches(dto.getNewPassword(), afterMember.getPassword())).isFalse();
        assertThat(afterMember.getNickname()).isNotEqualTo(dto.getNickname());
        assertThat(beforeNickname).isEqualTo(afterMember.getNickname());
        assertThat(afterMember.getSignature()).isNotEqualTo(dto.getSignature());
        assertThat(beforeSignature).isEqualTo(afterMember.getSignature());
    }
    
    @Test
    @DisplayName("정보 수정, 닉네임 중복")
    void update_duplicateNickname() {
        //Given
        List<Member> members = dummy.createMembers(2);
        for (Member member : members)
            em.persist(member);
        Member member1 = members.get(0);
        Member member2 = members.get(1);
        Long   id      = member1.getId();
        member1.authenticate(); //이메일 인증
        member2.authenticate(); //이메일 인증
        String beforeNickname  = member1.getNickname();
        String beforeSignature = member1.getSignature();
        
        MemberUpdateRequestDto dto = new MemberUpdateRequestDto(id, "Password1!@", "user2", "Signature",
                                                                "Password2!@", "Password2!@");
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> memberService.update(dto))
                .isInstanceOf(InvalidValueException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NICKNAME_DUPLICATION)
                .hasMessage("Invalid input value: " + dto.getNickname());
        
        Member afterMember = em.find(Member.class, id);
        
        assertThat(passwordEncoder.matches(dto.getNewPassword(), afterMember.getPassword())).isFalse();
        assertThat(afterMember.getNickname()).isNotEqualTo(dto.getNickname());
        assertThat(beforeNickname).isEqualTo(afterMember.getNickname());
        assertThat(afterMember.getSignature()).isNotEqualTo(dto.getSignature());
        assertThat(beforeSignature).isEqualTo(afterMember.getSignature());
    }
    
    @Test
    @DisplayName("회원 탈퇴")
    void delete() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        Long   id       = member.getId();
        String password = "Password1!@";
        
        //When
        memberService.delete(id, password);
        clear();
        
        //Then
        assertThat(em.find(Member.class, id).isDeleteYn()).isTrue();
    }
    
    @Test
    @DisplayName("회원 탈퇴, 존재하지 않는 계정")
    void delete_unknown() {
        //Given
        Long   id       = new Random().nextLong();
        String password = "Password1!@";
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> memberService.delete(id, password))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Member' by id: " + id);
    }
    
    @Test
    @DisplayName("회원 탈퇴, 비밀번호 불일치")
    void delete_wrongPassword() {
        //Given
        Member member = dummy.createMembers(1).get(0);
        em.persist(member);
        Long   id       = member.getId();
        String password = "Password2!@";
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> memberService.delete(id, password))
                .isInstanceOf(InvalidValueException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_DO_NOT_MATCH)
                .hasMessage("Invalid input value, Password do not match.");
    }
    
}