package com.ohgiraffers.refrigegobackend.notification.controller;

import com.ohgiraffers.refrigegobackend.notification.domain.Notification;
import com.ohgiraffers.refrigegobackend.notification.domain.NotificationType;
import com.ohgiraffers.refrigegobackend.notification.dto.NotificationRequestDto;
import com.ohgiraffers.refrigegobackend.notification.dto.NotificationResponseDto;
import com.ohgiraffers.refrigegobackend.notification.infrastructure.repository.NotificationRepository;
import com.ohgiraffers.refrigegobackend.notification.service.NotificationService;
import com.ohgiraffers.refrigegobackend.user.dto.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    public NotificationController(NotificationService notificationService,
        NotificationRepository notificationRepository) {
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public ResponseEntity<?> getNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String username = userDetails.getUsername();

        List<NotificationResponseDto> notifications = notificationService.getNotifications(username);
        System.out.println("notifications : "+notifications);

        return ResponseEntity.ok(notifications);
    }


    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        System.out.println("알림id " + id + " : " + userDetails.getUsername());

        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }
}
