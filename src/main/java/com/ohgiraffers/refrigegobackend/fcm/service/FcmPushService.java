package com.ohgiraffers.refrigegobackend.fcm.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.ohgiraffers.refrigegobackend.fcm.infrastructure.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmPushService {

    private final FcmTokenRepository fcmTokenRepository;
    private final FirebaseMessaging firebaseMessaging;

    /**
     * 특정 사용자에게 푸시 알림을 전송합니다.
     * 
     * @param userId 사용자 ID
     * @param title 알림 제목
     * @param body 알림 내용
     * @return 전송 성공 여부
     */
    public boolean sendNotificationToUser(Long userId, String title, String body) {
        try {
            String fcmToken = fcmTokenRepository.findByUserId(userId)
                    .map(token -> token.getFcmToken())
                    .orElse(null);

            if (fcmToken == null) {
                log.warn("사용자 {}의 FCM 토큰이 존재하지 않습니다.", userId);
                return false;
            }

            return sendNotificationToToken(fcmToken, title, body);
        } catch (Exception e) {
            log.error("사용자 {}에게 알림 전송 중 오류 발생: {}", userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * FCM 토큰으로 직접 푸시 알림을 전송합니다.
     * 
     * @param fcmToken FCM 토큰
     * @param title 알림 제목
     * @param body 알림 내용
     * @return 전송 성공 여부
     */
    public boolean sendNotificationToToken(String fcmToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("FCM 알림 전송 성공 - token: {}, response: {}", maskToken(fcmToken), response);
            return true;
        } catch (FirebaseMessagingException e) {
            log.error("FCM 알림 전송 실패 - token: {}, error: {}", maskToken(fcmToken), e.getMessage());
            
            // 토큰이 유효하지 않은 경우 삭제
            if (e.getMessagingErrorCode() == com.google.firebase.messaging.MessagingErrorCode.UNREGISTERED ||
                e.getMessagingErrorCode() == com.google.firebase.messaging.MessagingErrorCode.INVALID_ARGUMENT) {
                log.warn("유효하지 않은 FCM 토큰 삭제: {}", maskToken(fcmToken));
                deleteInvalidToken(fcmToken);
            }
            
            return false;
        } catch (Exception e) {
            log.error("FCM 알림 전송 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 비동기로 푸시 알림을 전송합니다.
     * 
     * @param userId 사용자 ID
     * @param title 알림 제목
     * @param body 알림 내용
     */
    public void sendNotificationToUserAsync(Long userId, String title, String body) {
        CompletableFuture.runAsync(() -> {
            sendNotificationToUser(userId, title, body);
        });
    }

    /**
     * 유효하지 않은 FCM 토큰을 삭제합니다.
     * 
     * @param fcmToken 삭제할 FCM 토큰
     */
    private void deleteInvalidToken(String fcmToken) {
        try {
            fcmTokenRepository.findByFcmToken(fcmToken)
                    .ifPresent(token -> {
                        fcmTokenRepository.delete(token);
                        log.info("유효하지 않은 FCM 토큰 삭제 완료 - userId: {}", token.getUserId());
                    });
        } catch (Exception e) {
            log.error("유효하지 않은 FCM 토큰 삭제 중 오류 발생: {}", e.getMessage(), e);
        }
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