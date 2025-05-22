package com.ohgiraffers.refrigegobackend.user.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.refrigegobackend.user.dto.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        // 로그인 URL 설정
        setFilterProcessesUrl("/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = "";
        String password = "";

        try {
            // Content-Type이 application/json인 경우
            if (request.getContentType() != null && request.getContentType().contains("application/json")) {
                // JSON에서 username과 password 추출
                Map<String, String> jsonData = objectMapper.readValue(request.getInputStream(), Map.class);
                username = jsonData.get("username");
                password = jsonData.get("password");
                System.out.println("JSON Login attempt - username: " + username);
            } else {
                // Form 데이터에서 username과 password 추출
                username = obtainUsername(request);
                password = obtainPassword(request);
                System.out.println("Form Login attempt - username: " + username);
            }
        } catch (IOException e) {
            System.out.println("Login request parsing error: " + e.getMessage());
            throw new AuthenticationException("Authentication failed: " + e.getMessage()) {};
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);
        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String username = customUserDetails.getUsername();
        Long userId = customUserDetails.getId();  // userId 추가

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String token = jwtUtil.createJwt(username, role, userId, 60*60*10L * 1000);  // userId 추가
        
        // CORS 대응을 위한 헤더 추가
        response.addHeader("Access-Control-Expose-Headers", "Authorization");
        response.addHeader("Authorization", "Bearer " + token);
        
        // 응답 본문에도 토큰 추가 (프론트엔드에서 쉽게 접근할 수 있도록)
        response.setContentType("application/json");
        response.getWriter().write("{\"token\":\"" + token + "\", \"userId\":" + userId + "}");
        
        System.out.println("Login successful for user: " + username + " (ID: " + userId + ")");
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        System.out.println("Login failed: " + failed.getMessage());
        response.setStatus(401);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Authentication failed: " + failed.getMessage() + "\"}");
    }
}
