package com.ohgiraffers.refrigegobackend.ai.service;

import com.ohgiraffers.refrigegobackend.ai.client.AiRecommendationResponse;
import com.ohgiraffers.refrigegobackend.ai.client.AiRecommendedRecipe;
import com.ohgiraffers.refrigegobackend.ai.client.AiRecommendationClient;
import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository.RecipeRepository;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecipeRecommendationRequestDto;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecipeRecommendationResponseDto;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecommendedRecipeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 기반 레시피 추천 서비스
 * AI 서버와 통신하여 시맨틱 검색 기반 레시피 추천을 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiRecommendationService {

    private final AiRecommendationClient aiRecommendationClient;
    private final RecipeRepository recipeRepository;

    /**
     * AI 서버를 통한 레시피 추천
     * 
     * @param requestDto 추천 요청 정보
     * @return AI 기반 추천 결과
     */
    public RecipeRecommendationResponseDto recommendRecipesWithAi(RecipeRecommendationRequestDto requestDto) {
        log.info("AI 기반 레시피 추천 시작 - 사용자: {}, 재료: {}", 
                requestDto.getUserId(), requestDto.getSelectedIngredients());

        try {
            // 1. AI 서버 헬스체크
            if (!aiRecommendationClient.isAiServerHealthy()) {
                log.warn("AI 서버가 사용 불가능합니다. 대체 로직으로 처리합니다.");
                return createFallbackResponse(requestDto);
            }

            // 2. AI 서버에 추천 요청
            AiRecommendationResponse aiResponse = aiRecommendationClient.requestRecipeRecommendation(
                requestDto.getUserId(),
                requestDto.getSelectedIngredients(),
                requestDto.getLimit()
            );

            // 3. AI 응답을 백엔드 응답 형식으로 변환
            List<RecommendedRecipeDto> recommendedRecipes = convertAiResponseToBackendResponse(aiResponse);

            // 4. 레시피 상세 정보를 DB에서 보완
            enrichRecipeDetails(recommendedRecipes);

            log.info("AI 기반 레시피 추천 완료 - 추천된 레시피 수: {}", recommendedRecipes.size());

            return new RecipeRecommendationResponseDto(
                recommendedRecipes,
                recommendedRecipes.size(),
                requestDto.getSelectedIngredients()
            );

        } catch (Exception e) {
            log.error("AI 기반 레시피 추천 중 오류 발생: ", e);
            log.info("대체 로직으로 처리합니다.");
            return createFallbackResponse(requestDto);
        }
    }

    /**
     * AI 응답을 백엔드 응답 형식으로 변환
     */
    private List<RecommendedRecipeDto> convertAiResponseToBackendResponse(AiRecommendationResponse aiResponse) {
        if (aiResponse == null || aiResponse.getRecipes() == null) {
            return List.of();
        }

        return aiResponse.getRecipes().stream()
                .map(aiRecipe -> {
                    // AI 서버 응답 로깅
                    log.info("AI 레시피 데이터: {}", aiRecipe);
                    log.info("match_reason: {}", aiRecipe.getMatchReason());
                    log.info("missing_ingredients: {}", aiRecipe.getMissingIngredients());
                    log.info("matched_ingredients: {}", aiRecipe.getMatchedIngredients());
                    
                    return this.convertAiRecipeToBackendRecipe(aiRecipe);
                })
                .collect(Collectors.toList());
    }

    /**
     * AI 서버의 레시피를 백엔드 레시피 DTO로 변환
     */
    private RecommendedRecipeDto convertAiRecipeToBackendRecipe(AiRecommendedRecipe aiRecipe) {
        return RecommendedRecipeDto.builder()
                .recipeId(aiRecipe.getRcpSeq())
                .recipeName(aiRecipe.getRcpNm())
                .ingredients(aiRecipe.getIngredientsAsString()) // 새로운 헬퍼 메서드 사용
                .cookingMethod1(aiRecipe.getRcpWay2() != null ? aiRecipe.getRcpWay2() : "")
                .cookingMethod2(aiRecipe.getRcpCategory() != null ? aiRecipe.getRcpCategory() : "")
                .imageUrl("") // AI 서버 응답에 이미지 URL 없음
                .matchedIngredientCount(aiRecipe.getMatchedIngredients() != null ? aiRecipe.getMatchedIngredients().size() : 0)
                .matchedIngredients(aiRecipe.getMatchedIngredients() != null ? aiRecipe.getMatchedIngredients() : List.of())
                .missingIngredients(aiRecipe.getMissingIngredients() != null ? aiRecipe.getMissingIngredients() : List.of())
                .matchScore(aiRecipe.getScore() != null ? aiRecipe.getScore() : 0.0)
                .isFavorite(false) // TODO: 북마크 서비스와 연동
                .build();
    }

    /**
     * DB에서 레시피 상세 정보를 보완
     */
    private void enrichRecipeDetails(List<RecommendedRecipeDto> recommendedRecipes) {
        for (int i = 0; i < recommendedRecipes.size(); i++) {
            RecommendedRecipeDto recipe = recommendedRecipes.get(i);
            try {
                Recipe dbRecipe = recipeRepository.findById(recipe.getRecipeId()).orElse(null);
                if (dbRecipe != null) {
                    // AI 서버 응답에 누락된 정보를 DB에서 보완하여 새로운 객체 생성
                    RecommendedRecipeDto enrichedRecipe = RecommendedRecipeDto.builder()
                            .recipeId(recipe.getRecipeId())
                            .recipeName(recipe.getRecipeName())
                            .ingredients(recipe.getIngredients() != null && !recipe.getIngredients().isEmpty() 
                                    ? recipe.getIngredients() : dbRecipe.getRcpPartsDtls())
                            .cookingMethod1(recipe.getCookingMethod1() != null && !recipe.getCookingMethod1().isEmpty() 
                                    ? recipe.getCookingMethod1() : dbRecipe.getManual01())
                            .cookingMethod2(recipe.getCookingMethod2() != null && !recipe.getCookingMethod2().isEmpty() 
                                    ? recipe.getCookingMethod2() : dbRecipe.getManual02())
                            .imageUrl(recipe.getImageUrl() != null ? recipe.getImageUrl() : dbRecipe.getImage())
                            .matchedIngredientCount(recipe.getMatchedIngredientCount())
                            .matchedIngredients(recipe.getMatchedIngredients())
                            .missingIngredients(recipe.getMissingIngredients()) // AI 서버에서 온 배열 유지
                            .isFavorite(recipe.isFavorite())
                            .matchScore(recipe.getMatchScore())
                            .build();
                    
                    // 리스트의 해당 위치를 새로운 객체로 교체
                    recommendedRecipes.set(i, enrichedRecipe);
                }
            } catch (Exception e) {
                log.warn("레시피 상세 정보 보완 실패 - 레시피 ID: {}, 오류: {}", 
                        recipe.getRecipeId(), e.getMessage());
            }
        }
    }

    /**
     * AI 서버 사용 불가 시 대체 응답 생성
     */
    private RecipeRecommendationResponseDto createFallbackResponse(RecipeRecommendationRequestDto requestDto) {
        log.info("AI 서버 대체 로직 실행 - 기본 응답 반환");
        
        // 빈 응답 반환 (또는 기존 MySQL 기반 로직 호출)
        return new RecipeRecommendationResponseDto(
            List.of(),
            0,
            requestDto.getSelectedIngredients()
        );
    }

    /**
     * AI 서버 연결 상태 확인
     */
    public boolean isAiServerAvailable() {
        return aiRecommendationClient.isAiServerHealthy();
    }
}
