package com.ohgiraffers.refrigegobackend.recommendation.service;

import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository.RecipeRepository;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecipeRecommendationRequestDto;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecipeRecommendationResponseDto;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecommendedRecipeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 레시피 추천 서비스
 * - 사용자가 선택한 재료를 기반으로 레시피를 추천
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecipeRecommendationService {

    private final RecipeRepository recipeRepository;

    /**
     * 사용자가 선택한 재료를 기반으로 레시피 추천
     * 
     * @param requestDto 추천 요청 정보 (선택한 재료들)
     * @return 추천된 레시피 목록
     */
    public RecipeRecommendationResponseDto recommendRecipes(RecipeRecommendationRequestDto requestDto) {
        log.info("레시피 추천 시작 - 선택한 재료: {}", requestDto.getSelectedIngredients());

        // 1. 모든 레시피 조회
        List<Recipe> allRecipes = recipeRepository.findAll();

        // 2. 각 레시피에 대해 매칭 점수 계산 및 추천 레시피 생성
        List<RecommendedRecipeDto> recommendedRecipes = allRecipes.stream()
                .map(recipe -> calculateMatchScore(recipe, requestDto.getSelectedIngredients()))
                .filter(dto -> dto.getMatchedIngredientCount() > 0) // 최소 1개 이상 매칭된 레시피만
                .sorted(Comparator
                        .comparingDouble(RecommendedRecipeDto::getMatchScore).reversed() // 매칭 점수 높은 순
                        .thenComparingInt((RecommendedRecipeDto dto) -> dto.getMatchedIngredientCount()).reversed()) // 매칭 재료 수 많은 순
                .limit(requestDto.getLimit() != null ? requestDto.getLimit() : 10) // 제한 개수만큼
                .collect(Collectors.toList());

        log.info("레시피 추천 완료 - 추천된 레시피 수: {}", recommendedRecipes.size());

        return new RecipeRecommendationResponseDto(
                recommendedRecipes,
                recommendedRecipes.size(),
                requestDto.getSelectedIngredients()
        );
    }

    /**
     * 특정 레시피와 선택된 재료들의 매칭 점수를 계산
     * 
     * @param recipe 레시피
     * @param selectedIngredients 선택된 재료들
     * @return 추천 레시피 DTO
     */
    private RecommendedRecipeDto calculateMatchScore(Recipe recipe, List<String> selectedIngredients) {
        
        // final로 선언하여 람다 표현식에서 사용 가능하게 함
        final String ingredients = recipe.getRcpPartsDtls() != null ? recipe.getRcpPartsDtls() : "";

        // 레시피 재료에서 선택한 재료와 매칭되는 것들 찾기
        List<String> matchedIngredients = selectedIngredients.stream()
                .filter(ingredient -> containsIngredient(ingredients, ingredient))
                .collect(Collectors.toList());

        int matchedCount = matchedIngredients.size();

        return RecommendedRecipeDto.fromEntity(
                recipe, 
                matchedCount, 
                matchedIngredients, 
                false, // recipe_bookmarks를 사용하므로 기본값 false
                selectedIngredients.size()
        );
    }

    /**
     * 레시피 재료 문자열에 특정 재료가 포함되는지 확인
     * (대소문자 구분 없이, 공백 제거하여 비교)
     * 
     * @param recipeIngredients 레시피 재료 문자열
     * @param ingredient 찾을 재료
     * @return 포함 여부
     */
    private boolean containsIngredient(String recipeIngredients, String ingredient) {
        if (recipeIngredients == null || ingredient == null) {
            return false;
        }
        
        // 공백 제거하고 소문자로 변환하여 비교
        String normalizedRecipeIngredients = recipeIngredients.replaceAll("\\s", "").toLowerCase();
        String normalizedIngredient = ingredient.replaceAll("\\s", "").toLowerCase();
        
        return normalizedRecipeIngredients.contains(normalizedIngredient);
    }

    /**
     * 특정 레시피 상세 정보 조회
     * 
     * @param recipeId 레시피 ID
     * @return 레시피 상세 정보
     */
    public RecommendedRecipeDto getRecipeDetail(String recipeId) {
        log.info("레시피 상세 조회 - 레시피: {}", recipeId);

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("레시피를 찾을 수 없습니다. ID: " + recipeId));

        return RecommendedRecipeDto.builder()
                .recipeId(recipe.getRcpSeq())
                .recipeName(recipe.getRcpNm())
                .ingredients(recipe.getRcpPartsDtls())
                .cookingMethod1(recipe.getManual01())
                .cookingMethod2(recipe.getManual02())
                .matchedIngredientCount(0) // 상세 조회에서는 의미없음
                .matchedIngredients(List.of())
                .isFavorite(false) // recipe_bookmarks에서 관리
                .matchScore(0.0)
                .build();
    }
}
