package com.ohgiraffers.refrigegobackend.recommendation.infrastructure.repository;

import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recommendation.domain.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * RecipeIngredient Repository
 * - 레시피-식재료 연결 테이블 데이터 접근
 * - 추천 시스템에서 사용자 재료 기반 레시피 조회에 활용
 */
@Repository
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {

    /**
     * 특정 레시피의 모든 식재료 조회
     * @param recipeId 레시피 ID
     * @return 해당 레시피의 식재료 목록
     */
    @Query("SELECT ri FROM RecipeIngredient ri " +
           "JOIN FETCH ri.ingredient " +
           "WHERE ri.recipe.rcpSeq = :recipeId")
    List<RecipeIngredient> findByRecipeIdWithIngredient(@Param("recipeId") String recipeId);

    /**
     * 특정 식재료를 포함하는 모든 레시피 조회
     * @param ingredientId 식재료 ID
     * @return 해당 식재료를 포함하는 레시피 목록
     */
    @Query("SELECT ri FROM RecipeIngredient ri " +
           "JOIN FETCH ri.recipe " +
           "WHERE ri.ingredient.id = :ingredientId")
    List<RecipeIngredient> findByIngredientIdWithRecipe(@Param("ingredientId") Long ingredientId);

    /**
     * 사용자 재료 목록과 매칭되는 레시피 조회 (추천 시스템 핵심 쿼리)
     * @param ingredientIds 사용자가 보유한 식재료 ID 목록
     * @return 매칭 정보와 함께 레시피 목록 반환
     */
    @Query("SELECT ri.recipe.rcpSeq as recipeId, ri.recipe.rcpNm as recipeName, " +
           "COUNT(ri.ingredient.id) as totalIngredients, " +
           "COUNT(CASE WHEN ri.ingredient.id IN :ingredientIds THEN 1 END) as matchedIngredients, " +
           "(COUNT(CASE WHEN ri.ingredient.id IN :ingredientIds THEN 1 END) * 100.0 / COUNT(ri.ingredient.id)) as matchPercentage " +
           "FROM RecipeIngredient ri " +
           "GROUP BY ri.recipe.rcpSeq, ri.recipe.rcpNm " +
           "HAVING (COUNT(CASE WHEN ri.ingredient.id IN :ingredientIds THEN 1 END) * 100.0 / COUNT(ri.ingredient.id)) >= :minMatchPercentage " +
           "ORDER BY (COUNT(CASE WHEN ri.ingredient.id IN :ingredientIds THEN 1 END) * 100.0 / COUNT(ri.ingredient.id)) DESC")
    List<Object[]> findRecipesByIngredientsWithMatchRatio(@Param("ingredientIds") List<Long> ingredientIds,
                                                         @Param("minMatchPercentage") Double minMatchPercentage);

    /**
     * 주재료 기준으로 레시피 조회 (더 정확한 추천을 위해)
     * @param ingredientIds 사용자가 보유한 식재료 ID 목록
     * @return 주재료가 포함된 레시피 목록
     */
    @Query("SELECT ri FROM RecipeIngredient ri " +
           "JOIN FETCH ri.recipe " +
           "WHERE ri.ingredient.id IN :ingredientIds " +
           "AND ri.isMainIngredient = true")
    List<RecipeIngredient> findRecipesByMainIngredients(@Param("ingredientIds") List<Long> ingredientIds);

    /**
     * 특정 레시피의 식재료 개수 조회
     * @param recipeId 레시피 ID
     * @return 해당 레시피의 총 식재료 개수
     */
    @Query("SELECT COUNT(ri) FROM RecipeIngredient ri WHERE ri.recipe.rcpSeq = :recipeId")
    Long countIngredientsByRecipeId(@Param("recipeId") String recipeId);

    // 기준 레시피의 주재료 아이디 목록 조회
    @Query("""
        SELECT ri.ingredient.id
        FROM RecipeIngredient ri
        WHERE ri.recipe.rcpSeq = :recipeId
          AND ri.isMainIngredient = true
    """)
    List<Long> findMainIngredientIdsByRecipeId(@Param("recipeId") String recipeId);

    // 해당 주재료를 사용하는 다른 레시피 조회 (기준 레시피 제외)
    @Query("""
        SELECT DISTINCT ri.recipe
        FROM RecipeIngredient ri
        WHERE ri.isMainIngredient = true
          AND ri.ingredient.id IN :ingredientIds
          AND ri.recipe.rcpSeq <> :recipeId
    """)
    List<Recipe> findRecipesByMainIngredientIds(@Param("ingredientIds") List<Long> ingredientIds,
                                                @Param("recipeId") String recipeId);

    /**
     * 제철 재료명과 조리법을 기준으로 레시피 조회 (날씨 기반 추천용)
     * @param ingredientNames 제철 재료명 목록
     * @param cookingTypes 날씨에 맞는 조리법 목록
     * @return 조건에 맞는 레시피 목록
     */
    @Query("""
        SELECT DISTINCT ri.recipe
        FROM RecipeIngredient ri
        WHERE ri.ingredient.name IN :ingredientNames
        AND ri.recipe.cuisineType IN :cookingTypes
        ORDER BY ri.recipe.rcpNm
    """)
    List<Recipe> findRecipesBySeasonalIngredientsAndCookingTypes(
            @Param("ingredientNames") List<String> ingredientNames,
            @Param("cookingTypes") List<String> cookingTypes);

    /**
     * 사용자 재료명과 특정 레시피 ID 목록에서 매칭되는 레시피 조회
     * @param ingredientNames 사용자가 보유한 재료명 목록
     * @param recipeIds 검색 대상 레시피 ID 목록
     * @return 매칭되는 레시피 목록
     */
    @Query("""
        SELECT DISTINCT ri.recipe
        FROM RecipeIngredient ri
        WHERE ri.ingredient.name IN :ingredientNames
        AND ri.recipe.rcpSeq IN :recipeIds
        ORDER BY ri.recipe.rcpNm
    """)
    List<Recipe> findRecipesByIngredientsAndRecipeIds(
            @Param("ingredientNames") List<String> ingredientNames,
            @Param("recipeIds") List<String> recipeIds);

}
