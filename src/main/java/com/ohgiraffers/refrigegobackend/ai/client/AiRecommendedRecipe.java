package com.ohgiraffers.refrigegobackend.ai.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * AI 서버에서 반환하는 추천 레시피 정보를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendedRecipe {
    
    @JsonProperty("rcp_seq")
    private String rcpSeq;
    
    @JsonProperty("rcp_nm")
    private String rcpNm;
    
    private Double score;
    
    @JsonProperty("match_reason")
    private String matchReason;
    
    @JsonProperty("missing_ingredients")
    private List<String> missingIngredients;
    
    @JsonProperty("matched_ingredients")
    private List<String> matchedIngredients;
    
    // String에서 List<Ingredient>로 변경 - 이것이 핵심!
    private List<Ingredient> ingredients;
    
    @JsonProperty("rcp_way2")
    private String rcpWay2;
    
    @JsonProperty("rcp_category")
    private String rcpCategory;
    
    /**
     * 재료 리스트를 문자열로 변환하는 헬퍼 메서드
     * 기존 코드와의 호환성을 위해 제공
     */
    public String getIngredientsAsString() {
        if (ingredients == null || ingredients.isEmpty()) {
            return "";
        }
        
        return ingredients.stream()
                .map(Ingredient::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
    
    /**
     * 주재료만 문자열로 반환하는 메서드
     */
    public String getMainIngredientsAsString() {
        if (ingredients == null || ingredients.isEmpty()) {
            return "";
        }
        
        return ingredients.stream()
                .filter(ingredient -> ingredient.getIsMainIngredient() != null && ingredient.getIsMainIngredient())
                .map(Ingredient::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
    
    /**
     * 매칭된 재료 문자열로 반환하는 메서드
     */
    public String getMatchedIngredientsAsString() {
        if (matchedIngredients == null || matchedIngredients.isEmpty()) {
            return "";
        }
        
        return String.join(", ", matchedIngredients);
    }
    
    /**
     * 부족한 재료 문자열로 반환하는 메서드
     */
    public String getMissingIngredientsAsString() {
        if (missingIngredients == null || missingIngredients.isEmpty()) {
            return "";
        }
        
        return String.join(", ", missingIngredients);
    }
    
    @Override
    public String toString() {
        return "AiRecommendedRecipe{" +
                "rcpSeq='" + rcpSeq + '\'' +
                ", rcpNm='" + rcpNm + '\'' +
                ", score=" + score +
                ", matchReason='" + matchReason + '\'' +
                ", missingIngredients=" + missingIngredients +
                ", matchedIngredients=" + matchedIngredients +
                ", ingredients=" + ingredients +
                ", rcpWay2='" + rcpWay2 + '\'' +
                ", rcpCategory='" + rcpCategory + '\'' +
                '}';
    }
}
