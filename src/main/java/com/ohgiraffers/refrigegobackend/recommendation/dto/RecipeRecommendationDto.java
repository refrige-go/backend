package com.ohgiraffers.refrigegobackend.recommendation.dto;


import lombok.Data;

@Data
public class RecipeRecommendationDto {

    private String recipeNm;
    private String rcpSeq;
    private String category;
    private String image;
    private String rcpPartsDtls;
    private String cuisineType;
    private String rcpWay2;
    private boolean bookmarked;

    public RecipeRecommendationDto(String recipeNm, String rcpSeq, String category, String image, String rcpPartsDtls, String cuisineType, String rcpWay2, boolean bookmarked) {
        this.recipeNm = recipeNm;
        this.rcpSeq = rcpSeq;
        this.category = category;
        this.image = image;
        this.rcpPartsDtls = rcpPartsDtls;
        this.cuisineType = cuisineType;
        this.rcpWay2 = rcpWay2;
        this.bookmarked = bookmarked;
    }
}

