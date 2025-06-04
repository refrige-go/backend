package com.ohgiraffers.refrigegobackend.search.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeSearchResultDTO {
    
    private String rcpSeq;
    private String rcpNm;
    private String rcpCategory;
    private String rcpWay2;
    private String image;        // 메인 이미지 URL
    private String thumbnail;    // 썸네일 이미지 URL
    private double score;
    private String matchReason;
    private List<RecipeIngredientDTO> ingredients;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeIngredientDTO {
        private Long ingredientId;
        private String name;
        private boolean isMainIngredient;
    }
}
