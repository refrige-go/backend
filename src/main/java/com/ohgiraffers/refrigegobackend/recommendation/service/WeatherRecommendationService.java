package com.ohgiraffers.refrigegobackend.recommendation.service;

import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recommendation.client.SeasonalIngredientApiClient;
import com.ohgiraffers.refrigegobackend.recommendation.client.WeatherApiClient;
import com.ohgiraffers.refrigegobackend.recommendation.dto.WeatherInfo;
import com.ohgiraffers.refrigegobackend.recommendation.infrastructure.repository.RecipeIngredientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class WeatherRecommendationService {

    Logger log = LoggerFactory.getLogger(WeatherRecommendationService.class);

    private final WeatherApiClient weatherApiClient;
    private final SeasonalIngredientApiClient seasonalIngredientApiClient;
    private final RecipeIngredientRepository recipeIngredientRepository;

    public WeatherRecommendationService(
            WeatherApiClient weatherApiClient,
            SeasonalIngredientApiClient seasonalIngredientApiClient,
            RecipeIngredientRepository recipeIngredientRepository
    ) {
        this.weatherApiClient = weatherApiClient;
        this.seasonalIngredientApiClient = seasonalIngredientApiClient;
        this.recipeIngredientRepository = recipeIngredientRepository;
    }

    private List<String> mapWeatherToCookingTypes(String conditionText, double tempC) {
        List<String> types = new ArrayList<>();

        // 1. 날씨 텍스트 기반 매핑
        if (conditionText.contains("Sunny") || conditionText.contains("Clear")) {
            types.addAll(List.of("굽기", "볶기"));
        } else if (conditionText.contains("Cloudy") || conditionText.contains("Overcast")) {
            types.addAll(List.of("찌기", "끓이기"));
        } else if (conditionText.contains("Rain") || conditionText.contains("Drizzle") || conditionText.contains("Snow")) {
            types.addAll(List.of("찌기", "끓이기"));
        } else {
            types.add("볶기"); // fallback
        }

        // 2. 기온 보정
        if (tempC >= 28) {
            types.add("기타");
            types.add("튀기기");
        } else if (tempC <= 5) {
            types.add("끓이기");
            types.add("찌기");
        }

        return types.stream().distinct().toList(); // 중복 제거
    }

    public List<Recipe> getWeatherBasedRecipes(double lat, double lon) {
        WeatherInfo info = weatherApiClient.getWeather(lat, lon); // API 호출
        String condition = info.getConditionText(); // "Partly cloudy" 등
        double tempC = info.getTemperature();       // 25.3℃ 등
        log.info("🌤 날씨 condition: {}, 온도: {}", condition, tempC);

        int month = LocalDate.now().getMonthValue();
        List<String> seasonalIngredients = seasonalIngredientApiClient.getSeasonalIngredients(month);
        log.info("🌱 {}월 제철 재료 리스트: {}", month, seasonalIngredients);

        List<String> cookingTypes = mapWeatherToCookingTypes(condition, tempC);
        log.info("🍳 추천 조리법 리스트: {}", cookingTypes);

        // 링크 테이블을 통해 제철 재료를 포함하고 날씨에 맞는 조리법을 가진 레시피를 한 번에 조회
        List<Recipe> recipes = recipeIngredientRepository.findRecipesBySeasonalIngredientsAndCookingTypes(
                seasonalIngredients, cookingTypes);
        
        log.info("📦 조건에 맞는 레시피 개수: {}", recipes.size());
        for (Recipe r : recipes) {
            log.info("➡️ 레시피 이름: {}, 조리법: {}", r.getRcpNm(), r.getCuisineType());
        }

        return recipes;
    }
}
