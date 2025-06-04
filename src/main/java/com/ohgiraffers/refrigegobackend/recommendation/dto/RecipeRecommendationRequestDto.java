package com.ohgiraffers.refrigegobackend.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 레시피 추천 요청 DTO
 * - 사용자가 선택한 재료들을 받기 위한 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RecipeRecommendationRequestDto {

    /**
     * 사용자 ID (선택적, 로그인하지 않은 사용자도 추천 받을 수 있음)
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

    // userId 없이 재료만으로 생성할 수 있는 생성자
    public RecipeRecommendationRequestDto(List<String> selectedIngredients) {
        this.selectedIngredients = selectedIngredients;
        this.limit = 10;
    }

    public RecipeRecommendationRequestDto(List<String> selectedIngredients, Integer limit) {
        this.selectedIngredients = selectedIngredients;
        this.limit = limit;
    }
}
