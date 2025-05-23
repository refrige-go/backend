package com.ohgiraffers.refrigegobackend.user.jwt;

import com.ohgiraffers.refrigegobackend.user.dto.CustomUserDetails;
import com.ohgiraffers.refrigegobackend.user.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // JWT 검증을 건너뛸 경로들
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/login",
            "/",
            "/join",
            "/user-ingredients",
            "/ingredients",
            "/api/recipes",
            "/api/recipe",
            "/api/recommendations"
    );


    public JWTFilter(JWTUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();
        return EXCLUDED_PATHS.stream().anyMatch(requestURI::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //request에서 Authorization 헤더를 찾음
        String authorization = request.getHeader("Authorization");

        //Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            System.out.println("token null for path: " + request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.split(" ")[1];

        //토큰 소멸 시간 검증
        if (jwtUtil.isExpired(token)) {
            System.out.println("token expired");
            filterChain.doFilter(request, response);
            return;
        }

        String loginId = jwtUtil.getLoginId(token);

        if (loginId == null) {
            System.out.println("loginId not found in token claims for path: " + request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);

            if (userDetails != null && userDetails instanceof CustomUserDetails) {
                Authentication authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);

            } else {
                System.out.println("User details not found or invalid type for loginId: " + loginId + " for path: " + request.getRequestURI());
            }

        } catch (Exception e) {
            System.out.println("Error loading user from DB for loginId: " + loginId + " for path: " + request.getRequestURI() + ". Error: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
