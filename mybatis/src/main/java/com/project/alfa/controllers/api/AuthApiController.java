package com.project.alfa.controllers.api;

import com.project.alfa.services.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/auth",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthApiController {
    
    private final JwtService jwtService;
    
    /**
     * POST: JWT Access 토큰 Refresh
     *
     * @param request
     * @param response
     * @param body
     * @param userDetails
     * @return
     */
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(HttpServletRequest request,
                                               HttpServletResponse response,
                                               @RequestBody(required = false) final Map<String, String> body,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        String refreshToken = getRefreshToken(request, body);
        if (refreshToken != null && !refreshToken.trim().isEmpty()) {
            String accessToken = jwtService.refreshAccessToken(refreshToken, userDetails);
            refreshToken = jwtService.generateRefreshToken(userDetails);
            response.setHeader("Authorization", "Bearer " + accessToken);
            
            //1. RefreshToken 쿠키로 전달
            Cookie cookie = new Cookie("refreshToken", refreshToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge((int) jwtService.getExpirationFromToken(refreshToken));
            response.addCookie(cookie);
            
            return ResponseEntity.ok("Access Token refresh complete.");
            
            //2. RefreshToken JSON으로 전달
//            return ResponseEntity.ok(new Gson().toJson(refreshToken));
        }
        return ResponseEntity.badRequest().body("Refresh Token is missing.");
    }
    
    /**
     * JWT Refresh 토큰 추출
     *
     * @param request
     * @param body
     * @return JWT Refresh 토큰
     */
    private String getRefreshToken(HttpServletRequest request, final Map<String, String> body) {
        //1. 쿠키에서 RefreshToken 추출
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies)
                if (cookie.getName().equals("refreshToken"))
                    return cookie.getValue();
        
        //2. 헤더에서 RefreshToken 추출
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Refresh "))
            return header.substring(8);
        
        //3. JSON에서 RefreshToken 추출
        if (body != null)
            return body.get("refreshToken");
        
        return null;
    }
    
}
