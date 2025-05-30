//package com.ohgiraffers.refrigegobackend.recommendation.service;
//
//import com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository.RecipeRepository;
//import com.ohgiraffers.refrigegobackend.recommendation.dto.LocationToAiDTO;
//import com.ohgiraffers.refrigegobackend.recommendation.dto.RecipeDTO;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Service
//public class WeatherRecommendationService {
//
//    private final RestTemplate restTemplate;
//    private final RecipeRepository recipeRepository;
//
//    public WeatherRecommendationService(RestTemplate restTemplate, RecipeRepository recipeRepository) {
//        this.restTemplate = restTemplate;
//        this.recipeRepository = recipeRepository;
//    }
//
//    public Map<String, Object> sendLocationToAIServer(Double latitude, Double longitude) {
//
//        String aiServerUrl = "http://localhost:8000/weather/recommend/ai";
//
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("latitude", latitude);
//        requestBody.put("longitude", longitude);
//
//        List<RecipeDTO> recipeList = fetchRecipesFromDB();
//        requestBody.put("recipes", recipeList);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
//
//        ResponseEntity<Map> response = restTemplate.postForEntity(aiServerUrl, requestEntity, Map.class);
//        return response.getBody();
//    }
//
//    private List<RecipeDTO> fetchRecipesFromDB() {
//        List<RecipeDTO> recipes = recipeRepository.findAll().stream().map(recipe -> RecipeDTO.builder()
//                .name(recipe.getRcpNm())
//                .ingredients(recipe.getRcpPartsDtls())
//                .category(recipe.getRcpCategory())
//                .type(recipe.getCuisineType())
//                .build()
//        ).collect(Collectors.toList());
//
//        return recipes;
//    }
//}
