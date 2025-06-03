package com.ohgiraffers.refrigegobackend.user.controller;



import com.ohgiraffers.refrigegobackend.user.entity.RefreshToken;
import com.ohgiraffers.refrigegobackend.user.jwt.JWTUtil;
import com.ohgiraffers.refrigegobackend.user.repository.RefreshRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Date;

@RestController
public class ReissueController {

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public ReissueController(JWTUtil jwtUtil, RefreshRepository refreshRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        // 여기서는 controller에 작성했지만 service쪽으로 빼기!!
        //get refresh token
        // 내부 쿠키들을 순환하면서 refresh라는 key값을 찾음
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {

            if (cookie.getName().equals("refresh")) {

                refresh = cookie.getValue();
                // 있으면 refresh에 저장
            }
        }

        // refresh 토큰이 없으면 
        if (refresh == null) {

            //response status code
            // 응답은 다르게 지정 할 수있음
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        //expired check
        // 있을때 만료되있는지 검증
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {

            //response status code
            // 만료되었을 때의 응답
            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);

        if (!category.equals("refresh")) {

            //response status code
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }


        //DB에 저장되어 있는지 확인
        Boolean isExist = refreshRepository.existsByRefresh(refresh);
        if (!isExist) {

            //response body
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        // ---토큰 검증 끝---


        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        //make new JWT
        // 새로운 access 토큰 생성
        // reissue에서 새로운 토큰을 발급하므로, 시간도 여기서 재설정. 처음 발급한 토큰(Loginfilter)시간과 일치시켜야함!
        String newAccess = jwtUtil.createJwt("access", username, role, 600000L); // 10분
        String newRefresh = jwtUtil.createJwt("refresh", username, role, 86400000L); //하루

        //Refresh 토큰 저장 DB에 기존의 Refresh 토큰 삭제 후 새 Refresh 토큰 저장
        // Refresh 토큰 삭제 (동시성/중복 삭제 예외 방어)
        try {
            refreshRepository.deleteByRefresh(refresh);
        } catch (Exception e) {
            System.out.println("동시성/중복 삭제 예외: " + e.getMessage());
            // 무시, 정상 흐름 진행
        }


        addRefreshEntity(username, newRefresh, 86400000L); // 새로운 토큰 저장

        //response
        // 헤더에 새로운 access 토큰 발급
        response.setHeader("access", newAccess);
        response.addCookie(createCookie("refresh", newRefresh));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 재발급한 새로운 토큰을 저장하는 메서드
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
        cookie.setMaxAge(24*60*60);
        //cookie.setSecure(true);
        //cookie.setPath("/");
        cookie.setHttpOnly(true);
        // 클라이언트 측(JavaScript 등)에서 쿠키에 접근하지 못하게 막는 보안 설정 (필수)

        return cookie;
    }
}
