package com.ohgiraffers.refrigegobackend.recommendation.dto;

import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimilarIngredientRecipeDTO {

    private final Recipe recipe;
    private boolean bookmarked;

    public SimilarIngredientRecipeDTO(Recipe recipe, boolean bookmarked) {
        this.recipe = recipe;
        this.bookmarked = bookmarked;
    }

    public RecipeRecommendationDto toResponseDto() {
        return new RecipeRecommendationDto(
                recipe.getRcpNm(),
                recipe.getRcpSeq(),
                recipe.getRcpCategory(),
                recipe.getImage(),
                recipe.getRcpPartsDtls(),
                recipe.getCuisineType(),
                recipe.getRcpWay2(),
                bookmarked
        );
    }
}
