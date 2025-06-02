package com.ohgiraffers.refrigegobackend.notification.service;

import com.ohgiraffers.refrigegobackend.ingredient.domain.UserIngredient;
import com.ohgiraffers.refrigegobackend.notification.domain.Notification;
import com.ohgiraffers.refrigegobackend.notification.domain.NotificationType;
import com.ohgiraffers.refrigegobackend.notification.dto.NotificationRequestDto;
import com.ohgiraffers.refrigegobackend.notification.dto.NotificationResponseDto;
import com.ohgiraffers.refrigegobackend.notification.infrastructure.repository.NotificationRepository;
import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationResponseDto> getNotifications(Long userId) {
        return notificationRepository.findByUserId(userId).stream()
                .map(NotificationResponseDto::new)
                .collect(Collectors.toList());
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
    }

    public void sendRecipeRecommendation(Long userId, Recipe recipe) {
        String title = "오늘의 추천 레시피";
        String content = recipe.getRcpNm() + " 어때요? 냉장고 재료로 만들 수 있어요!";

        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .type(NotificationType.RECIPERECOMMENDATION.name())
                .recipeId(recipe.getRcpSeq())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }
}
