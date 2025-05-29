package com.ohgiraffers.refrigegobackend.recommendation.controller;

import com.ohgiraffers.refrigegobackend.common.util.SecurityUtil;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecipeRecommendationRequestDto;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecipeRecommendationResponseDto;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecommendedRecipeDto;
import com.ohgiraffers.refrigegobackend.recommendation.service.RecipeRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 레시피 추천 관련 API 컨트롤러
 * - 재료 기반 레시피 추천
 * - 레시피 상세 조회
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")  // CORS 추가
public class RecipeRecommendationController {

    private final RecipeRecommendationService recommendationService;

    /**
     * 선택한 재료 기반 레시피 추천 API
     * POST /api/recommendations/recipes
     * 
     * @param requestDto 추천 요청 정보 (선택한 재료들)
     * @return 추천된 레시피 목록
     */
    @PostMapping("/recipes")
    public ResponseEntity<RecipeRecommendationResponseDto> recommendRecipes(
            @RequestBody RecipeRecommendationRequestDto requestDto) {
        
        try {
            // JWT 토큰이 있으면 사용자 정보 설정 (선택적)
            Long currentUserId = SecurityUtil.getCurrentUserId();
            if (currentUserId != null) {
                requestDto.setUserId(String.valueOf(currentUserId));
                log.info("인증된 사용자의 레시피 추천 요청 - 사용자 ID: {}, 선택한 재료: {}", 
                        currentUserId, requestDto.getSelectedIngredients());
            } else {
                log.info("익명 사용자의 레시피 추천 요청 - 선택한 재료: {}", requestDto.getSelectedIngredients());
            }

            // 입력 유효성 검증
            if (requestDto.getSelectedIngredients() == null || requestDto.getSelectedIngredients().isEmpty()) {
                throw new IllegalArgumentException("선택한 재료가 없습니다.");
            }
            
            if (requestDto.getSelectedIngredients().size() < 1) {
                throw new IllegalArgumentException("최소 1개 이상의 재료를 선택해주세요.");
            }

            RecipeRecommendationResponseDto response = recommendationService.recommendRecipes(requestDto);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("레시피 추천 중 오류 발생: ", e);
            throw new RuntimeException("레시피 추천 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 레시피 상세 정보 조회 API
     * GET /api/recommendations/recipes/{recipeId}
     * 
     * @param recipeId 레시피 ID
     * @return 레시피 상세 정보
     */
    @GetMapping("/recipes/{recipeId}")
    public ResponseEntity<RecommendedRecipeDto> getRecipeDetail(
            @PathVariable String recipeId) {
        
        log.info("레시피 상세 조회 요청 - 레시피: {}", recipeId);

        RecommendedRecipeDto recipeDetail = recommendationService.getRecipeDetail(recipeId);
        
        return ResponseEntity.ok(recipeDetail);
    }

    /**
     * 특정 사용자의 냉장고 재료 기반 자동 추천 API
     * GET /api/recommendations/auto/{userId}
     * 
     * @param userId 사용자 ID
     * @param limit 최대 추천 개수 (기본값: 5)
     * @return 자동 추천된 레시피 목록
     */
    @GetMapping("/auto/{userId}")
    public ResponseEntity<RecipeRecommendationResponseDto> getAutoRecommendations(
            @PathVariable String userId,
            @RequestParam(defaultValue = "5") Integer limit) {
        
        log.info("자동 레시피 추천 요청 - 사용자: {}, 제한: {}", userId, limit);

        // TODO: 사용자의 UserIngredient 조회 후 추천 로직 구현
        throw new RuntimeException("자동 추천 기능은 사용자 재료 관리 기능과 연동 후 구현 예정입니다.");
    }

    /**
     * 주재료 기반 레시피 추천 API
     * POST /api/recommendations/main-ingredients
     * 
     * @param requestDto 주재료 추천 요청 정보
     * @return 주재료 기반 추천된 레시피 목록
     */
    @PostMapping("/main-ingredients")
    public ResponseEntity<RecipeRecommendationResponseDto> recommendByMainIngredients(
            @RequestBody RecipeRecommendationRequestDto requestDto) {
        
        try {
            log.info("주재료 기반 레시피 추천 요청 - 선택한 재료: {}", requestDto.getSelectedIngredients());

            if (requestDto.getSelectedIngredients() == null || requestDto.getSelectedIngredients().isEmpty()) {
                throw new IllegalArgumentException("선택한 재료가 없습니다.");
            }

            RecipeRecommendationResponseDto response = recommendationService
                    .recommendByMainIngredients(requestDto.getSelectedIngredients());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("주재료 기반 레시피 추천 중 오류 발생: ", e);
            throw new RuntimeException("주재료 기반 레시피 추천 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
