package com.ohgiraffers.refrigegobackend.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 레시피 추천 요청 DTO
 * - 사용자가 선택한 재료들을 받기 위한 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeRecommendationRequestDto {

    /**
     * 사용자 ID
     */
    private String userId;

    /**
     * 사용자가 선택한 재료명 리스트
     */
    private List<String> selectedIngredients;

    /**
     * 최대 추천 레시피 개수 (기본값: 10)
     */
    private Integer limit = 10;
}
