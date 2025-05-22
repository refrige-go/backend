package com.ohgiraffers.refrigegobackend.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 레시피 추천 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeRecommendationResponseDto {

    /**
     * 추천된 레시피 목록
     */
    private List<RecommendedRecipeDto> recommendedRecipes;

    /**
     * 총 추천 레시피 개수
     */
    private int totalCount;

    /**
     * 사용자가 선택한 재료 목록
     */
    private List<String> selectedIngredients;
}
