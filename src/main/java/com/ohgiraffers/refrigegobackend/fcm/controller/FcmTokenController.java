package com.ohgiraffers.refrigegobackend.fcm.controller;

import com.ohgiraffers.refrigegobackend.fcm.dto.FcmTokenRequest;
import com.ohgiraffers.refrigegobackend.fcm.dto.FcmTokenResponse;
import com.ohgiraffers.refrigegobackend.fcm.service.FcmTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fcm-token")
@RequiredArgsConstructor
@Slf4j
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    /**
     * FCM 토큰을 저장하거나 업데이트합니다.
     * 
     * @param request FCM 토큰 요청
     * @param authentication 인증 정보
     * @return FCM 토큰 응답
     */
    @PostMapping
    public ResponseEntity<FcmTokenResponse> saveOrUpdateFcmToken(
            @Valid @RequestBody FcmTokenRequest request,
            Authentication authentication) {
        
        Long userId = Long.parseLong(authentication.getName());
        log.info("FCM 토큰 저장/업데이트 API 호출 - userId: {}", userId);
        
        FcmTokenResponse response = fcmTokenService.saveOrUpdateFcmToken(userId, request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자의 FCM 토큰을 조회합니다.
     * 
     * @param authentication 인증 정보
     * @return FCM 토큰 (존재하지 않으면 null)
     */
    @GetMapping
    public ResponseEntity<String> getFcmToken(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("FCM 토큰 조회 API 호출 - userId: {}", userId);
        
        String fcmToken = fcmTokenService.getFcmTokenByUserId(userId);
        
        return ResponseEntity.ok(fcmToken);
    }

    /**
     * 사용자의 FCM 토큰을 삭제합니다.
     * 
     * @param authentication 인증 정보
     * @return 성공 응답
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteFcmToken(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("FCM 토큰 삭제 API 호출 - userId: {}", userId);
        
        fcmTokenService.deleteFcmTokenByUserId(userId);
        
        return ResponseEntity.noContent().build();
    }
} 