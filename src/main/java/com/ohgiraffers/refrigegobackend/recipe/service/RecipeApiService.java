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
     * 외부 식품안전처 API에서 start~end 범위 내 레시피 JSON 데이터를 문자열로 가져옴
     * @param start 조회 시작 번호
     * @param end 조회 종료 번호
     * @return API에서 받은 JSON 문자열
     */
    public String fetchRecipes(int start, int end) {
        String url = "https://openapi.foodsafetykorea.go.kr/api/" + apiKey + "/COOKRCP01/json/" + start + "/" + end;
        return restTemplate.getForObject(url, String.class);
    }

    /**
     * fetchRecipes(start, end)에서 받은 JSON 문자열을 RecipeApiResponseDto 타입으로 변환 (파싱)
     * @param start 조회 시작 번호
     * @param end 조회 종료 번호
     * @return JSON을 DTO로 매핑한 RecipeApiResponseDto 객체, 실패 시 null 반환
     */
    public RecipeApiResponseDto fetchRecipeDto(int start, int end) {
        String jsonResponse = fetchRecipes(start, end);
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(jsonResponse, RecipeApiResponseDto.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * start부터 end까지 구간별로 API 호출 후 데이터 DB에 저장 (배치 저장)
     * 한 번에 너무 많은 데이터를 처리하지 않도록 분할 처리 권장
     * @param start 시작 인덱스
     * @param end 종료 인덱스
     */
    public void saveRecipesBatch(int start, int end) {
        RecipeApiResponseDto responseDto = fetchRecipeDto(start, end);

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

    /**
     * 전체 레시피를 일정 간격(batchSize)으로 나누어 여러 번 저장 처리
     * @param totalCount 저장할 총 레시피 수 예상
     * @param batchSize 한 번에 호출할 데이터 개수
     */
    public void saveAllRecipes(int totalCount, int batchSize) {
        for (int start = 1; start <= totalCount; start += batchSize) {
            int end = Math.min(start + batchSize - 1, totalCount);
            saveRecipesBatch(start, end);
        }
    }

    /**
     * 지정된 범위(start~end) 내 레시피 데이터를 외부 API에서 받아 DB에 저장
     * @param start API 호출 시작 인덱스 (예: 1)
     * @param end API 호출 종료 인덱스 (예: 10)
     */
    public void saveRecipes(int start, int end) {
        // API URL을 범위에 맞게 생성
        String url = "https://openapi.foodsafetykorea.go.kr/api/" + apiKey + "/COOKRCP01/json/" + start + "/" + end;

        // API 호출해서 JSON 문자열 받아오기
        String jsonResponse = restTemplate.getForObject(url, String.class);

        try {
            // JSON -> DTO 변환
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            RecipeApiResponseDto responseDto = mapper.readValue(jsonResponse, RecipeApiResponseDto.class);

            // DTO가 null 아니고 레시피 리스트 존재하면 저장 진행
            if (responseDto != null && responseDto.getCOOKRCP01() != null) {
                Arrays.stream(responseDto.getCOOKRCP01().getRow()).forEach(dto -> {
                    Recipe recipe = new Recipe();
                    recipe.setRcpSeq(dto.getRcpSeq());
                    recipe.setRcpNm(dto.getRcpNm());
                    recipe.setRcpPartsDtls(dto.getRcpPartsDtls());
                    recipe.setManual01(dto.getManual01());
                    recipe.setManual02(dto.getManual02());
                    recipeRepository.save(recipe); // DB 저장
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}