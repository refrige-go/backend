package com.ohgiraffers.refrigegobackend.recommendation.service;

import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository.RecipeRepository;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecipeRecommendationRequestDto;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecipeRecommendationResponseDto;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecommendedRecipeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("레시피 추천 서비스 테스트")
class RecipeRecommendationServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeRecommendationService recommendationService;

    private Recipe recipe1;
    private Recipe recipe2;
    private Recipe recipe3;

    @BeforeEach
    void setUp() {
        // 테스트용 레시피 데이터 생성
        recipe1 = new Recipe();
        recipe1.setRcpSeq("RCP_001");
        recipe1.setRcpNm("브로콜리 볶음");
        recipe1.setRcpPartsDtls("브로콜리 200g, 양파 1개, 마늘 2쪽, 소금, 후추");
        recipe1.setManual01("브로콜리를 끓는 물에 데친다.");
        recipe1.setManual02("팬에 기름을 두르고 양파와 마늘을 볶는다.");

        recipe2 = new Recipe();
        recipe2.setRcpSeq("RCP_002");
        recipe2.setRcpNm("계란 볶음밥");
        recipe2.setRcpPartsDtls("계란 2개, 밥 1공기, 양파 1/2개, 대파, 간장");
        recipe2.setManual01("계란을 풀어서 스크램블을 만든다.");
        recipe2.setManual02("팬에 밥과 계란을 넣고 볶는다.");

        recipe3 = new Recipe();
        recipe3.setRcpSeq("RCP_003");
        recipe3.setRcpNm("토마토 파스타");
        recipe3.setRcpPartsDtls("파스타면 100g, 토마토소스, 마늘, 올리브오일");
        recipe3.setManual01("파스타면을 삶는다.");
        recipe3.setManual02("토마토소스와 마늘을 볶아 소스를 만든다.");
    }

    @Test
    @DisplayName("재료 기반 레시피 추천 - 성공")
    void recommendRecipes_Success() {
        // given
        RecipeRecommendationRequestDto requestDto = new RecipeRecommendationRequestDto();
        requestDto.setUserId("user123");
        requestDto.setSelectedIngredients(Arrays.asList("브로콜리", "양파", "계란"));
        requestDto.setLimit(10);

        List<Recipe> allRecipes = Arrays.asList(recipe1, recipe2, recipe3);

        given(recipeRepository.findAll()).willReturn(allRecipes);

        // when
        RecipeRecommendationResponseDto response = recommendationService.recommendRecipes(requestDto);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRecommendedRecipes()).hasSize(2); // recipe1, recipe2만 매칭
        assertThat(response.getTotalCount()).isEqualTo(2);
        assertThat(response.getSelectedIngredients()).isEqualTo(requestDto.getSelectedIngredients());

        // 첫 번째 레시피가 브로콜리 볶음인지 확인 (2개 재료 매칭)
        RecommendedRecipeDto firstRecommended = response.getRecommendedRecipes().get(0);
        assertThat(firstRecommended.getRecipeId()).isEqualTo("RCP_001");
        assertThat(firstRecommended.getMatchedIngredientCount()).isEqualTo(2);
        assertThat(firstRecommended.isFavorite()).isFalse(); // recipe_bookmarks에서 관리

        // 두 번째 레시피가 계란 볶음밥인지 확인 (2개 재료 매칭)
        RecommendedRecipeDto secondRecommended = response.getRecommendedRecipes().get(1);
        assertThat(secondRecommended.getRecipeId()).isEqualTo("RCP_002");
        assertThat(secondRecommended.getMatchedIngredientCount()).isEqualTo(2);
        assertThat(secondRecommended.isFavorite()).isFalse(); // recipe_bookmarks에서 관리
    }

    @Test
    @DisplayName("재료 기반 레시피 추천 - 매칭된 레시피 없음")
    void recommendRecipes_NoMatch() {
        // given
        RecipeRecommendationRequestDto requestDto = new RecipeRecommendationRequestDto();
        requestDto.setUserId("user123");
        requestDto.setSelectedIngredients(Arrays.asList("딸기", "바나나"));
        requestDto.setLimit(10);

        List<Recipe> allRecipes = Arrays.asList(recipe1, recipe2, recipe3);

        given(recipeRepository.findAll()).willReturn(allRecipes);

        // when
        RecipeRecommendationResponseDto response = recommendationService.recommendRecipes(requestDto);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRecommendedRecipes()).isEmpty();
        assertThat(response.getTotalCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("레시피 상세 조회 - 성공")
    void getRecipeDetail_Success() {
        // given
        String recipeId = "RCP_001";

        given(recipeRepository.findById(recipeId))
                .willReturn(Optional.of(recipe1));

        // when
        RecommendedRecipeDto result = recommendationService.getRecipeDetail(recipeId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRecipeId()).isEqualTo("RCP_001");
        assertThat(result.getRecipeName()).isEqualTo("브로콜리 볶음");
        assertThat(result.isFavorite()).isFalse(); // recipe_bookmarks에서 관리
    }

    @Test
    @DisplayName("레시피 상세 조회 - 레시피 없음")
    void getRecipeDetail_NotFound() {
        // given
        String recipeId = "INVALID_ID";

        given(recipeRepository.findById(recipeId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> recommendationService.getRecipeDetail(recipeId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("레시피를 찾을 수 없습니다. ID: INVALID_ID");
    }
}
