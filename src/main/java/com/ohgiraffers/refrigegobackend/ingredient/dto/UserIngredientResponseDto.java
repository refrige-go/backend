package com.ohgiraffers.refrigegobackend.ingredient.dto;

import com.ohgiraffers.refrigegobackend.ingredient.domain.UserIngredient;
import lombok.Getter;

/**
 * 유저 냉장고 보유 재료 조회 응답 DTO
 */
@Getter
public class UserIngredientResponseDto {

    private final Long id;             // user_ingredients 고유 ID
    private final String userId;       // 유저 ID
    private final Long ingredientId;   // 기준 재료 ID (nullable)
    private final String name;         // 재료명 (기준재료 or 직접입력)
    private final boolean isFrozen;    // 냉동 여부
    private final Long expiryDaysLeft; // 유통기한까지 남은 일수 (null 가능)
    private final String category;

    public UserIngredientResponseDto(UserIngredient entity, String name, String category) {
        this.id = entity.getId();
        this.userId = entity.getUserId();
        this.ingredientId = entity.getIngredientId();
        this.name = name;
        this.isFrozen = entity.isFrozen();
        this.category = category;

        // expiryDate가 null이면 null, 아니면 D-day 계산값 저장
        if (entity.getExpiryDate() != null) {
            this.expiryDaysLeft = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), entity.getExpiryDate());
        } else {
            this.expiryDaysLeft = null;
        }
    }
}