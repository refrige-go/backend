package com.ohgiraffers.refrigegobackend.recipe.dto.response;

public class RecipeByCategoryDTO {
    
    private String recipeNm;
    private String category;
    private String image;
    private String rcpPartsDtls;
    private String cuisineType;
    private String rcpWay2;

    public RecipeByCategoryDTO() {}

    public RecipeByCategoryDTO(String recipeNm, String category, String image, String rcpPartsDtls, String cuisineType, String rcpWay2) {
        this.recipeNm = recipeNm;
        this.category = category;
        this.image = image;
        this.rcpPartsDtls = rcpPartsDtls;
        this.cuisineType = cuisineType;
        this.rcpWay2 = rcpWay2;
    }

    public String getRecipeNm() {
        return recipeNm;
    }

    public void setRecipeNm(String recipeNm) {
        this.recipeNm = recipeNm;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getRcpPartsDtls() {
        return rcpPartsDtls;
    }

    public void setRcpPartsDtls(String rcpPartsDtls) {
        this.rcpPartsDtls = rcpPartsDtls;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }

    public String getRcpWay2() {
        return rcpWay2;
    }

    public void setRcpWay2(String rcpWay2) {
        this.rcpWay2 = rcpWay2;
    }
}
