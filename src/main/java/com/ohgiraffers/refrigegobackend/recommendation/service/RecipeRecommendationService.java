package com.ohgiraffers.refrigegobackend.recommendation.service;

import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository.RecipeRepository;
import com.ohgiraffers.refrigegobackend.recommendation.domain.RecipeFavorite;
import com.ohgiraffers.refrigegobackend.recommendation.dto.*;
import com.ohgiraffers.refrigegobackend.recommendation.infrastructure.repository.RecipeFavoriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 레시피 추천 서비스
 * - 사용자가 선택한 재료를 기반으로 레시피를 추천
 * - 레시피 찜하기 기능 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecipeRecommendationService {

    private final RecipeRepository recipeRepository;
    private final RecipeFavoriteRepository recipeFavoriteRepository;

    /**
     * 사용자가 선택한 재료를 기반으로 레시피 추천
     * 
     * @param requestDto 추천 요청 정보 (사용자 ID, 선택한 재료들)
     * @return 추천된 레시피 목록
     */
    public RecipeRecommendationResponseDto recommendRecipes(RecipeRecommendationRequestDto requestDto) {
        log.info("레시피 추천 시작 - 사용자: {}, 선택한 재료: {}", 
                requestDto.getUserId(), requestDto.getSelectedIngredients());

        // 1. 사용자가 찜한 레시피 ID 목록 조회
        Set<String> favoriteRecipeIds = recipeFavoriteRepository
                .findRecipeIdsByUserId(requestDto.getUserId())
                .stream()
                .collect(Collectors.toSet());

        // 2. 모든 레시피 조회
        List<Recipe> allRecipes = recipeRepository.findAll();

        // 3. 각 레시피에 대해 매칭 점수 계산 및 추천 레시피 생성
        List<RecommendedRecipeDto> recommendedRecipes = allRecipes.stream()
                .map(recipe -> calculateMatchScore(recipe, requestDto.getSelectedIngredients(), favoriteRecipeIds))
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
     * @param favoriteRecipeIds 찜한 레시피 ID 목록
     * @return 추천 레시피 DTO
     */
    private RecommendedRecipeDto calculateMatchScore(Recipe recipe, 
                                                   List<String> selectedIngredients,
                                                   Set<String> favoriteRecipeIds) {
        
        String ingredients = recipe.getRcpPartsDtls();
        if (ingredients == null) {
            ingredients = "";
        }

        // 레시피 재료에서 선택한 재료와 매칭되는 것들 찾기
        List<String> matchedIngredients = selectedIngredients.stream()
                .filter(ingredient -> containsIngredient(ingredients, ingredient))
                .collect(Collectors.toList());

        int matchedCount = matchedIngredients.size();
        boolean isFavorite = favoriteRecipeIds.contains(recipe.getRcpSeq());

        return RecommendedRecipeDto.fromEntity(
                recipe, 
                matchedCount, 
                matchedIngredients, 
                isFavorite,
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
     * 레시피 찜하기/찜하기 취소
     * 
     * @param requestDto 찜하기 요청 정보
     */
    @Transactional
    public void toggleRecipeFavorite(RecipeFavoriteRequestDto requestDto) {
        log.info("레시피 찜하기 토글 - 사용자: {}, 레시피: {}", 
                requestDto.getUserId(), requestDto.getRecipeId());

        // 이미 찜했는지 확인
        boolean alreadyFavorite = recipeFavoriteRepository
                .existsByUserIdAndRecipeId(requestDto.getUserId(), requestDto.getRecipeId());

        if (alreadyFavorite) {
            // 찜하기 취소
            RecipeFavorite favorite = recipeFavoriteRepository
                    .findByUserIdAndRecipeId(requestDto.getUserId(), requestDto.getRecipeId())
                    .orElseThrow(() -> new RuntimeException("찜하기 정보를 찾을 수 없습니다."));
            
            recipeFavoriteRepository.delete(favorite);
            log.info("레시피 찜하기 취소 완료");
        } else {
            // 찜하기 추가
            RecipeFavorite newFavorite = RecipeFavorite.builder()
                    .userId(requestDto.getUserId())
                    .recipeId(requestDto.getRecipeId())
                    .build();
            
            recipeFavoriteRepository.save(newFavorite);
            log.info("레시피 찜하기 추가 완료");
        }
    }

    /**
     * 특정 사용자가 찜한 모든 레시피 조회
     * 
     * @param userId 사용자 ID
     * @return 찜한 레시피 목록
     */
    public List<RecommendedRecipeDto> getFavoriteRecipes(String userId) {
        log.info("찜한 레시피 조회 - 사용자: {}", userId);

        List<RecipeFavorite> favorites = recipeFavoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        List<RecommendedRecipeDto> favoriteRecipes = new ArrayList<>();
        
        for (RecipeFavorite favorite : favorites) {
            Recipe recipe = recipeRepository.findById(favorite.getRecipeId()).orElse(null);
            if (recipe != null) {
                RecommendedRecipeDto dto = new RecommendedRecipeDto(
                        recipe.getRcpSeq(),
                        recipe.getRcpNm(),
                        recipe.getRcpPartsDtls(),
                        recipe.getManual01(),
                        recipe.getManual02(),
                        0, // 찜한 레시피 조회에서는 매칭 점수는 의미없음
                        new ArrayList<>(),
                        true, // 찜한 레시피이므로 true
                        0.0
                );
                favoriteRecipes.add(dto);
            }
        }

        log.info("찜한 레시피 조회 완료 - 찜한 레시피 수: {}", favoriteRecipes.size());
        return favoriteRecipes;
    }

    /**
     * 특정 레시피 상세 정보 조회
     * 
     * @param recipeId 레시피 ID
     * @param userId 사용자 ID (찜하기 여부 확인용)
     * @return 레시피 상세 정보
     */
    public RecommendedRecipeDto getRecipeDetail(String recipeId, String userId) {
        log.info("레시피 상세 조회 - 레시피: {}, 사용자: {}", recipeId, userId);

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("레시피를 찾을 수 없습니다. ID: " + recipeId));

        boolean isFavorite = recipeFavoriteRepository.existsByUserIdAndRecipeId(userId, recipeId);

        return new RecommendedRecipeDto(
                recipe.getRcpSeq(),
                recipe.getRcpNm(),
                recipe.getRcpPartsDtls(),
                recipe.getManual01(),
                recipe.getManual02(),
                0, // 상세 조회에서는 매칭 수는 의미없음
                new ArrayList<>(),
                isFavorite,
                0.0
        );
    }
}
