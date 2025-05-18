package com.ohgiraffers.refrigegobackend.recipe.service;

import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recipe.dto.RecipeApiResponseDto;
import com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository.RecipeRepository; // 레포지토리 import
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Service
public class RecipeApiService {

    private final RestTemplate restTemplate;
    private final RecipeRepository recipeRepository;  // DB 저장용 레포지토리 객체

    @Value("${api.foodsafety.key}")
    private String apiKey; // 외부 API 키를 application.properties에서 주입 받음

    // 생성자 주입 (RestTemplate, RecipeRepository)
    public RecipeApiService(RestTemplate restTemplate, RecipeRepository recipeRepository) {
        this.restTemplate = restTemplate;
        this.recipeRepository = recipeRepository;
    }

    /**
     * 외부 식품안전처 API에서 1~10개의 레시피 JSON 데이터를 문자열로 가져옴
     * @return API에서 받은 JSON 문자열
     */
    public String fetchRecipes() {
        String url = "https://openapi.foodsafetykorea.go.kr/api/" + apiKey + "/COOKRCP01/json/1/10";
        return restTemplate.getForObject(url, String.class);
    }

    /**
     * fetchRecipes()에서 받은 JSON 문자열을 RecipeApiResponseDto 타입으로 변환 (파싱)
     * @return JSON을 DTO로 매핑한 RecipeApiResponseDto 객체, 실패 시 null 반환
     */
    public RecipeApiResponseDto fetchRecipeDto() {
        String jsonResponse = fetchRecipes();
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(jsonResponse, RecipeApiResponseDto.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 외부 API에서 받아온 레시피 DTO를 순회하며 DB에 저장
     * 저장 시 Recipe 엔티티로 변환 후 recipeRepository.save() 호출
     */
    public void saveRecipes() {
        RecipeApiResponseDto responseDto = fetchRecipeDto();

        if (responseDto != null && responseDto.getCOOKRCP01() != null) {
            Arrays.stream(responseDto.getCOOKRCP01().getRow()).forEach(dto -> {
                Recipe recipe = new Recipe();
                recipe.setRcpSeq(dto.getRcpSeq());               // 레시피 고유 번호
                recipe.setRcpNm(dto.getRcpNm());                 // 레시피 이름
                recipe.setRcpPartsDtls(dto.getRcpPartsDtls());   // 재료 상세
                recipe.setManual01(dto.getManual01());           // 조리 방법 1
                recipe.setManual02(dto.getManual02());           // 조리 방법 2
                recipe.setCuisineType(dto.getCuisineType());     // 요리 종류
                recipe.setRcpWay2(dto.getRcpWay2());             // 조리 방법 상세
                recipeRepository.save(recipe);                    // DB 저장
            });
        }
    }

}
