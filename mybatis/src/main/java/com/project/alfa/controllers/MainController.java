package com.project.alfa.controllers;

import com.project.alfa.services.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
@Tag(name = "Main API", description = "Main API 입니다.")
public class MainController {
    
    private final MemberService memberService;
    
    @GetMapping("/")
    @ResponseBody
    @Tag(name = "Main API")
    @Operation(summary = "메인 페이지", description = "메인 페이지를 출력합니다.")
    public ResponseEntity<String> mainPage() {
        return ResponseEntity.ok("Main page.");
    }
    
    @GetMapping("/verify-email")
    @ResponseBody
    @Tag(name = "Main API")
    @Operation(summary = "이메일 인증", description = "이메일 인증을 수행합니다.")
    public ResponseEntity<String> verifyEmail(@RequestParam String email,
                                              @RequestParam String authToken,
                                              HttpServletRequest request) {
        String        header      = request.getHeader("Date");
        LocalDateTime requestTime = null;
        if (header == null)
            requestTime = LocalDateTime.now();
        else
            requestTime = LocalDateTime.parse(header, DateTimeFormatter.RFC_1123_DATE_TIME);
        memberService.verifyEmailAuth(email, authToken, requestTime);
        return ResponseEntity.ok("Email verified successfully.");
    }
    
}
