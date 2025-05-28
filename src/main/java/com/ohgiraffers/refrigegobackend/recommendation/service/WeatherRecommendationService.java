package com.ohgiraffers.refrigegobackend.recommendation.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeatherRecommendationService {

    private final RestTemplate restTemplate;

    public WeatherRecommendationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> sendLocationToAIServer(Double latitude, Double longitude) {

        String aiServerUrl = "http://localhost:8000/weather/recommend";

        Map<String, Double> requestBody = new HashMap<>();
        requestBody.put("latitude", latitude);
        requestBody.put("longitude", longitude);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Double>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(aiServerUrl, requestEntity, Map.class);
        return response.getBody();
    }

}
