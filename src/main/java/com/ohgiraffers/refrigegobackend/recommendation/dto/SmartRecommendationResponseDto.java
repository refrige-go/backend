package com.ohgiraffers.refrigegobackend.recommendation.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * 스마트 레시피 추천 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmartRecommendationResponseDto {
    
    private List<SmartRecommendedRecipeDto> recommendedRecipes;
    private Integer totalCount;
    private List<String> selectedIngredients;
    private SmartCategoryInfo categoryInfo;
    private List<String> urgentIngredients; // 빨리 사용해야 할 재료
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SmartCategoryInfo {
        private Integer perfectMatches;    // 모든 재료 있음
        private Integer oneMissingMatches;  // 1개 부족
        private Integer twoMissingMatches;  // 2개 부족
        private Integer otherMatches;       // 그 외
    }
}
