package com.project.alfa.controllers;

import com.project.alfa.services.MemberService;
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
public class MainController {
    
    private final MemberService memberService;
    
    @GetMapping("/")
    @ResponseBody
    public ResponseEntity<String> mainPage() {
        return ResponseEntity.ok("Main page.");
    }
    
    @GetMapping("/verify-email")
    @ResponseBody
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
