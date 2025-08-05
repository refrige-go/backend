package com.ohgiraffers.refrigegobackend.user.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.refrigegobackend.user.entity.User;
import com.ohgiraffers.refrigegobackend.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AdminLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    public AdminLoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, UserRepository userRepository){
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        setFilterProcessesUrl("/admin/login"); //어드민 페이지 로그인 전용
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException{
        try{
            User user = new ObjectMapper().readValue(request.getInputStream(), User.class);
            log.debug("관리자 로그인 시도 - 사용자: {}, IP: {}", user.getUsername(), request.getRemoteAddr());
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(),user.getPassword(),null)
            );
        }catch (IOException e){
            log.error("관리자 로그인 시도 중 IOException 발생: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
        protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
            String username = authResult.getName();
            String role = authResult.getAuthorities().iterator().next().getAuthority();

            log.info("=== 관리자 인증 성공 - 사용자: {}, 권한: {}, IP: {} ===",
                    username, role, request.getRemoteAddr());

            //관리자 권한 확인
            if (!"ROLE_ADMIN".equals(role)){
                log.warn("=== 관리자 로그인 실패: {} (권한: {}) ===", username, role);
                log.warn("=== 시간: {} ===", java.time.LocalDateTime.now());
                log.warn("=== IP: {} ===", request.getRemoteAddr());
                log.warn("=== User-Agent: {} ===", request.getHeader("User-Agent"));

                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"관리자 권한이 필요합니다.\"}");

                return;
            }

            //실제 닉네임 가져오기
        User user = userRepository.findByUsernameAndDeletedFalse(username);
            if(user == null){
                log.error("관리자 로그인 실패 - 사용자를 찾을 수 없음: {}", username);
                throw new RuntimeException("사용자를 찾을 수 없습니다.");
            }

            // JWT 토큰 생성
            String access = jwtUtil.createJwt("access", username, role, 3600000L); //1시간
            String refresh = jwtUtil.createJwt("refresh",username,role,86400000L);


            log.info("관리자 JWT 토큰 생성 완료 - Access Token: {}, Refresh Token: {}",
                    access.substring(0, Math.min(20, access.length())) + "...",
                    refresh.substring(0, Math.min(20, refresh.length())) + "...");

            //응답 헤더에 토큰 설정
            response.addHeader("Authorization", "Bearer " + access);
            response.addHeader("Refresh", refresh);

            //응답 본문에 사용자 정보 포함
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("username", username);
            responseBody.put("nickname",user.getNickname());
            responseBody.put("role",role);
            responseBody.put("message","관리자 로그인 성공");

            response.setContentType("application/json;charset=UTF-8");
            new ObjectMapper().writeValue(response.getOutputStream(),responseBody);

        log.info("=== 관리자 로그인 성공: {} (닉네임: {}, 권한: {}, IP: {}) ===",
                username, user.getNickname(), role, request.getRemoteAddr());
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException{
        log.warn("관리자 로그인 실패 - IP: {}, User-Agent: {}, 오류: {}",
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                failed.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"아이디 또는 비밀번호가 올바르지 않습니다.\"}");
    }
}
