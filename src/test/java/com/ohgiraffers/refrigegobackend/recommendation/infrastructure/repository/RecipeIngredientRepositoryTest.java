package com.ohgiraffers.refrigegobackend.recommendation.infrastructure.repository;

import com.ohgiraffers.refrigegobackend.ingredient.domain.Ingredient;
import com.ohgiraffers.refrigegobackend.ingredient.domain.IngredientCategory;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.IngredientRepository;
import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository.RecipeRepository;
import com.ohgiraffers.refrigegobackend.recommendation.domain.RecipeIngredient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DataJpaTest
@ActiveProfiles("test")
class RecipeIngredientRepositoryTest {

    @Autowired
    private RecipeIngredientRepository recipeIngredientRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    private Recipe recipe1, recipe2, recipe3;
    private Ingredient ingredient1, ingredient2, ingredient3, ingredient4;

    @BeforeEach
    void setUp() {
        // 테스트용 식재료 생성
        ingredient1 = Ingredient.builder()
                .name("양파")
                .category(IngredientCategory.VEGETABLE)
                .defaultExpiryDays(30)
                .storageMethod("실온")
                .build();

        ingredient2 = Ingredient.builder()
                .name("당근")
                .category(IngredientCategory.VEGETABLE)
                .defaultExpiryDays(21)
                .storageMethod("냉장")
                .build();

        ingredient3 = Ingredient.builder()
                .name("감자")
                .category(IngredientCategory.VEGETABLE)
                .defaultExpiryDays(30)
                .storageMethod("실온")
                .build();

        ingredient4 = Ingredient.builder()
                .name("소고기")
                .category(IngredientCategory.MEAT)
                .defaultExpiryDays(3)
                .storageMethod("냉장")
                .build();

        ingredientRepository.saveAll(List.of(ingredient1, ingredient2, ingredient3, ingredient4));

        // 테스트용 레시피 생성
        recipe1 = Recipe.builder()
                .rcpSeq("TEST001")
                .rcpNm("야채볶음")
                .cuisineType("한식")
                .rcpCategory("볶음")
                .build();

        recipe2 = Recipe.builder()
                .rcpSeq("TEST002")
                .rcpNm("소고기볶음")
                .cuisineType("한식")
                .rcpCategory("볶음")
                .build();

        recipe3 = Recipe.builder()
                .rcpSeq("TEST003")
                .rcpNm("감자튀김")
                .cuisineType("양식")
                .rcpCategory("튀김")
                .build();

        recipeRepository.saveAll(List.of(recipe1, recipe2, recipe3));

        // 레시피-식재료 연결 데이터 생성
        // recipe1(야채볶음): 양파, 당근, 감자
        RecipeIngredient ri1 = RecipeIngredient.builder()
                .recipe(recipe1)
                .ingredient(ingredient1)
                .isMainIngredient(true)
                .build();

        RecipeIngredient ri2 = RecipeIngredient.builder()
                .recipe(recipe1)
                .ingredient(ingredient2)
                .isMainIngredient(false)
                .build();

        RecipeIngredient ri3 = RecipeIngredient.builder()
                .recipe(recipe1)
                .ingredient(ingredient3)
                .isMainIngredient(false)
                .build();

        // recipe2(소고기볶음): 소고기, 양파
        RecipeIngredient ri4 = RecipeIngredient.builder()
                .recipe(recipe2)
                .ingredient(ingredient4)
                .isMainIngredient(true)
                .build();

        RecipeIngredient ri5 = RecipeIngredient.builder()
                .recipe(recipe2)
                .ingredient(ingredient1)
                .isMainIngredient(false)
                .build();

        // recipe3(감자튀김): 감자
        RecipeIngredient ri6 = RecipeIngredient.builder()
                .recipe(recipe3)
                .ingredient(ingredient3)
                .isMainIngredient(true)
                .build();

        recipeIngredientRepository.saveAll(List.of(ri1, ri2, ri3, ri4, ri5, ri6));
    }

    @Test
    @DisplayName("특정 레시피의 모든 식재료를 조회한다")
    void findByRecipeIdWithIngredient() {
        // given
        String recipeId = "TEST001";

        // when
        List<RecipeIngredient> result = recipeIngredientRepository.findByRecipeIdWithIngredient(recipeId);

        // then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(ri -> ri.getIngredient().getName())
                .containsExactlyInAnyOrder("양파", "당근", "감자");
    }

    @Test
    @DisplayName("특정 식재료를 포함하는 모든 레시피를 조회한다")
    void findByIngredientIdWithRecipe() {
        // given
        Long ingredientId = ingredient1.getId(); // 양파

        // when
        List<RecipeIngredient> result = recipeIngredientRepository.findByIngredientIdWithRecipe(ingredientId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ri -> ri.getRecipe().getRcpNm())
                .containsExactlyInAnyOrder("야채볶음", "소고기볶음");
    }

