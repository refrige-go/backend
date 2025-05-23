package com.ohgiraffers.refrigegobackend.ingredient.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class IngredientAddRequestDto {
    private List<Long> ingredientIds;  // 선택된 재료 ID들
}