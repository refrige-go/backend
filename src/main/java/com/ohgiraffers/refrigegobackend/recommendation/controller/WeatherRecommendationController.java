package com.ohgiraffers.refrigegobackend.recommendation.controller;

import com.ohgiraffers.refrigegobackend.recommendation.dto.LocationDTO;
import com.ohgiraffers.refrigegobackend.recommendation.service.WeatherRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
     * @param locationDto
     * @return
     */
    @PostMapping("/location")
    public ResponseEntity<Map<String, Object>> receiveLocation(@RequestBody LocationDTO locationDto) {
        double latitude = locationDto.getLatitude();
        double longitude = locationDto.getLongitude();

        log.info("위치 수신: {}, {}", latitude, longitude);

        Map<String, Object> aiResponse = weatherRecommendationService.sendLocationToAIServer(latitude, longitude);

        return ResponseEntity.ok(aiResponse);
    }
}
