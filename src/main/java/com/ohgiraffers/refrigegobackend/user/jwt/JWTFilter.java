package com.ohgiraffers.refrigegobackend.user.jwt;

import com.ohgiraffers.refrigegobackend.user.dto.CustomUserDetails;
import com.ohgiraffers.refrigegobackend.user.entity.Role;
import com.ohgiraffers.refrigegobackend.user.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
/*
* 1. 로그인 시 → LoginFilter에서 JWT를 발급
2. 이후 요청마다 → 클라이언트는 JWT를 Authorization 헤더에 실어 보냄
3. 서버는 JWTFilter에서 이 토큰을 검사함 ✅ (지금 보는 코드!)
4. 유효하면 → Spring Security에 인증 객체 등록
5. 컨트롤러에서는 "인증된 사용자"로 인식됨
*
* */
// JWT 토큰 검증 필터
// 사용자가 API 요청을 보낼 때 Authorization 헤더에 담긴 JWT 토큰을 검증하고, 인증된 사용자 정보를 SecurityContext에 등록하는 필터

// 요청당 한 번만 실행되는 Spring Security 필터

@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {

        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Authorization 헤더에서 access 토큰을 꺼내는 코드
        String header = request.getHeader("Authorization");

        log.debug("=== JWTFilter 토큰 검증 시작 ===");
        log.debug("원본 Authorization 헤더: '{}'", header);

        if (header == null || !header.startsWith("Bearer ")) {
            log.debug("Authorization 헤더가 없거나 Bearer로 시작하지 않음 - 필터 스킵");
            filterChain.doFilter(request, response);
            return;
        }
        String accessToken = header.substring(7); // "Bearer " 이후 토큰 값만 추출
        log.debug("추출된 토큰 (정리 전): '{}' (길이: {})",
                accessToken.substring(0, Math.min(20, accessToken.length())) + "...",
                accessToken.length());

        // 중복 "Bearer " 제거
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
            log.debug("중복 Bearer 제거 후: '{}' (길이: {})",
                    accessToken.substring(0, Math.min(20, accessToken.length())) + "...",
                    accessToken.length());
        }

        // 토큰 정리 (제어 문자 제거)
        String cleanToken = accessToken.replaceAll("[\\x00-\\x1F\\x7F-\\x9F]", "").trim();
        log.debug("정리된 토큰: '{}' (길이: {})",
                cleanToken.substring(0, Math.min(20, cleanToken.length())) + "...",
                cleanToken.length());

        // 토큰 만료 여부 확인, 만료시 다음 필터로 넘기지 않음
        try {
            jwtUtil.isExpired(cleanToken);
        } catch (ExpiredJwtException e) {
            log.warn("토큰 만료: {}", e.getMessage());

            // JSON 응답으로 변경
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            PrintWriter writer = response.getWriter();
            writer.print("{\"error\":\"토큰이 만료되었습니다.\",\"code\":\"TOKEN_EXPIRED\"}");
            return;
        } catch (Exception e) {
            log.warn("JWT 토큰 파싱 오류: {}", e.getMessage());

            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            PrintWriter writer = response.getWriter();
            writer.print("{\"error\":\"유효하지 않은 토큰입니다.\",\"code\":\"INVALID_TOKEN\"}");
            return;
        }

        // 토큰이 access인지 확인 (발급시 페이로드에 명시)
        String category;
        try {
            category = jwtUtil.getCategory(cleanToken); // cleanToken 사용
        } catch (Exception e) {
            log.warn("카테고리 추출 오류: {}", e.getMessage());

            PrintWriter writer = response.getWriter();
            writer.print("토큰 형식이 올바르지 않습니다");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (!category.equals("access")) {
            log.warn("잘못된 토큰 카테고리: {}", category);

            // response body
            PrintWriter writer = response.getWriter();
            writer.print("access token이 아닙니다");

            // response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // username, role 값을 획득
        String username;
        String role;

        try {
            username = jwtUtil.getUsername(cleanToken); // cleanToken 사용
            role = jwtUtil.getRole(cleanToken); // cleanToken 사용

            log.info("토큰 검증 성공 - username: {}, role: {}, category: {}", username, role, category);
        } catch (Exception e) {
            log.error("사용자 정보 추출 오류: {}", e.getMessage(), e);

            PrintWriter writer = response.getWriter();
            writer.print("토큰에서 사용자 정보를 추출할 수 없습니다");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setRole(role);
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null,
                customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        log.debug("=== JWTFilter 토큰 검증 완료 ===");
        filterChain.doFilter(request, response);
    }
}

/*
 * JWTFilter는 사용자의 JWT를 검사하고, 인증된 사용자 정보를 Spring Security에 등록해서
 * 로그인하지 않아도 사용자 정보를 기억하고 보호된 API에 접근 가능하게 해주는 필터
 *
 *
 */
