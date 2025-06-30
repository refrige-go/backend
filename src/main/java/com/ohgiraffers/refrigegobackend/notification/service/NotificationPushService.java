package com.ohgiraffers.refrigegobackend.notification.service;

import com.ohgiraffers.refrigegobackend.fcm.service.FcmPushService;
import com.ohgiraffers.refrigegobackend.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPushService {

    private final FcmPushService fcmPushService;

    /**
     * 알림을 생성하고 FCM 푸시를 전송합니다.
     * 
     * @param notification 생성된 알림
     */
    public void sendNotificationPush(Notification notification) {
        try {
            log.info("알림 푸시 전송 시작 - userId: {}, title: {}", 
                    notification.getUserId(), notification.getTitle());
            
            boolean success = fcmPushService.sendNotificationToUser(
                    notification.getUserId(),
                    notification.getTitle(),
                    notification.getContent()
            );
            
            if (success) {
                log.info("알림 푸시 전송 성공 - userId: {}, title: {}", 
                        notification.getUserId(), notification.getTitle());
            } else {
                log.warn("알림 푸시 전송 실패 - userId: {}, title: {}", 
                        notification.getUserId(), notification.getTitle());
            }
        } catch (Exception e) {
            log.error("알림 푸시 전송 중 오류 발생 - userId: {}, error: {}", 
                    notification.getUserId(), e.getMessage(), e);
        }
    }

    /**
     * 비동기로 알림 푸시를 전송합니다.
     * 
     * @param notification 생성된 알림
     */
    public void sendNotificationPushAsync(Notification notification) {
        fcmPushService.sendNotificationToUserAsync(
                notification.getUserId(),
                notification.getTitle(),
                notification.getContent()
        );
    }
} 