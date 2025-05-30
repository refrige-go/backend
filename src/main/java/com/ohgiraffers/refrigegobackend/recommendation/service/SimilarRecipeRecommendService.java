package com.ohgiraffers.refrigegobackend.recommendation.service;

import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.IngredientRepository;
import com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SimilarRecipeRecommendService {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final RestTemplate restTemplate;

    public SimilarRecipeRecommendService(RecipeRepository recipeRepository, IngredientRepository ingredientRepository, RestTemplate restTemplate) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.restTemplate = restTemplate;
    }
}
