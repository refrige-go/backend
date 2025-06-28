package com.ohgiraffers.refrigegobackend.fcm.service;

import com.ohgiraffers.refrigegobackend.fcm.domain.FcmToken;
import com.ohgiraffers.refrigegobackend.fcm.dto.FcmTokenRequest;
import com.ohgiraffers.refrigegobackend.fcm.dto.FcmTokenResponse;
import com.ohgiraffers.refrigegobackend.fcm.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;

    /**
     * FCM 토큰을 저장하거나 업데이트합니다.
     * 사용자당 하나의 토큰만 저장되며, 이미 존재하면 업데이트합니다.
     * 
     * @param userId 사용자 ID
     * @param request FCM 토큰 요청
     * @return FCM 토큰 응답
     */
    @Transactional
    public FcmTokenResponse saveOrUpdateFcmToken(Long userId, FcmTokenRequest request) {
        log.info("FCM 토큰 저장/업데이트 요청 - userId: {}, token: {}", userId, maskToken(request.getFcmToken()));
        
        boolean isNewToken = false;
        FcmToken fcmToken;
        
        // 기존 토큰이 있는지 확인
        if (fcmTokenRepository.existsByUserId(userId)) {
            // 기존 토큰 업데이트
            fcmToken = fcmTokenRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("사용자의 FCM 토큰을 찾을 수 없습니다."));
            fcmToken.setFcmToken(request.getFcmToken());
            log.info("기존 FCM 토큰 업데이트 - userId: {}", userId);
        } else {
            // 새로운 토큰 생성
            fcmToken = FcmToken.builder()
                    .userId(userId)
                    .fcmToken(request.getFcmToken())
                    .build();
            isNewToken = true;
            log.info("새로운 FCM 토큰 생성 - userId: {}", userId);
        }
        
        FcmToken savedToken = fcmTokenRepository.save(fcmToken);
        
        return FcmTokenResponse.builder()
                .userId(savedToken.getUserId())
                .fcmToken(savedToken.getFcmToken())
                .createdAt(savedToken.getCreatedAt())
                .updatedAt(savedToken.getUpdatedAt())
                .isNewToken(isNewToken)
                .build();
    }

    /**
     * 사용자의 FCM 토큰을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return FCM 토큰 (존재하지 않으면 null)
     */
    public String getFcmTokenByUserId(Long userId) {
        return fcmTokenRepository.findByUserId(userId)
                .map(FcmToken::getFcmToken)
                .orElse(null);
    }

    /**
     * 사용자의 FCM 토큰을 삭제합니다.
     * 
     * @param userId 사용자 ID
     */
    @Transactional
    public void deleteFcmTokenByUserId(Long userId) {
        log.info("FCM 토큰 삭제 요청 - userId: {}", userId);
        fcmTokenRepository.deleteByUserId(userId);
    }

    /**
     * FCM 토큰을 마스킹하여 로그에 출력합니다.
     * 
     * @param token 원본 토큰
     * @return 마스킹된 토큰
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
} 