package com.ohgiraffers.refrigegobackend.recipe.dto.response;

public class RecipeByCategoryDTO {
    
    private String recipeNm;
    private String category;
    private String image;

    public RecipeByCategoryDTO() {}
    
    public RecipeByCategoryDTO(String recipeNm, String category, String image) {
        this.recipeNm = recipeNm;
        this.category = category;
        this.image = image;
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
}
