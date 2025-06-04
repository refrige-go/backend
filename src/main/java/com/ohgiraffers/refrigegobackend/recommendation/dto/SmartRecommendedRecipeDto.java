package com.ohgiraffers.refrigegobackend.recommendation.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * 스마트 추천 레시피 DTO (기존 RecommendedRecipeDto 확장)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmartRecommendedRecipeDto {
    
    // 기본 레시피 정보
    private String recipeId;
    private String recipeName;
    private String ingredients;
    private String cookingMethod1;
    private String cookingMethod2;
    private String imageUrl;
    
    // 매칭 정보
    private Integer matchedIngredientCount;
    private List<String> matchedIngredients;
    private List<String> missingIngredients;
    private Double matchScore;
    private Boolean isFavorite;
    
    // 스마트 추천 정보
    private String matchStatus; // "PERFECT", "MISSING_1", "MISSING_2", "OTHER"
    private Integer urgencyScore; // 유통기한 기반 긴급도 (낮을수록 긴급)
    private List<String> urgentIngredients; // 이 레시피에서 빨리 사용해야 할 재료
    private String recommendReason; // 추천 이유
    
    // 상태별 표시 텍스트 반환
    public String getStatusText() {
        switch (matchStatus) {
            case "PERFECT":
                return "모든 재료 OK";
            case "MISSING_1":
                return "1개 재료 부족";
            case "MISSING_2":
                return "2개 재료 부족";
            default:
                return "추천 레시피";
        }
    }
    
    // 상태별 우선순위 반환 (낮을수록 우선)
    public Integer getStatusPriority() {
        switch (matchStatus) {
            case "PERFECT":
                return 1;
            case "MISSING_1":
                return 2;
            case "MISSING_2":
                return 3;
            default:
                return 4;
        }
    }
}
