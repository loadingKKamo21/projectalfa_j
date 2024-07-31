package com.project.alfa.controllers.api;

import com.google.gson.Gson;
import com.project.alfa.error.ErrorResponse;
import com.project.alfa.error.exception.ErrorCode;
import com.project.alfa.security.CustomUserDetails;
import com.project.alfa.services.MemberService;
import com.project.alfa.services.dto.MemberJoinRequestDto;
import com.project.alfa.services.dto.MemberUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/members",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Member API", description = "회원 API 입니다.")
public class MemberApiController {
    
    private final MemberService memberService;
    
    /**
     * GET: 회원 가입 페이지
     *
     * @return
     */
    @GetMapping
    @Tag(name = "Member API")
    @Operation(summary = "회원 가입 페이지", description = "회원 가입 페이지를 출력합니다.")
    public ResponseEntity<String> joinPage() {
        return ResponseEntity.ok(new Gson().toJson(new MemberJoinRequestDto()));
    }
    
    /**
     * POST: 회원 가입
     *
     * @param params - 회원 가입 정보 DTO
     * @return
     */
    @PostMapping
    @Tag(name = "Member API")
    @Operation(summary = "회원 가입", description = "회원 가입을 수행합니다.")
    public ResponseEntity<String> join(@Valid @RequestBody final MemberJoinRequestDto params) {
        memberService.join(params);
        return ResponseEntity.ok("Member joined successfully.");
    }
    
    /**
     * POST: 비밀번호 찾기
     *
     * @param username - 아이디(이메일)
     * @return
     */
    @PostMapping("/forgot-password")
    @Tag(name = "Member API")
    @Operation(summary = "비밀번호 찾기", description = "비밀번호 찾기를 수행합니다.")
    public ResponseEntity<String> forgotPassword(@RequestBody final String username) {
        //입력된 아이디(이메일) 검사
        if (!username.matches("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$"))
            return new ResponseEntity<>(
                    new Gson().toJson(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE)),
                    HttpStatus.BAD_REQUEST);
        memberService.findPassword(username);
        return ResponseEntity.ok("Successfully sending of \"Find password\" email.");
    }
    
    /**
     * GET: 프로필 조회 페이지
     *
     * @param userDetails
     * @return
     */
    @GetMapping("/profile")
    @Tag(name = "Member API")
    @Operation(summary = "프로필 조회", description = "프로필 조회 페이지를 출력합니다.")
    public ResponseEntity<String> profilePage(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(new Gson().toJson(memberService.findByUsername(userDetails.getUsername())));
    }
    
    /**
     * GET: 프로필 수정 페이지
     *
     * @param userDetails
     * @return
     */
    @GetMapping("/profile-update")
    @Tag(name = "Member API")
    @Operation(summary = "프로필 수정 페이지", description = "프로필 수정 페이지를 출력합니다.")
    public ResponseEntity<String> profileUpdatePage(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> map = new HashMap<>();
        map.put("member", memberService.findByUsername(userDetails.getUsername()));
        map.put("form", new MemberUpdateRequestDto());
        return new ResponseEntity<>(new Gson().toJson(map), HttpStatus.OK);
    }
    
    /**
     * POST: 프로필 수정
     *
     * @param userDetails
     * @param params      - 계정 수정 정보 DTO
     * @return
     */
    @PostMapping("/profile-update")
    @Tag(name = "Member API")
    @Operation(summary = "프로필 수정", description = "프로필 수정을 수행합니다.")
    public ResponseEntity<String> profileUpdate(@AuthenticationPrincipal UserDetails userDetails,
                                                @Valid @RequestBody final MemberUpdateRequestDto params) {
        //로그인 정보(UserDetails)의 ID와 계정 수정 정보(MemberUpdateRequestDto)의 ID 비교
        if (params.getId().equals(((CustomUserDetails) userDetails).getId())) {
            memberService.update(params);
            return ResponseEntity.ok("Member updated successfully.");
        } else
            return new ResponseEntity<>("Member update denied.", HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * POST: 회원 탈퇴
     *
     * @param userDetails
     * @param params      - 회원 탈퇴 정보 DTO
     * @return
     */
    @PostMapping("/delete")
    @Tag(name = "Member API")
    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 수행합니다.")
    public ResponseEntity<String> deleteAccount(@AuthenticationPrincipal UserDetails userDetails,
                                                @RequestBody final MemberUpdateRequestDto params) {
        //로그인 정보(UserDetails)의 ID와 계정 탈퇴 정보(MemberUpdateRequestDto)의 ID 비교
        if (params.getId().equals(((CustomUserDetails) userDetails).getId())) {
            memberService.delete(params.getId(), params.getPassword());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Location", "/logout");
            return new ResponseEntity<>("Member deleted successfully.", httpHeaders, HttpStatus.FOUND);
        } else
            return new ResponseEntity<>("Member delete denied.", HttpStatus.UNAUTHORIZED);
    }
    
}
