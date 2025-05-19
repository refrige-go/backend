package com.ohgiraffers.refrigegobackend.ingredient.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UserIngredientBatchRequestDto {
    private String userId;
    private List<UserIngredientItem> ingredients;

    @Getter
    @Setter
    public static class UserIngredientItem {
        private Long ingredientId;     // 기준 재료 ID, null 가능
        private String customName;     // 직접 추가명, ingredientId 없을 때 사용
        private LocalDate purchaseDate;
        private LocalDate expiryDate;
        private boolean isFrozen;
    }
}

