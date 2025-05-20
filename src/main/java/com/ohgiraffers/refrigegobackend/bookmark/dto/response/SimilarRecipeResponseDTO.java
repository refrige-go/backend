package com.ohgiraffers.refrigegobackend.bookmark.dto.response;

import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;

public class SimilarRecipeResponseDTO {
    private String recipeId;
    private String name;
    private String image;
    private String ingredients;

    public SimilarRecipeResponseDTO(Recipe recipe) {
        this.recipeId = recipe.getRcpSeq();
        this.name = recipe.getRcpNm();
        this.image = recipe.getImage();
        this.ingredients = recipe.getRcpPartsDtls();
    }

    public SimilarRecipeResponseDTO() {}

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }
}
