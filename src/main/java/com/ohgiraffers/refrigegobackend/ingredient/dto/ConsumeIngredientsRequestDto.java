package com.ohgiraffers.refrigegobackend.ingredient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 요리 완료 시 재료 소비 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsumeIngredientsRequestDto {

    /**
     * 소비할 재료 ID 목록
     */
    private List<Long> ingredientIds;

    /**
     * 요리한 레시피 ID (선택적, 로그용)
     */
    private String recipeId;
}