    @Test
    @DisplayName("사용자 재료 목록과 매칭되는 레시피를 매칭 비율과 함께 조회한다")
    void findRecipesByIngredientsWithMatchRatio() {
        // given
        List<Long> userIngredientIds = List.of(ingredient1.getId(), ingredient2.getId()); // 양파, 당근
        Double minMatchPercentage = 50.0;

        // when
        List<Object[]> results = recipeIngredientRepository
                .findRecipesByIngredientsWithMatchRatio(userIngredientIds, minMatchPercentage);

        // then
        assertThat(results).hasSize(2); // 야채볶음(66.7%), 소고기볶음(50%)

        // 첫 번째 결과 (야채볶음 - 매칭률이 더 높음)
        Object[] firstResult = results.get(0);
        assertThat(firstResult[0]).isEqualTo("TEST001"); // recipeId
        assertThat(firstResult[1]).isEqualTo("야채볶음");   // recipeName
        assertThat(firstResult[2]).isEqualTo(3L);        // totalIngredients
        assertThat(firstResult[3]).isEqualTo(2L);        // matchedIngredients
        assertThat((Double) firstResult[4]).isCloseTo(66.67, within(0.1)); // matchPercentage

        // 두 번째 결과 (소고기볶음)
        Object[] secondResult = results.get(1);
        assertThat(secondResult[0]).isEqualTo("TEST002"); // recipeId
        assertThat(secondResult[1]).isEqualTo("소고기볶음"); // recipeName
        assertThat(secondResult[2]).isEqualTo(2L);        // totalIngredients
        assertThat(secondResult[3]).isEqualTo(1L);        // matchedIngredients
        assertThat((Double) secondResult[4]).isCloseTo(50.0, within(0.1)); // matchPercentage
    }

    @Test
    @DisplayName("매칭 비율이 기준치 미만인 레시피는 조회되지 않는다")
    void findRecipesByIngredientsWithMatchRatio_BelowThreshold() {
        // given
        List<Long> userIngredientIds = List.of(ingredient3.getId()); // 감자만
        Double minMatchPercentage = 80.0; // 80% 이상

        // when
        List<Object[]> results = recipeIngredientRepository
                .findRecipesByIngredientsWithMatchRatio(userIngredientIds, minMatchPercentage);

        // then
        assertThat(results).hasSize(1); // 감자튀김만 (100% 매칭)
        
        Object[] result = results.get(0);
        assertThat(result[0]).isEqualTo("TEST003"); // 감자튀김
        assertThat((Double) result[4]).isCloseTo(100.0, within(0.1));
    }

    @Test
    @DisplayName("주재료 기준으로 레시피를 조회한다")
    void findRecipesByMainIngredients() {
        // given
        List<Long> ingredientIds = List.of(ingredient1.getId(), ingredient3.getId()); // 양파, 감자

        // when
        List<RecipeIngredient> results = recipeIngredientRepository.findRecipesByMainIngredients(ingredientIds);

        // then
        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(ri -> ri.getRecipe().getRcpNm())
                .containsExactlyInAnyOrder("야채볶음", "감자튀김");
        
        // 모두 주재료여야 함
        assertThat(results)
                .allMatch(RecipeIngredient::getIsMainIngredient);
    }

    @Test
    @DisplayName("특정 레시피의 식재료 개수를 조회한다")
    void countIngredientsByRecipeId() {
        // given
        String recipeId = "TEST001"; // 야채볶음 (양파, 당근, 감자)

        // when
        Long count = recipeIngredientRepository.countIngredientsByRecipeId(recipeId);

        // then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("존재하지 않는 레시피의 식재료 개수는 0을 반환한다")
    void countIngredientsByRecipeId_NotExists() {
        // given
        String nonExistentRecipeId = "NOTEXIST";

        // when
        Long count = recipeIngredientRepository.countIngredientsByRecipeId(nonExistentRecipeId);

        // then
        assertThat(count).isEqualTo(0L);
    }

    @Test
    @DisplayName("사용자가 재료를 하나도 가지고 있지 않으면 빈 결과를 반환한다")
    void findRecipesByIngredientsWithMatchRatio_NoMatch() {
        // given
        List<Long> emptyIngredientIds = List.of(999L); // 존재하지 않는 재료 ID
        Double minMatchPercentage = 10.0;

        // when
        List<Object[]> results = recipeIngredientRepository
                .findRecipesByIngredientsWithMatchRatio(emptyIngredientIds, minMatchPercentage);

        // then
        assertThat(results).isEmpty();
    }
}
