package com.ohgiraffers.refrigegobackend.user.jwt;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.refrigegobackend.user.dto.LoginDTO;
import com.ohgiraffers.refrigegobackend.user.entity.RefreshToken;
import com.ohgiraffers.refrigegobackend.user.repository.RefreshRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;


// login 요청을 가로채서 username/password를 검증하고, 성공 시 JWT를 발급하는 필터
// JWT 인증의 시작점
// 사용자가 /login 주소로 아이디/비밀번호를 보낼 때, 이 필터가 그 요청을 가로채서 처리
// 인증이 성공하면 JWT 토큰을 발급해서 응답에 실어줌

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private RefreshRepository refreshRepository;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, RefreshRepository refreshRepository) {

        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    // 메서드는 사용자가 /login으로 보낸 요청에서 아이디/비밀번호를 꺼내고,
    // AuthenticationManager에게 인증 요청을 보냄

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        final LoginDTO loginDTO;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ServletInputStream inputStream = request.getInputStream();
            String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            loginDTO = objectMapper.readValue(messageBody, LoginDTO.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        System.out.println(loginDTO.getUsername());

        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        System.out.println("프론트로 부터 받은 유저 아이디"+username);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

        return authenticationManager.authenticate(authToken);
    }
    // 토큰을 생성해서 authenticate메서드에 전달, authenticate내부에서 아래와같은 과정이 일어남
        /* 1. CustomUserDetailsService.loadUserByUsername(username)
          2. UserDetails와 입력한 password를 비교
          3. 성공하면 Authentication 객체 반환
          4. 실패하면 AuthenticationException 발생 */

    //인증이 성공했을 때 JWT 토큰을 만들어서 응답 헤더에 추가하는 곳
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {

        //유저 정보
        String username = authentication.getName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        //토큰 생성
        String access = jwtUtil.createJwt("access", username, role, 600000L); //10분
        String refresh = jwtUtil.createJwt("refresh", username, role, 86400000L); //24시간

        //Refresh 토큰 저장 메서드 (아래에 있음)
        addRefreshEntity(username, refresh, 86400000L);

        // 응답 설정
        response.setHeader("Authorization", "Bearer " + access);
        response.addCookie(createCookie("refresh", refresh));
        response.setStatus(HttpStatus.OK.value());


        System.out.println("access 토큰과 refresh 토큰 생성 후 저장");


    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {

        response.setStatus(401);
        // 실패했을때에는 401을 날림
    }

    // 리프레시 토큰을 저장소에 저장하는 메서드
    // !! 계속 리프레시 토큰을 저장하면 데이터베이스에 쌓이게되므로 일정 시간이 지난 토큰은 자동으로 삭제되게끔 구현을 추가하기!!
    private void addRefreshEntity(String username, String refresh, Long expiredMs) {

        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsername(username);
        refreshToken.setRefresh(refresh);
        refreshToken.setExpiration(date.toString());

        refreshRepository.save(refreshToken);
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60); // 쿠키가 브라우저에 저장될 시간 (24시간), 이 설정이 없으면 브라우저 종료시 삭제되게
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true); // XSS 공격으로부터 보호

        return cookie;
    }
}
