package com.ohgiraffers.refrigegobackend.recommendation.dto;

import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 레시피 추천 응답 DTO
 * - 추천된 레시피 정보와 매칭 점수를 포함
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendedRecipeDto {

    /**
     * 레시피 고유번호
     */
    private String recipeId;

    /**
     * 레시피 이름
     */
    private String recipeName;

    /**
     * 레시피 재료 상세
     */
    private String ingredients;

    /**
     * 조리 방법 1
     */
    private String cookingMethod1;

    /**
     * 조리 방법 2
     */
    private String cookingMethod2;

    /**
     * 매칭된 재료 개수
     */
    private int matchedIngredientCount;

    /**
     * 매칭된 재료 목록
     */
    private List<String> matchedIngredients;

    /**
     * 부족한 재료 목록 (AI 서버 응답용)
     */
    private List<String> missingIngredients;

    /**
     * 찜하기 여부 (recipe_bookmarks 모듈에서 관리)
     */
    @Builder.Default
    private boolean isFavorite = false;

    /**
     * 매칭 점수 (매칭된 재료 수 / 전체 선택 재료 수)
     */
    private double matchScore;

    private String imageUrl;

    public static RecommendedRecipeDto fromEntity(Recipe recipe, int matchedCount,
                                                  List<String> matchedIngredients,
                                                  boolean isFavorite,
                                                  int totalSelectedIngredients) {
        double score = totalSelectedIngredients > 0 ?
                (double) matchedCount / totalSelectedIngredients : 0.0;

        return RecommendedRecipeDto.builder()
                .recipeId(recipe.getRcpSeq())
                .recipeName(recipe.getRcpNm())
                .ingredients(recipe.getRcpPartsDtls())
                .cookingMethod1(recipe.getManual01())
                .cookingMethod2(recipe.getManual02())
                .matchedIngredientCount(matchedCount)
                .matchedIngredients(matchedIngredients)
                .isFavorite(isFavorite)
                .matchScore(score)
                .imageUrl(recipe.getImage())
                .build();
    }
    
    /**
     * 부족한 재료를 문자열로 반환하는 메서드 (프론트엔드 호환성)
     */
    public String getMissingIngredientsString() {
        if (missingIngredients == null || missingIngredients.isEmpty()) {
            return null;
        }
        return String.join(", ", missingIngredients);
    }
}
