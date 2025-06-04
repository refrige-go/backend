package com.ohgiraffers.refrigegobackend.recommendation.controller;

import com.ohgiraffers.refrigegobackend.recommendation.dto.LocationToAiDTO;
import com.ohgiraffers.refrigegobackend.recommendation.dto.WeatherRecommendResponseDTO;
import com.ohgiraffers.refrigegobackend.recommendation.service.WeatherRecommendationService;
import com.ohgiraffers.refrigegobackend.user.dto.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@Slf4j
//@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class WeatherRecommendationController {

    private final WeatherRecommendationService weatherRecommendationService;

    public WeatherRecommendationController(WeatherRecommendationService weatherRecommendationService) {
        this.weatherRecommendationService = weatherRecommendationService;
    }

    /**
     * 사용자 위치 정보 받기
     * @param locationToAiDto
     */
    @PostMapping("/location")
    public ResponseEntity<WeatherRecommendResponseDTO> receiveLocation(
            @RequestBody LocationToAiDTO locationToAiDto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        String username = customUserDetails.getUsername();
        WeatherRecommendResponseDTO result = weatherRecommendationService.getWeatherBasedRecipes(
                username,
                locationToAiDto.getLatitude(),
                locationToAiDto.getLongitude()
        );

        return ResponseEntity.ok(result);
    }
}
