package com.ohgiraffers.refrigegobackend.recommendation.infrastructure.scheduler;

import com.ohgiraffers.refrigegobackend.recommendation.service.RecipeRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RecipeRecommendationScheduler {

    private final RecipeRecommendationService recipeRecommendationService;

    @Autowired
    public RecipeRecommendationScheduler(RecipeRecommendationService recipeRecommendationService) {
        this.recipeRecommendationService = recipeRecommendationService;
    }

    @Scheduled(cron = "0 0 15 * * *")
    public void scheduleRecipeRecommendation() {
        recipeRecommendationService.generateRecipeRecommendationForAllUsers();
    }
}
