package com.ohgiraffers.refrigegobackend.recommendation.service;

import com.ohgiraffers.refrigegobackend.ingredient.domain.Ingredient;
import com.ohgiraffers.refrigegobackend.ingredient.domain.IngredientCategory;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.IngredientRepository;
import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository.RecipeRepository;
import com.ohgiraffers.refrigegobackend.recommendation.domain.RecipeIngredient;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecipeRecommendationRequestDto;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecipeRecommendationResponseDto;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecommendedRecipeDto;
import com.ohgiraffers.refrigegobackend.recommendation.infrastructure.repository.RecipeIngredientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeRecommendationServiceTest {

    @Mock
    private RecipeIngredientRepository recipeIngredientRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeRecommendationService recipeRecommendationService;

    private Ingredient ingredient1, ingredient2, ingredient3;
    private Recipe recipe1, recipe2;

    @BeforeEach
    void setUp() {
        // 테스트용 식재료 생성
        ingredient1 = Ingredient.builder()
                .id(1L)
                .name("양파")
                .category(IngredientCategory.VEGETABLE)
                .build();

        ingredient2 = Ingredient.builder()
                .id(2L)
                .name("당근")
                .category(IngredientCategory.VEGETABLE)
                .build();

        ingredient3 = Ingredient.builder()
                .id(3L)
                .name("감자")
                .category(IngredientCategory.VEGETABLE)
                .build();

        // 테스트용 레시피 생성
        recipe1 = Recipe.builder()
                .rcpSeq("TEST001")
                .rcpNm("야채볶음")
                .rcpPartsDtls("양파, 당근, 감자를 넣고 볶습니다")
                .manual01("재료를 준비합니다")
                .manual02("볶아서 완성합니다")
                .image("test-image-url")
                .build();

        recipe2 = Recipe.builder()
                .rcpSeq("TEST002")
                .rcpNm("감자튀김")
                .rcpPartsDtls("감자를 튀겨서 만듭니다")
                .manual01("감자를 자릅니다")
                .image("test-image-url2")
                .build();
    }

    @Test
    @DisplayName("선택한 재료 기반으로 레시피를 추천한다")
    void recommendRecipes_Success() {
        // given
        List<String> selectedIngredients = List.of("양파", "당근");
        RecipeRecommendationRequestDto request = new RecipeRecommendationRequestDto(selectedIngredients, 10);

        // 재료명 → ID 변환 Mock
        given(ingredientRepository.findByNameIn(selectedIngredients))
                .willReturn(List.of(ingredient1, ingredient2));

        // DB 매칭 결과 Mock
        Object[] result1 = {"TEST001", "야채볶음", 3L, 2L, 66.67};
        List<Object[]> mockResults = Collections.singletonList(result1);
        when(recipeIngredientRepository.findRecipesByIngredientsWithMatchRatio(List.of(1L, 2L), 30.0))
                .thenReturn(mockResults);

        // 레시피 상세 정보 조회 Mock
        given(recipeRepository.findById("TEST001"))
                .willReturn(Optional.of(recipe1));

        // when
        RecipeRecommendationResponseDto response = recipeRecommendationService.recommendRecipes(request);

        // then
        assertThat(response.getRecommendedRecipes()).hasSize(1);
        
        RecommendedRecipeDto recommendedRecipe = response.getRecommendedRecipes().get(0);
        assertThat(recommendedRecipe.getRecipeId()).isEqualTo("TEST001");
        assertThat(recommendedRecipe.getRecipeName()).isEqualTo("야채볶음");
        assertThat(recommendedRecipe.getMatchedIngredientCount()).isEqualTo(2);
        assertThat(recommendedRecipe.getMatchScore()).isCloseTo(0.6667, within(0.01));

        // Mock 메서드 호출 검증
        verify(ingredientRepository).findByNameIn(selectedIngredients);
        verify(recipeIngredientRepository).findRecipesByIngredientsWithMatchRatio(List.of(1L, 2L), 30.0);
        verify(recipeRepository).findById("TEST001");
    }

    @Test
    @DisplayName("매칭되는 표준 재료가 없으면 빈 결과를 반환한다")
    void recommendRecipes_NoMatchingIngredients() {
        // given
        List<String> selectedIngredients = List.of("존재하지않는재료");
        RecipeRecommendationRequestDto request = new RecipeRecommendationRequestDto(selectedIngredients, 10);

        given(ingredientRepository.findByNameIn(selectedIngredients))
                .willReturn(List.of()); // 빈 결과

        // when
        RecipeRecommendationResponseDto response = recipeRecommendationService.recommendRecipes(request);

        // then
        assertThat(response.getRecommendedRecipes()).isEmpty();
        assertThat(response.getTotalCount()).isEqualTo(0);
        assertThat(response.getSelectedIngredients()).isEqualTo(selectedIngredients);
    }

    @Test
    @DisplayName("주재료 기반으로 레시피를 추천한다")
    void recommendByMainIngredients_Success() {
        // given
        List<String> ingredientNames = List.of("감자");

        given(ingredientRepository.findByNameIn(ingredientNames))
                .willReturn(List.of(ingredient3));

        RecipeIngredient recipeIngredient = RecipeIngredient.builder()
                .recipe(recipe2)
                .ingredient(ingredient3)
                .isMainIngredient(true)
                .build();

        given(recipeIngredientRepository.findRecipesByMainIngredients(List.of(3L)))
                .willReturn(List.of(recipeIngredient));

        // when
        RecipeRecommendationResponseDto response = recipeRecommendationService
                .recommendByMainIngredients(ingredientNames);

        // then
        assertThat(response.getRecommendedRecipes()).hasSize(1);
        
        RecommendedRecipeDto recommendedRecipe = response.getRecommendedRecipes().get(0);
        assertThat(recommendedRecipe.getRecipeId()).isEqualTo("TEST002");
        assertThat(recommendedRecipe.getRecipeName()).isEqualTo("감자튀김");
        assertThat(recommendedRecipe.getMatchScore()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("레시피 상세 정보를 조회한다")
    void getRecipeDetail_Success() {
        // given
        String recipeId = "TEST001";
        given(recipeRepository.findById(recipeId))
                .willReturn(Optional.of(recipe1));

        // when
        RecommendedRecipeDto result = recipeRecommendationService.getRecipeDetail(recipeId);

        // then
        assertThat(result.getRecipeId()).isEqualTo("TEST001");
        assertThat(result.getRecipeName()).isEqualTo("야채볶음");
        assertThat(result.getIngredients()).isEqualTo("양파, 당근, 감자를 넣고 볶습니다");
        assertThat(result.getCookingMethod1()).isEqualTo("재료를 준비합니다");
        assertThat(result.getCookingMethod2()).isEqualTo("볶아서 완성합니다");
        assertThat(result.getImageUrl()).isEqualTo("test-image-url");
    }

    @Test
    @DisplayName("존재하지 않는 레시피 ID로 조회 시 예외가 발생한다")
    void getRecipeDetail_NotFound() {
        // given
        String nonExistentRecipeId = "NOTEXIST";
        given(recipeRepository.findById(nonExistentRecipeId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> recipeRecommendationService.getRecipeDetail(nonExistentRecipeId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("레시피를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("요청 제한 개수가 적용된다")
    void recommendRecipes_WithLimit() {
        // given
        List<String> selectedIngredients = List.of("양파");
        RecipeRecommendationRequestDto request = new RecipeRecommendationRequestDto(selectedIngredients, 1); // 제한: 1개

        given(ingredientRepository.findByNameIn(selectedIngredients))
                .willReturn(List.of(ingredient1));

        Object[] result1 = {"TEST001", "야채볶음", 3L, 1L, 33.33};
        Object[] result2 = {"TEST002", "감자튀김", 1L, 1L, 100.0};
        List<Object[]> mockResults = Arrays.<Object[]>asList(result1, result2);
        when(recipeIngredientRepository.findRecipesByIngredientsWithMatchRatio(List.of(1L), 30.0))
                .thenReturn(mockResults);

        given(recipeRepository.findById("TEST001"))
                .willReturn(Optional.of(recipe1));

        // when
        RecipeRecommendationResponseDto response = recipeRecommendationService.recommendRecipes(request);

        // then
        assertThat(response.getRecommendedRecipes()).hasSize(1); // 제한된 개수만 반환
    }
}
