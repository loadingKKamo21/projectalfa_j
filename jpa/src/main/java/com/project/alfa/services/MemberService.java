package com.project.alfa.services;

import com.project.alfa.aop.annotation.LockAop;
import com.project.alfa.entities.AuthInfo;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Role;
import com.project.alfa.error.exception.EntityNotFoundException;
import com.project.alfa.error.exception.ErrorCode;
import com.project.alfa.error.exception.InvalidValueException;
import com.project.alfa.repositories.v1.MemberRepositoryV1;
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
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    
    private final MemberRepositoryV1 memberRepository;
    //private final MemberRepositoryV2 memberRepository;
    //private final MemberRepositoryV3 memberRepository;
    private final PasswordEncoder    passwordEncoder;
    private final EmailSender        emailSender;
    
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
        if (memberRepository.existsByUsername(dto.getUsername().toLowerCase()))
            throw new InvalidValueException("Invalid input value: " + dto.getUsername(),
                                            ErrorCode.USERNAME_DUPLICATION);
        //닉네임 중복 확인
        if (memberRepository.existsByNickname(dto.getNickname()))
            throw new InvalidValueException("Invalid input value: " + dto.getNickname(),
                                            ErrorCode.NICKNAME_DUPLICATION);
        
        Member member = Member.builder()
                              .username(dto.getUsername().toLowerCase())
                              .password(passwordEncoder.encode(dto.getPassword()))
                              .authInfo(AuthInfo.builder()
                                                .emailAuthToken(UUID.randomUUID().toString())
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
        
        Optional<Member> optionalMember = memberRepository.authenticateEmail(username.toLowerCase(), authToken,
                                                                             authTime);
        
        if (!optionalMember.isPresent() || member != optionalMember.get())
            //인증이 완료되지 않은 경우: 토큰 불일치 또는 인증 만료 제한 시간 초과
            resendVerifyEmail(username);
        else if (optionalMember.get() == member)
            //인증 정보가 일치하는 경우
            member.authenticate();
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
        //새로운 인증 토큰 설정
        member.updateEmailAuthToken(UUID.randomUUID().toString());
        
        //인증 메일 재전송
        emailSender.sendVerificationEmail(username,
                                          member.getAuthInfo().getEmailAuthToken(),
                                          member.getAuthInfo().getEmailAuthExpireTime());
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
        if (!member.getAuthInfo().isAuth()) {
            resendVerifyEmail(username);
            throw new InvalidValueException("Email is not verified.", ErrorCode.AUTH_NOT_COMPLETED);
        }
        
        //임시 비밀번호 생성 및 반영
        String tempPassword = RandomGenerator.randomPassword(20);
        member.updatePassword(passwordEncoder.encode(tempPassword));
        
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
        
        //이메일 인증 여부 확인
        if (!member.getAuthInfo().isAuth()) {
            resendVerifyEmail(member.getUsername());
            throw new InvalidValueException("Email is not verified.", ErrorCode.AUTH_NOT_COMPLETED);
        }
        
        //비밀번호 확인
        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword()))
            throw new InvalidValueException("Invalid input value, Password do not match.",
                                            ErrorCode.PASSWORD_DO_NOT_MATCH);
        
        //닉네임 변경
        if (!member.getNickname().equals(dto.getNickname())) {
            //변경할 닉네임 중복 확인
            if (memberRepository.existsByNickname(dto.getNickname()))
                throw new InvalidValueException("Invalid input value: " + dto.getNickname(),
                                                ErrorCode.NICKNAME_DUPLICATION);
            member.updateNickname(dto.getNickname());
        }
        
        //서명 변경
        if (member.getSignature() == null) {
            if (dto.getSignature() != null && !dto.getSignature().trim().isEmpty())
                member.updateSignature(dto.getSignature());
        } else if (!member.getSignature().equals(dto.getSignature()))
            member.updateSignature(dto.getSignature());
        
        //비밀번호 변경
        if ((dto.getNewPassword() != null && !dto.getNewPassword().trim().isEmpty()) &&
            !passwordEncoder.matches(dto.getNewPassword(), member.getPassword())) {
            //신규 비밀번호 확인
            if (!dto.getNewPassword().equals(dto.getRepeatNewPassword()))
                throw new InvalidValueException("Invalid input value, New password do not match.",
                                                ErrorCode.PASSWORD_DO_NOT_MATCH);
            member.updatePassword(passwordEncoder.encode(dto.getNewPassword()));
        }
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
        
        member.isDelete(true);
    }
    
}
