package com.ohgiraffers.refrigegobackend.ai.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * AI 서버에서 반환하는 재료 정보를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {
    
    @JsonProperty("ingredient_id")
    private Integer ingredientId;
    
    private String name;
    
    @JsonProperty("is_main_ingredient")
    private Boolean isMainIngredient;
    
    @Override
    public String toString() {
        return "Ingredient{" +
                "ingredientId=" + ingredientId +
                ", name='" + name + '\'' +
                ", isMainIngredient=" + isMainIngredient +
                '}';
    }
}
