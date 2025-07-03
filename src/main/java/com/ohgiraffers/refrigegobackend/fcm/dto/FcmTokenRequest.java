package com.ohgiraffers.refrigegobackend.fcm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenRequest {
    
    @NotBlank(message = "FCM 토큰은 필수입니다.")
    private String fcmToken;
} 