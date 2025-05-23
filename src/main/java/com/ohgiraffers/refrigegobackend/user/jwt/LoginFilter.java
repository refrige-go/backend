package com.ohgiraffers.refrigegobackend.user.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.refrigegobackend.user.dto.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(LoginFilter.class);
    private static final long JWT_EXPIRATION_MS = 10 * 60 * 60 * 1000L; // 10시간

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/login"); // 로그인 URL 지정
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = "";
        String password = "";

        try {
            if (request.getContentType() != null && request.getContentType().contains("application/json")) {
                Map<String, String> jsonData = objectMapper.readValue(request.getInputStream(), Map.class);
                username = jsonData.get("username");
                password = jsonData.get("password");
                log.info("JSON Login attempt - username: {}", username);
            } else {
                username = obtainUsername(request);
                password = obtainPassword(request);
                log.info("Form Login attempt - username: {}", username);
            }
        } catch (IOException e) {
            log.error("Login request parsing error", e);
            throw new AuthenticationException("Authentication failed: " + e.getMessage()) {};
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authentication)
            throws IOException, ServletException {

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String username = customUserDetails.getUsername();
        Long userId = customUserDetails.getId();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String role = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        String token = jwtUtil.createJwt(username, role, userId, JWT_EXPIRATION_MS);

        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader("Access-Control-Expose-Headers", "Authorization");
        response.addHeader("Authorization", "Bearer " + token);

        response.setContentType("application/json");
        response.getWriter().write("{\"token\":\"" + token + "\", \"userId\":" + userId + "}");

        log.info("Login successful for user: {} (ID: {})", username, userId);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed)
            throws IOException, ServletException {
        log.warn("Login failed: {}", failed.getMessage());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Authentication failed: " + failed.getMessage() + "\"}");
    }
}
