package com.ohgiraffers.refrigegobackend.user.jwt;

import com.ohgiraffers.refrigegobackend.user.dto.CustomUserDetails;
import com.ohgiraffers.refrigegobackend.user.entity.Role;
import com.ohgiraffers.refrigegobackend.user.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String accessToken = header.substring(7); // "Bearer " 이후 토큰 값만 추출

        // 토큰 만료 여부 확인, 만료시 다음 필터로 넘기지 않음
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {

            // response body
            PrintWriter writer = response.getWriter();
            writer.print("access token이 만료되었습니다");

            // response status code
            // 응답은 401이나 400으로 설정가능
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰이 access인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(accessToken);

        if (!category.equals("access")) {

            // response body
            PrintWriter writer = response.getWriter();
            writer.print("access token이 아닙니다");

            // response status code
            // 응답은 401이나 400으로 설정가능
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // username, role 값을 획득
        String username = jwtUtil.getUsername(accessToken);
        String role = jwtUtil.getRole(accessToken);

        User user = new User();
        user.setUsername(username);
        user.setRole(role);
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null,
                customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}

/*
 * JWTFilter는 사용자의 JWT를 검사하고, 인증된 사용자 정보를 Spring Security에 등록해서
 * 로그인하지 않아도 사용자 정보를 기억하고 보호된 API에 접근 가능하게 해주는 필터
 *
 *
 */
