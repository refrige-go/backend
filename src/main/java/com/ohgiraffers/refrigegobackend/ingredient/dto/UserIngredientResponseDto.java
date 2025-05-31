package com.ohgiraffers.refrigegobackend.ingredient.dto;

import com.ohgiraffers.refrigegobackend.ingredient.domain.UserIngredient;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 유저 냉장고 보유 재료 조회 응답 DTO
 */
@Getter
public class UserIngredientResponseDto {

    private final Long id;              // user_ingredients 고유 ID
    private final Long userId;          // 유저 ID
    private final Long ingredientId;    // 기준 재료 ID (nullable)
    private final String name;          // 재료명 (기준재료 or 직접입력)
    private final boolean isFrozen;     // 냉동 여부
    private final Long expiryDaysLeft;  // 유통기한까지 남은 일수
    private final String category;      // 기준 카테고리 or null
    private final LocalDate purchaseDate;
    private final LocalDate expiryDate;
    private final String imageUrl;

    public UserIngredientResponseDto(UserIngredient entity, String name, String category) {
        this.id = entity.getId();
        this.userId = entity.getUserId();
        this.ingredientId = entity.getIngredient() != null ? entity.getIngredient().getId() : null; // 기준 재료 ID
        this.name = name;
        this.isFrozen = entity.getIsFrozen();
        this.category = category;
        this.purchaseDate = entity.getPurchaseDate();
        this.expiryDate = entity.getExpiryDate();
        this.imageUrl = entity.getImageUrl();

        if (expiryDate != null) {
            this.expiryDaysLeft = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        } else {
            this.expiryDaysLeft = null;
        }
    }
}
