package com.ohgiraffers.refrigegobackend.recommendation.controller;

import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recommendation.dto.LocationToAiDTO;
import com.ohgiraffers.refrigegobackend.recommendation.service.WeatherRecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weather")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class WeatherRecommendationController {

    private final WeatherRecommendationService weatherRecommendationService;

    public WeatherRecommendationController(WeatherRecommendationService weatherRecommendationService) {
        this.weatherRecommendationService = weatherRecommendationService;
    }

    /**
     * 사용자 위치 정보 받기
     * @param locationToAiDto
     * @return
     */
    @PostMapping("/location")
    public ResponseEntity<List<Recipe>> receiveLocation(@RequestBody LocationToAiDTO locationToAiDto) {
        log.info("📍 요청 받은 위치: 위도={}, 경도={}", locationToAiDto.getLatitude(), locationToAiDto.getLongitude());

        List<Recipe> result = weatherRecommendationService.getWeatherBasedRecipes(locationToAiDto.getLatitude(), locationToAiDto.getLongitude());
        log.info("🍽 최종 추천 레시피 개수: {}", result.size());
        return ResponseEntity.ok(result);
    }
}
