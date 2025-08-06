package com.ohgiraffers.refrigegobackend.user.jwt;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

//JWT 토큰을 만들고, 파싱하고, 검증하는 도구 클래스

/*
 *JWT를 다루는 핵심 도구
 * createJwt()	토큰을 새로 발급함 (username, role, 만료시간)
 * getUsername()	토큰에서 사용자 이름을 추출
 * getRole()	토큰에서 사용자 역할을 추출
 * isExpired()	토큰이 만료되었는지 검사
 *
 * JWT는 보통 로그인 후 발급되고, 이후 인증은 JWT로만 처리
 * */

@Slf4j
@Component
public class JWTUtil {

    private SecretKey secretKey;
    // 생성자 - 비밀키 설정
    public JWTUtil(@Value("${spring.jwt.secret}")String secret) {

        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String getUsername(String token) {
        String cleanToken = cleanString(token);
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(cleanToken).getPayload().get("username", String.class);
    }

    public String getRole(String token) {
        String cleanToken = cleanString(token);
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(cleanToken).getPayload().get("role", String.class);
    }

    public String getCategory(String token) {
        String cleanToken = cleanString(token);
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(cleanToken).getPayload().get("category", String.class);
    }

    // isExpired – 만료 여부 검사
    public Boolean isExpired(String token) {
        String cleanToken = cleanString(token);
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(cleanToken).getPayload().getExpiration().before(new Date());
    }

    // 문자열 정리 함수
    private String cleanString(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("[\\x00-\\x1F\\x7F-\\x9F]", "");
    }

    // createJwt – 토큰 발급
    public String createJwt(String category, String username, String role, Long expiredMs) {

        log.debug("=== JWT 생성 시작 ===");
        log.debug("원본 category: '{}' (길이: {})", category, category != null ? category.length() : 0);
        log.debug("원본 username: '{}' (길이: {})", username, username != null ? username.length() : 0);
        log.debug("원본 role: '{}' (길이: {})", role, role != null ? role.length() : 0);

        if ("access".equals(category)) {
            log.info("발급시간: {}", new Date(System.currentTimeMillis()));
            log.info("만료시간: {}", new Date(System.currentTimeMillis() + expiredMs));
        }

        String token = Jwts.builder()
                .claim("category", category)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();

        // 생성된 토큰 확인
        log.debug("생성된 토큰 길이: {}", token.length());
        log.debug("토큰 시작 부분: {}", token.substring(0, Math.min(50, token.length())));
        log.debug("=== JWT 생성 완료 ===");

        return token;
    }
    /*
     * claim("username", username): 사용자 이름을 토큰에 담음
     *   claim("role", role): 역할(권한)을 토큰에 담음
     *   issuedAt: 발급 시간
     *   expiration: 만료 시간
     *   signWith(secretKey): 서명(위조 방지)
     *   .compact(): JWT 문자열로 만들어 반환
     *
     *
     * */
}