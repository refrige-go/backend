package com.ohgiraffers.refrigegobackend.ingredients.dto;

import com.ohgiraffers.refrigegobackend.ingredients.domain.UserIngredient;
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

    public UserIngredientResponseDto(UserIngredient entity, String name) {
        this.id = entity.getId();
        this.userId = entity.getUserId();
        this.ingredientId = entity.getIngredientId(); // 기준 재료 ID
        this.name = name; // 기준 or custom 재료 이름
        this.isFrozen = entity.isFrozen();
    }
}
