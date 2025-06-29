package com.ohgiraffers.refrigegobackend.notification.service;

import com.ohgiraffers.refrigegobackend.ingredient.domain.UserIngredient;
import com.ohgiraffers.refrigegobackend.notification.domain.Notification;
import com.ohgiraffers.refrigegobackend.notification.domain.NotificationType;
import com.ohgiraffers.refrigegobackend.notification.dto.NotificationRequestDto;
import com.ohgiraffers.refrigegobackend.notification.dto.NotificationResponseDto;
import com.ohgiraffers.refrigegobackend.notification.infrastructure.repository.NotificationRepository;
import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.user.entity.User;
import com.ohgiraffers.refrigegobackend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationPushService notificationPushService;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, 
                             UserRepository userRepository,
                             NotificationPushService notificationPushService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationPushService = notificationPushService;
    }

    public List<NotificationResponseDto> getNotifications(String username) {
        User user = userRepository.findByUsernameAndDeletedFalse(username);
        List<Notification> notifications = notificationRepository.findByUserId(user.getId());

        try {
            List<NotificationResponseDto> result = notifications.stream()
                    .map(NotificationResponseDto::new)
                    .collect(Collectors.toList());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void markAsRead(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public void sendIngredientExpirationAlert(Long userId, List<UserIngredient> ingredients) {
        String names = ingredients.stream()
                .map(UserIngredient::getIngredientName)
                .collect(Collectors.joining(", "));

        System.out.printf("🔔 [User %d] 유통기한 임박 재료: %s%n", userId, names);

        // 실제 알림 생성 및 FCM 푸시 전송
        String title = "유통기한 임박 알림";
        String content = names + "의 유통기한이 곧 만료됩니다!";

        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .type(NotificationType.EXPIRINGINGREDIENT)
                .ingredientsId(ingredients.stream()
                        .map(UserIngredient::getId)
                        .collect(Collectors.toList()))
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        // DB에 알림 저장
        notificationRepository.save(notification);
        
        // FCM 푸시 알림 전송
        notificationPushService.sendNotificationPushAsync(notification);
    }

    public void sendRecipeRecommendation(Long userId, Recipe recipe) {
        try {
            log.info("📱 레시피 추천 알림 생성 시작 - 사용자 ID: {}, 레시피: {}", userId, recipe.getRcpNm());
            
            String title = "오늘의 추천 레시피";
            String content = recipe.getRcpNm() + " 어때요? 냉장고 재료로 만들 수 있어요!";

            log.info("📝 알림 내용 - 제목: {}, 내용: {}", title, content);

            Notification notification = Notification.builder()
                    .userId(userId)
                    .title(title)
                    .content(content)
                    .type(NotificationType.RECIPERECOMMENDATION)
                    .recipeId(recipe.getRcpSeq())
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            log.info("🏗️ 알림 객체 생성 완료 - ID: {}, 생성시간: {}", notification.getId(), notification.getCreatedAt());

            // DB에 알림 저장
            log.info("💾 MongoDB에 알림 저장 시도...");
            Notification savedNotification = notificationRepository.save(notification);
            log.info("✅ MongoDB 저장 완료 - 저장된 알림 ID: {}", savedNotification.getId());
            
            // FCM 푸시 알림 전송
            log.info("📲 FCM 푸시 알림 전송 시도...");
            notificationPushService.sendNotificationPushAsync(savedNotification);
            log.info("✅ FCM 푸시 알림 전송 완료");
            
        } catch (Exception e) {
            log.error("❌ 레시피 추천 알림 생성 중 에러 발생 - 사용자 ID: {}, 레시피: {}", userId, recipe.getRcpNm(), e);
            throw e;
        }
    }
}
