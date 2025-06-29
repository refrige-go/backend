package com.ohgiraffers.refrigegobackend.recommendation.infrastructure.scheduler;

import com.ohgiraffers.refrigegobackend.recommendation.service.RecipeRecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RecipeRecommendationScheduler {

    private final RecipeRecommendationService recipeRecommendationService;

    @Autowired
    public RecipeRecommendationScheduler(RecipeRecommendationService recipeRecommendationService) {
        this.recipeRecommendationService = recipeRecommendationService;
    }

    @Scheduled(cron = "0 0 0 * * *") // 오전9시-하루한번만
    public void scheduleRecipeRecommendation() {
        log.info("🍳 레시피 추천 스케줄러 시작 - {}", java.time.LocalDateTime.now());
        
        try {
            recipeRecommendationService.generateRecipeRecommendationForAllUsers();
            log.info("✅ 레시피 추천 스케줄러 완료");
        } catch (Exception e) {
            log.error("❌ 레시피 추천 스케줄러 실행 중 에러 발생", e);
        }
    }
}
