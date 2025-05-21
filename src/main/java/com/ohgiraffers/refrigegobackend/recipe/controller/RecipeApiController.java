package com.ohgiraffers.refrigegobackend.recipe.controller;

import com.ohgiraffers.refrigegobackend.recipe.service.RecipeApiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recipe API 컨트롤러 클래스
 *
 * - 외부 API로부터 레시피 데이터를 조회(fetch)하고
 * - DB에 저장(save)하는 REST API 엔드포인트 제공
 */
@RestController
public class RecipeApiController {

    private final RecipeApiService recipeApiService;

    // 생성자 주입 방식으로 RecipeApiService 주입
    public RecipeApiController(RecipeApiService recipeApiService) {
        this.recipeApiService = recipeApiService;
    }

//    /**
//     * 레시피 데이터를 외부 API에서 JSON 문자열로 조회하는 엔드포인트
//     * GET /api/recipes/fetch
//     *
//     * @return JSON 문자열 형태의 레시피 데이터
//     */
//    @GetMapping("/api/recipes/fetch")
//    public String fetchRecipes() {
//        return recipeApiService.fetchRecipes(1, 10); // 1~10개 호출
//    }

    /**
     * 외부 API에서 조회한 레시피 데이터를 DB에 저장하는 엔드포인트
     * GET /api/recipes/save
     *
     * @return 저장 완료 메시지
     */
    @GetMapping("/api/recipes/save")
    public String saveRecipes(@RequestParam int start, @RequestParam int end) {
        recipeApiService.saveRecipes(start, end);
        return "레시피 저장 완료";
    }

    @GetMapping("/api/recipes/saveAll")
    public String saveAllRecipes() {
        int totalCount = 1136; //
        int batchSize = 100;   // 한 번에 처리할 개수
        recipeApiService.saveAllRecipes(totalCount, batchSize);
        return "레시피 저장 완료";
    }
}
