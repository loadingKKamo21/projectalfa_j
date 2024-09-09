package com.project.alfa.services;

import com.project.alfa.aop.annotation.LockAop;
import com.project.alfa.entities.AuthInfo;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Role;
import com.project.alfa.error.exception.EntityNotFoundException;
import com.project.alfa.error.exception.ErrorCode;
import com.project.alfa.error.exception.InvalidValueException;
import com.project.alfa.repositories.MemberRepository;
import com.project.alfa.services.dto.MemberInfoResponseDto;
import com.project.alfa.services.dto.MemberJoinRequestDto;
import com.project.alfa.services.dto.MemberUpdateRequestDto;
import com.project.alfa.utils.EmailSender;
import com.project.alfa.utils.RandomGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    
    private static final Long MAX_EXPIRE_TIME = 5L; //이메일 인증 만료 제한 시간
    
    private final MemberRepository memberRepository;
    private final PasswordEncoder  passwordEncoder;
    private final EmailSender      emailSender;
    
    /**
     * 회원 가입
     *
     * @param dto - 계정 가입 정보 DTO
     * @return PK
     */
    @Transactional
    public Long join(final MemberJoinRequestDto dto) {
        //비밀번호 확인
        if (!dto.getPassword().equals(dto.getRepeatPassword()))
            throw new InvalidValueException("Invalid input value, Password do not match.",
                                            ErrorCode.PASSWORD_DO_NOT_MATCH);
        //아이디(이메일) 중복 확인
        if (usernameDuplicationCheck(dto.getUsername()))
            throw new InvalidValueException("Invalid input value: " + dto.getUsername(),
                                            ErrorCode.USERNAME_DUPLICATION);
        //닉네임 중복 확인
        if (nicknameDuplicationCheck(dto.getNickname()))
            throw new InvalidValueException("Invalid input value: " + dto.getNickname(),
                                            ErrorCode.NICKNAME_DUPLICATION);
        
        Member member = Member.builder()
                              .username(dto.getUsername().toLowerCase())
                              .password(passwordEncoder.encode(dto.getPassword()))
                              .authInfo(AuthInfo.builder()
                                                .emailAuthToken(UUID.randomUUID().toString())
                                                .emailAuthExpireTime(
                                                        LocalDateTime.now().withNano(0).plusMinutes(MAX_EXPIRE_TIME))
                                                .build())
                              .nickname(dto.getNickname())
                              .role(Role.USER)
                              .build();
        
        memberRepository.save(member);
        
        //가입 인증 메일 전송
        emailSender.sendVerificationEmail(member.getUsername(),
                                          member.getAuthInfo().getEmailAuthToken(),
                                          member.getAuthInfo().getEmailAuthExpireTime());
        
        return member.getId();
    }
    
    /**
     * 이메일 인증: 인증 토큰 및 인증 시간 확인 후 미인증 -> 인증 상태로 변경
     *
     * @param username  - 메일 주소
     * @param authToken - 인증 토큰
     * @param authTime  - 인증 시간
     */
    @LockAop
    @Transactional
    public void verifyEmailAuth(final String username, final String authToken, final LocalDateTime authTime) {
        Member member = memberRepository.findByUsername(username.toLowerCase(), false)
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                "Could not found 'Member' by username: " + username));
        
        //이미 인증된 계정인 경우
        if (member.getAuthInfo().isAuth())
            return;
        
        memberRepository.authenticateEmail(username.toLowerCase(), authToken, authTime);
        
        Long id = member.getId();
        //인증이 완료되지 않은 경우: 토큰 불일치 또는 인증 만료 제한 시간 초과
        if (!memberRepository.findById(id, false)
                             .orElseThrow(() -> new EntityNotFoundException("Could not found 'Member' by id: " + id))
                             .getAuthInfo().isAuth())
            resendVerifyEmail(username);
    }
    
    /**
     * 인증 메일 재전송: 새로운 인증 토큰 반영 및 인증 -> 미인증 상태로 변경
     *
     * @param username - 메일 주소
     */
    @LockAop
    @Transactional
    public void resendVerifyEmail(final String username) {
        Member member = memberRepository.findByUsername(username.toLowerCase(), false)
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                "Could not found 'Member' by username: " + username));
        
        String        authToken  = UUID.randomUUID().toString();    //새로운 인증 토큰
        LocalDateTime expireTime = LocalDateTime.now().withNano(0).plusMinutes(MAX_EXPIRE_TIME);    //새로운 인증 만료 제한 시간
        
        Member param = Member.builder()
                             .id(member.getId())
                             .authInfo(AuthInfo.builder()
                                               .emailAuthToken(authToken)
                                               .emailAuthExpireTime(expireTime)
                                               .build())
                             .build();
        
        memberRepository.update(param);
        
        //인증 메일 재전송
        emailSender.sendVerificationEmail(username, authToken, expireTime);
    }
    
    /**
     * 비밀번호 찾기: 아이디로 계정 검증 후 임시 비밀번호 변경 및 비밀번호 찾기 결과 메일 전송
     *
     * @param username - 메일 주소
     */
    @LockAop
    @Transactional
    public void findPassword(final String username) {
        Member member = memberRepository.findByUsername(username.toLowerCase(), false)
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                "Could not found 'Member' by username: " + username));
        
        //이메일 인증 여부 확인
        isVerifiedEmail(member.getUsername(), member.getAuthInfo().isAuth());
        
        //임시 비밀번호 생성 및 반영
        String tempPassword = RandomGenerator.randomPassword(20);
        memberRepository.update(Member.builder()
                                      .id(member.getId())
                                      .password(passwordEncoder.encode(tempPassword))
                                      .build());
        
        //비밀번호 찾기 결과 메일 전송
        emailSender.sendPasswordResetEmail(member.getUsername(), tempPassword);
    }
    
    /**
     * 계정 정보 조회
     *
     * @param id - PK
     * @return 계정 정보 DTO
     */
    public MemberInfoResponseDto findById(final Long id) {
        return new MemberInfoResponseDto(memberRepository.findById(id, false)
                                                         .orElseThrow(() -> new EntityNotFoundException(
                                                                 "Could not found 'Member' by id: " + id)));
    }
    
    /**
     * 계정 정보 조회
     *
     * @param username - 아이디(이메일)
     * @return 계정 정보 DTO
     */
    public MemberInfoResponseDto findByUsername(final String username) {
        return new MemberInfoResponseDto(memberRepository.findByUsername(username.toLowerCase(), false)
                                                         .orElseThrow(() -> new EntityNotFoundException(
                                                                 "Could not found 'Member' by username: " + username)));
    }
    
    /**
     * 계정 정보 수정
     *
     * @param dto - 계정 수정 정보 DTO
     */
    @LockAop
    @Transactional
    public void update(final MemberUpdateRequestDto dto) {
        Member member = memberRepository.findById(dto.getId(), false)
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                "Could not found 'Member' by id: " + dto.getId()));
        boolean flag = false;
        
        //이메일 인증 여부 확인
        isVerifiedEmail(member.getUsername(), member.getAuthInfo().isAuth());
        
        //비밀번호 확인
        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword()))
            throw new InvalidValueException("Invalid input value, Password do not match.",
                                            ErrorCode.PASSWORD_DO_NOT_MATCH);
        
        Member.MemberBuilder paramBuilder = Member.builder();
        paramBuilder.id(dto.getId());
        
        //닉네임 변경
        if (!member.getNickname().equals(dto.getNickname())) {
            //변경할 닉네임 중복 확인
            if (nicknameDuplicationCheck(dto.getNickname()))
                throw new InvalidValueException(
                        "Invalid input value: " + dto.getNickname(), ErrorCode.NICKNAME_DUPLICATION);
            flag = true;
            paramBuilder.nickname(dto.getNickname());
        }
        
        //서명 변경
        if (member.getSignature() == null) {
            if (dto.getSignature() != null && !dto.getSignature().trim().isEmpty()) {
                flag = true;
                paramBuilder.signature(dto.getSignature());
            }
        } else if (!member.getSignature().equals(dto.getSignature())) {
            flag = true;
            paramBuilder.signature(dto.getSignature());
        }
        
        //비밀번호 변경
        if ((dto.getNewPassword() != null && !dto.getNewPassword().trim().isEmpty())
            && !passwordEncoder.matches(dto.getNewPassword(), member.getPassword())) {
            //신규 비밀번호 확인
            if (!dto.getNewPassword().equals(dto.getRepeatNewPassword()))
                throw new InvalidValueException("Invalid input value, New password do not match.",
                                                ErrorCode.PASSWORD_DO_NOT_MATCH);
            flag = true;
            paramBuilder.password(passwordEncoder.encode(dto.getNewPassword()));
        }
        
        Member param = paramBuilder.build();
        
        //변경될 값이 있는 지 확인
        if (flag)
            memberRepository.update(param);
    }
    
    /**
     * 회원 탈퇴
     *
     * @param id       - PK
     * @param password - 비밀번호
     */
    @LockAop
    @Transactional
    public void delete(final Long id, final String password) {
        Member member = memberRepository.findById(id, false)
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                "Could not found 'Member' by id: " + id));
        
        //비밀번호 확인
        if (!passwordEncoder.matches(password, member.getPassword()))
            throw new InvalidValueException("Invalid input value, Password do not match.",
                                            ErrorCode.PASSWORD_DO_NOT_MATCH);
        
        memberRepository.deleteById(member.getId());
    }
    
    //==================== 검증 메서드 ====================//
    
    /**
     * 아이디(이메일) 중복 확인
     *
     * @param inputUsername - 중복 확인할 아이디
     * @return 중복 여부
     */
    private boolean usernameDuplicationCheck(final String inputUsername) {
        String username = inputUsername.toLowerCase();
        return (username != null && !username.trim().isEmpty()) && memberRepository.existsByUsername(username);
    }
    
    /**
     * 닉네임 중복 확인
     *
     * @param inputNickname - 중복 확인할 닉네임
     * @return 중복 여부
     */
    private boolean nicknameDuplicationCheck(final String inputNickname) {
        return (inputNickname != null && !inputNickname.trim().isEmpty()) &&
               memberRepository.existsByNickname(inputNickname);
    }
    
    /**
     * 이메일 인증 여부 확인: 인증 상태 -> return, 미인증 상태 -> 인증 메일 전송 및 RuntimeException
     *
     * @param username   - 메일 주소
     * @param authStatus - 인증 상태
     */
    private void isVerifiedEmail(final String username, final boolean authStatus) {
        if (!authStatus) {
            resendVerifyEmail(username);
            throw new InvalidValueException("Email is not verified.", ErrorCode.AUTH_NOT_COMPLETED);
        }
        return;
    }
    
}
