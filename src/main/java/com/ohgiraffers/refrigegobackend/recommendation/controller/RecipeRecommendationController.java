package com.ohgiraffers.refrigegobackend.recommendation.controller;

import com.ohgiraffers.refrigegobackend.recommendation.dto.*;
import com.ohgiraffers.refrigegobackend.recommendation.service.RecipeRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 레시피 추천 관련 API 컨트롤러
 * - 재료 기반 레시피 추천
 * - 레시피 찜하기/취소
 * - 찜한 레시피 조회
 * - 레시피 상세 조회
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Slf4j
public class RecipeRecommendationController {

    private final RecipeRecommendationService recommendationService;

    /**
     * 선택한 재료 기반 레시피 추천 API
     * POST /api/recommendations/recipes
     * 
     * @param requestDto 추천 요청 정보 (사용자 ID, 선택한 재료들)
     * @return 추천된 레시피 목록
     */
    @PostMapping("/recipes")
    public ResponseEntity<RecipeRecommendationResponseDto> recommendRecipes(
            @RequestBody RecipeRecommendationRequestDto requestDto) {
        
        log.info("레시피 추천 요청 - 사용자: {}, 재료 수: {}", 
                requestDto.getUserId(), requestDto.getSelectedIngredients().size());

        // 입력 유효성 검증
        if (requestDto.getSelectedIngredients() == null || requestDto.getSelectedIngredients().isEmpty()) {
            throw new IllegalArgumentException("선택한 재료가 없습니다.");
        }
        
        if (requestDto.getSelectedIngredients().size() < 2) {
            throw new IllegalArgumentException("최소 2개 이상의 재료를 선택해주세요.");
        }

        RecipeRecommendationResponseDto response = recommendationService.recommendRecipes(requestDto);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 레시피 찜하기/찜하기 취소 API
     * POST /api/recommendations/favorites/toggle
     * 
     * @param requestDto 찜하기 요청 정보 (사용자 ID, 레시피 ID)
     * @return 성공 메시지
     */
    @PostMapping("/favorites/toggle")
    public ResponseEntity<String> toggleRecipeFavorite(
            @RequestBody RecipeFavoriteRequestDto requestDto) {
        
        log.info("레시피 찜하기 토글 요청 - 사용자: {}, 레시피: {}", 
                requestDto.getUserId(), requestDto.getRecipeId());

        recommendationService.toggleRecipeFavorite(requestDto);
        
        return ResponseEntity.ok("찜하기 상태가 변경되었습니다.");
    }

    /**
     * 사용자가 찜한 레시피 목록 조회 API
     * GET /api/recommendations/favorites/{userId}
     * 
     * @param userId 사용자 ID
     * @return 찜한 레시피 목록
     */
    @GetMapping("/favorites/{userId}")
    public ResponseEntity<List<RecommendedRecipeDto>> getFavoriteRecipes(
            @PathVariable String userId) {
        
        log.info("찜한 레시피 조회 요청 - 사용자: {}", userId);

        List<RecommendedRecipeDto> favoriteRecipes = recommendationService.getFavoriteRecipes(userId);
        
        return ResponseEntity.ok(favoriteRecipes);
    }

    /**
     * 레시피 상세 정보 조회 API
     * GET /api/recommendations/recipes/{recipeId}
     * 
     * @param recipeId 레시피 ID
     * @param userId 사용자 ID (찜하기 여부 확인용)
     * @return 레시피 상세 정보
     */
    @GetMapping("/recipes/{recipeId}")
    public ResponseEntity<RecommendedRecipeDto> getRecipeDetail(
            @PathVariable String recipeId,
            @RequestParam String userId) {
        
        log.info("레시피 상세 조회 요청 - 레시피: {}, 사용자: {}", recipeId, userId);

        RecommendedRecipeDto recipeDetail = recommendationService.getRecipeDetail(recipeId, userId);
        
        return ResponseEntity.ok(recipeDetail);
    }

    /**
     * 특정 사용자의 냉장고 재료 기반 자동 추천 API (간단 버전)
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

        // TODO: 실제로는 사용자가 보유한 재료를 조회한 후 추천해야 함
        // 현재는 예시로 임시 구현
        throw new RuntimeException("자동 추천 기능은 사용자 재료 관리 기능과 연동 후 구현 예정입니다.");
    }
}
