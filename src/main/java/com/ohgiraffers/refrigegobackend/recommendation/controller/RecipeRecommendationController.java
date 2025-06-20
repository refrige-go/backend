package com.ohgiraffers.refrigegobackend.recommendation.controller;

import com.ohgiraffers.refrigegobackend.ai.service.AiRecommendationService;
import com.ohgiraffers.refrigegobackend.bookmark.dto.response.UserIngredientRecipeResponseDTO;
import com.ohgiraffers.refrigegobackend.common.util.SecurityUtil;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecipeRecommendationDto;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecipeRecommendationRequestDto;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecipeRecommendationResponseDto;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecommendedRecipeDto;
import com.ohgiraffers.refrigegobackend.recommendation.dto.SmartRecommendationRequestDto;
import com.ohgiraffers.refrigegobackend.recommendation.dto.SmartRecommendationResponseDto;
import com.ohgiraffers.refrigegobackend.recommendation.service.RecipeRecommendationService;
import com.ohgiraffers.refrigegobackend.user.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 레시피 추천 관련 API 컨트롤러
 * - 재료 기반 레시피 추천 (AI 서버 우선)
 * - 레시피 상세 조회
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Slf4j
// CORS는 CorsConfig에서 전역 설정됨
public class RecipeRecommendationController {

    private final RecipeRecommendationService recommendationService;
    private final RecipeRecommendationService recipeRecommendationService;
    private final AiRecommendationService aiRecommendationService; // AI 추천 서비스 추가

    /**
     * 선택한 재료 기반 레시피 추천 API (AI 서버 우선)
     * POST /api/recommendations/recipes
     * 
     * @param requestDto 추천 요청 정보 (선택한 재료들)
     * @return 추천된 레시피 목록
     */
    @PostMapping("/recipes")
    public ResponseEntity<RecipeRecommendationResponseDto> recommendRecipes(
            @RequestBody RecipeRecommendationRequestDto requestDto) {
        
        try {
            log.info("=== 레시피 추천 요청 시작 (AI 서버 우선) ===");
            log.info("요청 DTO: {}", requestDto);
            log.info("선택한 재료들: {}", requestDto.getSelectedIngredients());
            log.info("요청 제한 수: {}", requestDto.getLimit());
            
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
            if (requestDto.getSelectedIngredients() == null) {
                log.error("선택한 재료가 null입니다.");
                throw new IllegalArgumentException("선택한 재료가 없습니다.");
            }
            
            if (requestDto.getSelectedIngredients().isEmpty()) {
                log.error("선택한 재료 리스트가 비어있습니다.");
                throw new IllegalArgumentException("선택한 재료가 없습니다.");
            }
            
            if (requestDto.getSelectedIngredients().size() < 1) {
                log.error("선택한 재료 개수가 부족합니다: {}", requestDto.getSelectedIngredients().size());
                throw new IllegalArgumentException("최소 1개 이상의 재료를 선택해주세요.");
            }

            log.info("유효성 검증 통과 - AI 서버 추천 시도");
            
            // AI 서버 우선 사용, 실패 시 기존 MySQL 방식으로 fallback
            RecipeRecommendationResponseDto response;
            try {
                if (aiRecommendationService.isAiServerAvailable()) {
                    log.info("AI 서버 사용 가능 - AI 기반 추천 실행");
                    response = aiRecommendationService.recommendRecipesWithAi(requestDto);
                    log.info("AI 서버 추천 완료 - 응답 레시피 수: {}", response.getTotalCount());
                } else {
                    log.warn("AI 서버 사용 불가 - 기존 MySQL 방식으로 fallback");
                    response = recommendationService.recommendRecipes(requestDto);
                }
            } catch (Exception aiException) {
                log.error("AI 서버 추천 실패, 기존 방식으로 fallback: {}", aiException.getMessage());
                response = recommendationService.recommendRecipes(requestDto);
            }
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("입력 값 오류: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("레시피 추천 중 예상치 못한 오류 발생: ", e);
            return ResponseEntity.internalServerError().build();
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

    /**
     * 해당 레시피의 주재료를 사용한 다른 레시피 추천
     * @param id 레시피 ID
     * @param customUserDetails 로그인된 사용자 정보
     */
    @GetMapping("/{id}/similar-ingredients")
    public ResponseEntity<List<RecipeRecommendationDto>> recommendSimilarIngredients(@PathVariable String id,
                                                                                     @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String username = customUserDetails.getUsername();
        return ResponseEntity.ok(recommendationService.recommendSimilarByMainIngredients(username, id));
    }

    /**
     * 스마트 레시피 추천 API (유통기한 고려)
     * POST /api/recommendations/smart
     */
    @PostMapping("/smart")
    public ResponseEntity<SmartRecommendationResponseDto> recommendRecipesSmart(
            @RequestBody SmartRecommendationRequestDto requestDto) {
        
        try {
            log.info("=== 스마트 레시피 추천 요청 시작 ===");
            log.info("요청 DTO: {}", requestDto);
            log.info("선택한 재료들: {}", requestDto.getSelectedIngredients());
            
            // JWT 토큰이 있으면 사용자 정보 설정
            Long currentUserId = SecurityUtil.getCurrentUserId();
            if (currentUserId != null) {
                requestDto.setUserId(String.valueOf(currentUserId));
                log.info("인증된 사용자의 스마트 추천 요청 - 사용자 ID: {}", currentUserId);
            } else {
                log.info("익명 사용자의 스마트 추천 요청");
            }
            
            // 입력 유효성 검증
            if (requestDto.getSelectedIngredients() == null || requestDto.getSelectedIngredients().isEmpty()) {
                throw new IllegalArgumentException("선택한 재료가 없습니다.");
            }
            
            SmartRecommendationResponseDto response = recommendationService.recommendRecipesSmart(requestDto);
            
            log.info("스마트 레시피 추천 완료 - 추천된 레시피 수: {}", response.getTotalCount());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("입력 값 오류: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("스마트 레시피 추천 중 예상치 못한 오류 발생: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * AI 서버 상태 확인 API
     * GET /api/recommendations/ai-status
     */
    @GetMapping("/ai-status")
    public ResponseEntity<Object> getAiServerStatus() {
        try {
            boolean isAvailable = aiRecommendationService.isAiServerAvailable();
            return ResponseEntity.ok(Map.of(
                "aiServerAvailable", isAvailable,
                "status", isAvailable ? "healthy" : "unavailable",
                "fallbackMode", !isAvailable
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "aiServerAvailable", false,
                "status", "error",
                "fallbackMode", true,
                "error", e.getMessage()
            ));
        }
    }
}
