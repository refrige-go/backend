package com.ohgiraffers.refrigegobackend.recipe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recipe.dto.response.RecipeApiResponseDto;
import com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Service
public class RecipeApiService {

    private final RestTemplate restTemplate;
    private final RecipeRepository recipeRepository;

    @Value("${api.foodsafety.key}")
    private String apiKey;

    public RecipeApiService(RestTemplate restTemplate, RecipeRepository recipeRepository) {
        this.restTemplate = restTemplate;
        this.recipeRepository = recipeRepository;
    }

    /**
     * 외부 API에서 JSON 문자열 가져오기
     */
    public String fetchRecipes(int start, int end) {
        String url = "https://openapi.foodsafetykorea.go.kr/api/" + apiKey + "/COOKRCP01/json/" + start + "/" + end;
        return restTemplate.getForObject(url, String.class);
    }

    /**
     * JSON 문자열을 DTO 객체로 파싱
     */
    public RecipeApiResponseDto fetchRecipeDto(int start, int end) {
        String jsonResponse = fetchRecipes(start, end);
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonResponse, RecipeApiResponseDto.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * DTO → Entity로 변환 후 DB 저장 (배치 처리)
     */
    public void saveRecipesBatch(int start, int end) {
        RecipeApiResponseDto responseDto = fetchRecipeDto(start, end);

        if (responseDto != null && responseDto.getCOOKRCP01() != null) {
            Arrays.stream(responseDto.getCOOKRCP01().getRow())
                    .forEach((RecipeApiResponseDto.Recipe dto) -> { // DTO 타입 명시
                        Recipe recipe = new Recipe();
                        recipe.setRcpSeq(dto.getRcpSeq());
                        recipe.setRcpNm(dto.getRcpNm());
                        recipe.setRcpPartsDtls(dto.getRcpPartsDtls());

                        // 부가 정보
                        recipe.setCuisineType(dto.getCuisineType());
                        recipe.setRcpCategory(dto.getRcpCategory());
                        recipe.setRcpWay2(dto.getRcpWay2());
                        recipe.setImage(dto.getAttFileNoMain());
                        recipe.setThumbnail(dto.getAttFileNoMk());
                        recipe.setHashTag(dto.getHashTag());

                        // 영양 정보
                        recipe.setInfoEng(dto.getInfoEng());
                        recipe.setInfoCar(dto.getInfoCar());
                        recipe.setInfoPro(dto.getInfoPro());
                        recipe.setInfoFat(dto.getInfoFat());
                        recipe.setInfoNa(dto.getInfoNa());

                        // 조리 순서
                        recipe.setManual01(dto.getManual01());
                        recipe.setManual02(dto.getManual02());
                        recipe.setManual03(dto.getManual03());
                        recipe.setManual04(dto.getManual04());
                        recipe.setManual05(dto.getManual05());
                        recipe.setManual06(dto.getManual06());

                        recipeRepository.save(recipe);
                    });
        }
    }

    /**
     * 전체 레시피 저장 (batch 단위)
     */
    public void saveAllRecipes(int totalCount, int batchSize) {
        for (int start = 1; start <= totalCount; start += batchSize) {
            int end = Math.min(start + batchSize - 1, totalCount);
            saveRecipesBatch(start, end);
        }
    }

    /**
     * 단일 범위 레시피 저장
     */
    public void saveRecipes(int start, int end) {
        String json = fetchRecipes(start, end);

        try {
            ObjectMapper mapper = new ObjectMapper();
            RecipeApiResponseDto responseDto = mapper.readValue(json, RecipeApiResponseDto.class);

            if (responseDto != null && responseDto.getCOOKRCP01() != null) {
                Arrays.stream(responseDto.getCOOKRCP01().getRow())
                        .forEach((RecipeApiResponseDto.Recipe dto) -> {
                            Recipe recipe = new Recipe();
                            recipe.setRcpSeq(dto.getRcpSeq());
                            recipe.setRcpNm(dto.getRcpNm());
                            recipe.setRcpPartsDtls(dto.getRcpPartsDtls());

                            recipe.setCuisineType(dto.getCuisineType());
                            recipe.setRcpCategory(dto.getRcpCategory());
                            recipe.setRcpWay2(dto.getRcpWay2());
                            recipe.setImage(dto.getAttFileNoMain());
                            recipe.setThumbnail(dto.getAttFileNoMk());
                            recipe.setHashTag(dto.getHashTag());

                            recipe.setInfoEng(dto.getInfoEng());
                            recipe.setInfoCar(dto.getInfoCar());
                            recipe.setInfoPro(dto.getInfoPro());
                            recipe.setInfoFat(dto.getInfoFat());
                            recipe.setInfoNa(dto.getInfoNa());

                            recipe.setManual01(dto.getManual01());
                            recipe.setManual02(dto.getManual02());
                            recipe.setManual03(dto.getManual03());
                            recipe.setManual04(dto.getManual04());
                            recipe.setManual05(dto.getManual05());
                            recipe.setManual06(dto.getManual06());

                            recipeRepository.save(recipe);
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
