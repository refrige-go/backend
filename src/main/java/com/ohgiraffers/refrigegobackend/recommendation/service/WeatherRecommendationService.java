package com.ohgiraffers.refrigegobackend.recommendation.service;

import com.ohgiraffers.refrigegobackend.bookmark.infrastructure.repository.BookmarkRepository;
import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recommendation.client.SeasonalIngredientApiClient;
import com.ohgiraffers.refrigegobackend.recommendation.client.WeatherApiClient;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecipeRecommendationDto;
import com.ohgiraffers.refrigegobackend.recommendation.dto.WeatherInfo;
import com.ohgiraffers.refrigegobackend.recommendation.dto.WeatherRecommendResponseDTO;
import com.ohgiraffers.refrigegobackend.recommendation.infrastructure.repository.RecipeIngredientRepository;
import com.ohgiraffers.refrigegobackend.user.entity.User;
import com.ohgiraffers.refrigegobackend.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WeatherRecommendationService {

    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;
    Logger log = LoggerFactory.getLogger(WeatherRecommendationService.class);

    private final WeatherApiClient weatherApiClient;
    private final SeasonalIngredientApiClient seasonalIngredientApiClient;
    private final RecipeIngredientRepository recipeIngredientRepository;

    public WeatherRecommendationService(
            WeatherApiClient weatherApiClient,
            SeasonalIngredientApiClient seasonalIngredientApiClient,
            RecipeIngredientRepository recipeIngredientRepository,
            UserRepository userRepository, BookmarkRepository bookmarkRepository) {
        this.weatherApiClient = weatherApiClient;
        this.seasonalIngredientApiClient = seasonalIngredientApiClient;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.userRepository = userRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    private List<String> mapWeatherToCookingTypes(String conditionText, double tempC) {
        List<String> types = new ArrayList<>();

        // 1. 날씨 텍스트 기반 매핑
        if (conditionText.contains("Sunny") || conditionText.contains("Clear")) {
            types.addAll(List.of("굽기", "볶기"));
        } else if (conditionText.contains("Cloudy") || conditionText.contains("Overcast")) {
            types.addAll(List.of("찌기", "끓이기"));
        } else if (conditionText.contains("Rain") || conditionText.contains("Drizzle")
                || conditionText.contains("Snow")) {
            types.addAll(List.of("찌기", "끓이기"));
        } else {
            types.add("볶기");
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

    public WeatherRecommendResponseDTO getWeatherBasedRecipes(String username, double lat, double lon) {
        User user = userRepository.findByUsernameAndDeletedFalse(username);

        WeatherInfo info = weatherApiClient.getWeather(lat, lon);
        String condition = info.getConditionText();
        double tempC = info.getTemperature();
        log.info("🌤 날씨 condition: {}, 온도: {}", condition, tempC);

        int month = LocalDate.now().getMonthValue();
        List<String> seasonalIngredients = seasonalIngredientApiClient.getSeasonalIngredients(month);
        log.info("🌱 {}월 제철 재료 리스트: {}", month, seasonalIngredients);

        List<String> cookingTypes = mapWeatherToCookingTypes(condition, tempC);
        log.info("🍳 추천 조리법 리스트: {}", cookingTypes);

        List<Recipe> recipes = recipeIngredientRepository.findRecipesBySeasonalIngredientsAndCookingTypes(
                seasonalIngredients, cookingTypes);
        log.info("📦 조건에 맞는 레시피 개수: {}", recipes.size());

        // 북마크된 레시피 ID를 한 번에 조회하여 N+1 쿼리 방지
        List<String> bookmarkedRecipeIds = bookmarkRepository.findRecipeIdsByUserId(user.getId());

        List<RecipeRecommendationDto> recipeDtos = recipes.stream()
                .limit(7) // 7개로 제한
                .map(recipe -> {
                    boolean bookmarked = bookmarkedRecipeIds.contains(recipe.getRcpSeq());
                    return new RecipeRecommendationDto(
                            recipe.getRcpNm(),
                            recipe.getRcpSeq(),
                            recipe.getRcpCategory(),
                            recipe.getImage(),
                            recipe.getRcpPartsDtls(),
                            recipe.getCuisineType(),
                            recipe.getRcpWay2(),
                            bookmarked);
                })
                .collect(Collectors.toList());

        // 날씨 정보와 레시피 목록을 함께 반환
        return new WeatherRecommendResponseDTO(
                new WeatherRecommendResponseDTO.WeatherInfoDto(condition, tempC),
                recipeDtos);
    }
}
