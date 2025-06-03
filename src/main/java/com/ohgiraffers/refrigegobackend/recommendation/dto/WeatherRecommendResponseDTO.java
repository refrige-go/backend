package com.ohgiraffers.refrigegobackend.recommendation.dto;

import lombok.Data;

import java.util.List;

@Data
public class WeatherRecommendResponseDTO {

    private WeatherInfoDto weather;
    private List<RecipeRecommendationDto> recipes;

    @Data
    public static class WeatherInfoDto {
        private String conditionText;
        private double tempC;

        public WeatherInfoDto(String conditionText, double tempC) {
            this.conditionText = conditionText;
            this.tempC = tempC;
        }
    }

    public WeatherRecommendResponseDTO(WeatherInfoDto weather, List<RecipeRecommendationDto> recipes) {
        this.weather = weather;
        this.recipes = recipes;
    }
}
