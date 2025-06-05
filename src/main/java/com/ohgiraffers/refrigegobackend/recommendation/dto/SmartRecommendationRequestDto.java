package com.ohgiraffers.refrigegobackend.recommendation.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * 스마트 레시피 추천 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmartRecommendationRequestDto {
    
    private String userId;
    private List<String> selectedIngredients;
    private Integer limit;
    
    // 사용자의 실제 냉장고 재료 정보 (유통기한 포함)
    private List<UserIngredientInfo> userIngredients;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserIngredientInfo {
        private String name;
        private Integer expiryDaysLeft;
        private Boolean frozen;
        private String category;
        private String customName;
    }
}
