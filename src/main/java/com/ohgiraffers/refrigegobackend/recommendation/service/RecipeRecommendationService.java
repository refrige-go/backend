package com.ohgiraffers.refrigegobackend.recommendation.service;

import com.ohgiraffers.refrigegobackend.bookmark.infrastructure.repository.BookmarkRepository;
import com.ohgiraffers.refrigegobackend.ingredient.domain.Ingredient;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.IngredientRepository;
import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository.RecipeRepository;
import com.ohgiraffers.refrigegobackend.recommendation.dto.*;
import com.ohgiraffers.refrigegobackend.recommendation.infrastructure.repository.RecipeIngredientRepository;
import com.ohgiraffers.refrigegobackend.user.entity.User;
import com.ohgiraffers.refrigegobackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 레시피 추천 서비스
 * - RecipeIngredient 매핑 테이블을 활용한 정확한 추천
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecipeRecommendationService {

    private final RecipeIngredientRepository recipeIngredientRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;

    /**
     * 사용자가 선택한 재료를 기반으로 레시피 추천
     * - 매핑 테이블을 활용한 정확한 매칭
     * - DB 레벨에서 매칭 비율 계산
     * 
     * @param requestDto 추천 요청 정보 (선택한 재료들)
     * @return 추천된 레시피 목록
     */
    public RecipeRecommendationResponseDto recommendRecipes(RecipeRecommendationRequestDto requestDto) {
        log.info("레시피 추천 시작 - 선택한 재료: {}", requestDto.getSelectedIngredients());

        // 1. 재료명 → 재료 ID 변환
        List<Long> ingredientIds = convertIngredientNamesToIds(requestDto.getSelectedIngredients());
        
        if (ingredientIds.isEmpty()) {
            log.warn("매칭되는 표준 재료가 없습니다: {}", requestDto.getSelectedIngredients());
            return new RecipeRecommendationResponseDto(List.of(), 0, requestDto.getSelectedIngredients());
        }

        log.info("변환된 재료 ID: {}", ingredientIds);

        // 2. DB에서 매칭 비율 기반 레시피 조회 (최소 30% 이상 매칭)
        List<Object[]> matchResults = recipeIngredientRepository
                .findRecipesByIngredientsWithMatchRatio(ingredientIds, 30.0);

        // 3. 결과를 DTO로 변환
        List<RecommendedRecipeDto> recommendedRecipes = matchResults.stream()
                .limit(requestDto.getLimit() != null ? requestDto.getLimit() : 10)
                .map(this::convertToRecommendedRecipeDto)
                .collect(Collectors.toList());

        log.info("레시피 추천 완료 - 추천된 레시피 수: {}", recommendedRecipes.size());

        return new RecipeRecommendationResponseDto(
                recommendedRecipes,
                recommendedRecipes.size(),
                requestDto.getSelectedIngredients()
        );
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
                .imageUrl(recipe.getImage())
                .matchedIngredientCount(0) // 상세 조회에서는 의미없음
                .matchedIngredients(List.of())
                .isFavorite(false) // TODO: 북마크 서비스와 연동
                .matchScore(0.0)
                .build();
    }

    /**
     * 주재료 기반 추천 (더 정확한 추천)
     * 
     * @param ingredientNames 재료명 목록
     * @return 주재료 기반 추천 레시피
     */
    public RecipeRecommendationResponseDto recommendByMainIngredients(List<String> ingredientNames) {
        log.info("주재료 기반 추천 시작 - 재료: {}", ingredientNames);

        List<Long> ingredientIds = convertIngredientNamesToIds(ingredientNames);
        
        if (ingredientIds.isEmpty()) {
            return new RecipeRecommendationResponseDto(List.of(), 0, ingredientNames);
        }

        // 주재료만 매칭하는 레시피 조회
        List<com.ohgiraffers.refrigegobackend.recommendation.domain.RecipeIngredient> mainIngredientRecipes = 
                recipeIngredientRepository.findRecipesByMainIngredients(ingredientIds);

        List<RecommendedRecipeDto> recommendations = mainIngredientRecipes.stream()
                .map(ri -> RecommendedRecipeDto.builder()
                        .recipeId(ri.getRecipe().getRcpSeq())
                        .recipeName(ri.getRecipe().getRcpNm())
                        .ingredients(ri.getRecipe().getRcpPartsDtls())
                        .cookingMethod1(ri.getRecipe().getManual01())
                        .cookingMethod2(ri.getRecipe().getManual02())
                        .imageUrl(ri.getRecipe().getImage())
                        .matchedIngredientCount(1)
                        .matchScore(1.0) // 주재료 매칭이므로 높은 점수
                        .isFavorite(false)
                        .build())
                .distinct()
                .limit(10)
                .collect(Collectors.toList());

        log.info("주재료 기반 추천 완료 - 추천된 레시피 수: {}", recommendations.size());

        return new RecipeRecommendationResponseDto(recommendations, recommendations.size(), ingredientNames);
    }

    /**
     * 재료명을 표준 재료 ID로 변환 (개선된 버전)
     */
    private List<Long> convertIngredientNamesToIds(List<String> ingredientNames) {
        // 한 번의 쿼리로 모든 재료 조회 (성능 개선)
        List<Ingredient> ingredients = ingredientRepository.findByNameIn(ingredientNames);
        
        // 찾지 못한 재료들 로깅
        List<String> foundNames = ingredients.stream()
                .map(Ingredient::getName)
                .collect(Collectors.toList());
        
        List<String> notFoundNames = ingredientNames.stream()
                .filter(name -> !foundNames.contains(name))
                .collect(Collectors.toList());
        
        if (!notFoundNames.isEmpty()) {
            log.warn("표준 재료 테이블에서 찾을 수 없는 재료들: {}", notFoundNames);
        }
        
        return ingredients.stream()
                .map(Ingredient::getId)
                .collect(Collectors.toList());
    }

    /**
     * DB 쿼리 결과를 DTO로 변환
     */
    private RecommendedRecipeDto convertToRecommendedRecipeDto(Object[] result) {
        String recipeId = (String) result[0];
        String recipeName = (String) result[1];
        Long totalIngredients = (Long) result[2];
        Long matchedIngredients = (Long) result[3];
        Double matchPercentage = (Double) result[4];

        // 레시피 상세 정보 조회
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElse(null);

        return RecommendedRecipeDto.builder()
                .recipeId(recipeId)
                .recipeName(recipeName)
                .ingredients(recipe != null ? recipe.getRcpPartsDtls() : "")
                .cookingMethod1(recipe != null ? recipe.getManual01() : "")
                .cookingMethod2(recipe != null ? recipe.getManual02() : "")
                .imageUrl(recipe != null ? recipe.getImage() : "")
                .matchedIngredientCount(matchedIngredients.intValue())
                .matchedIngredients(List.of()) // TODO: 성능 최적화 후 구현
                .matchScore(matchPercentage / 100.0) // 0.0 ~ 1.0 범위로 정규화
                .isFavorite(false) // TODO: 북마크 서비스와 연동
                .build();
    }


    /**
     * 해당 레시피의 주재료를 사용한 다른 레시피 추천
     * @param username 사용자 아이디
     * @param recipeId 레시피 아이디
     * @return
     */
    public List<RecipeRecommendationDto> recommendSimilarByMainIngredients(String username, String recipeId) {

        User user = userRepository.findByUsername(username);

        // 1. 기준 레시피 주재료 아이디들 조회
        List<Long> mainIngredientIds = recipeIngredientRepository.findMainIngredientIdsByRecipeId(recipeId);

        if (mainIngredientIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 기준 레시피와 다른, 동일 주재료를 사용하는 레시피들 조회
        List<Recipe> similarRecipes = recipeIngredientRepository.findRecipesByMainIngredientIds(mainIngredientIds, recipeId);

        // 3. 북마크 여부 체크 및 DTO 변환
        return similarRecipes.stream()
                .map(recipe -> {
                    boolean bookmarked = bookmarkRepository.existsByUserIdAndRecipeRcpSeq(user.getId(), recipe.getRcpSeq());
                    return new SimilarIngredientRecipeDTO(recipe, bookmarked).toResponseDto();
                })
                .limit(10)  // 최대 10개 추천
                .collect(Collectors.toList());
    }
}
