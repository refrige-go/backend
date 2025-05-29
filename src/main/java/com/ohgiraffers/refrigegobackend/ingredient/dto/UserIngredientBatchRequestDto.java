package com.ohgiraffers.refrigegobackend.ingredient.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class UserIngredientBatchRequestDto {

    private Long userId;
    private List<UserIngredientItem> ingredients;

    @Getter
    @Setter
    public static class UserIngredientItem {
        private Long ingredientId;        // 기준 재료 ID
        private String customName;        // 직접 추가 재료명
        private LocalDate purchaseDate;
        private LocalDate expiryDate;
        private boolean isFrozen;
    }

    // (필요 시 사용되는 헬퍼 메서드)
    public List<Long> getIngredientIds() {
        return ingredients.stream()
                .map(UserIngredientItem::getIngredientId)
                .filter(Objects::nonNull)
                .toList();
    }
}
