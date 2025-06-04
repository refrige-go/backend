package com.ohgiraffers.refrigegobackend.notification.controller;

import com.ohgiraffers.refrigegobackend.notification.dto.NotificationRequestDto;
import com.ohgiraffers.refrigegobackend.notification.dto.NotificationResponseDto;
import com.ohgiraffers.refrigegobackend.notification.service.NotificationService;
import com.ohgiraffers.refrigegobackend.user.dto.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
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
