package com.ohgiraffers.refrigegobackend.fcm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenResponse {
    
    private Long userId;
    private String fcmToken;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isNewToken;
} 