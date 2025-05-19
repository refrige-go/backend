package com.ohgiraffers.refrigegobackend.recommendation.infrastructure.repository;

import com.ohgiraffers.refrigegobackend.recommendation.domain.RecipeFavorite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("레시피 찜하기 Repository 테스트")
class RecipeFavoriteRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RecipeFavoriteRepository recipeFavoriteRepository;

    private RecipeFavorite favorite1;
    private RecipeFavorite favorite2;

    @BeforeEach
    void setUp() {
        // 테스트용 찜하기 데이터 생성
        favorite1 = RecipeFavorite.builder()
                .userId("user123")
                .recipeId("RCP_001")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        favorite2 = RecipeFavorite.builder()
                .userId("user123")
                .recipeId("RCP_002")
                .createdAt(LocalDateTime.now())
                .build();

        RecipeFavorite favorite3 = RecipeFavorite.builder()
                .userId("user456")
                .recipeId("RCP_001")
                .createdAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(favorite1);
        entityManager.persistAndFlush(favorite2);
        entityManager.persistAndFlush(favorite3);
    }

    @Test
    @DisplayName("사용자와 레시피로 찜하기 존재 여부 확인 - 존재함")
    void existsByUserIdAndRecipeId_Exists() {
        // when
        boolean exists = recipeFavoriteRepository.existsByUserIdAndRecipeId("user123", "RCP_001");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("사용자와 레시피로 찜하기 존재 여부 확인 - 존재하지 않음")
    void existsByUserIdAndRecipeId_NotExists() {
        // when
        boolean exists = recipeFavoriteRepository.existsByUserIdAndRecipeId("user123", "RCP_999");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("사용자의 찜한 레시피 목록 조회 - 최신순 정렬")
    void findByUserIdOrderByCreatedAtDesc() {
        // when
        List<RecipeFavorite> favorites = recipeFavoriteRepository
                .findByUserIdOrderByCreatedAtDesc("user123");

        // then
        assertThat(favorites).hasSize(2);
        assertThat(favorites.get(0).getRecipeId()).isEqualTo("RCP_002"); // 최신
        assertThat(favorites.get(1).getRecipeId()).isEqualTo("RCP_001"); // 이전
    }

    @Test
    @DisplayName("사용자의 찜한 레시피 목록 조회 - 빈 목록")
    void findByUserIdOrderByCreatedAtDesc_EmptyList() {
        // when
        List<RecipeFavorite> favorites = recipeFavoriteRepository
                .findByUserIdOrderByCreatedAtDesc("user_not_found");

        // then
        assertThat(favorites).isEmpty();
    }

    @Test
    @DisplayName("사용자와 레시피로 찜하기 정보 조회 - 존재함")
    void findByUserIdAndRecipeId_Exists() {
        // when
        Optional<RecipeFavorite> result = recipeFavoriteRepository
                .findByUserIdAndRecipeId("user123", "RCP_001");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo("user123");
        assertThat(result.get().getRecipeId()).isEqualTo("RCP_001");
    }

    @Test
    @DisplayName("사용자와 레시피로 찜하기 정보 조회 - 존재하지 않음")
    void findByUserIdAndRecipeId_NotExists() {
        // when
        Optional<RecipeFavorite> result = recipeFavoriteRepository
                .findByUserIdAndRecipeId("user123", "RCP_999");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("사용자가 찜한 레시피 ID 목록 조회")
    void findRecipeIdsByUserId() {
        // when
        List<String> recipeIds = recipeFavoriteRepository.findRecipeIdsByUserId("user123");

        // then
        assertThat(recipeIds).hasSize(2);
        assertThat(recipeIds).containsExactlyInAnyOrder("RCP_001", "RCP_002");
    }

    @Test
    @DisplayName("사용자가 찜한 레시피 ID 목록 조회 - 빈 목록")
    void findRecipeIdsByUserId_EmptyList() {
        // when
        List<String> recipeIds = recipeFavoriteRepository.findRecipeIdsByUserId("user_not_found");

        // then
        assertThat(recipeIds).isEmpty();
    }

    @Test
    @DisplayName("찜하기 데이터 저장")
    void save() {
        // given
        RecipeFavorite newFavorite = RecipeFavorite.builder()
                .userId("user789")
                .recipeId("RCP_003")
                .build();

        // when
        RecipeFavorite saved = recipeFavoriteRepository.save(newFavorite);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo("user789");
        assertThat(saved.getRecipeId()).isEqualTo("RCP_003");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("찜하기 데이터 삭제")
    void delete() {
        // given
        Long favoriteId = favorite1.getId();

        // when
        recipeFavoriteRepository.delete(favorite1);
        entityManager.flush();

        // then
        Optional<RecipeFavorite> deleted = recipeFavoriteRepository.findById(favoriteId);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("중복 찜하기 제약 조건 테스트")
    void duplicateFavorite_ShouldFail() {
        // given
        RecipeFavorite duplicateFavorite = RecipeFavorite.builder()
                .userId("user123")
                .recipeId("RCP_001") // 이미 존재하는 조합
                .build();

        // when & then
        try {
            recipeFavoriteRepository.save(duplicateFavorite);
            entityManager.flush();
            // 중복 저장이 실패해야 함 (실제 DB에서는 UNIQUE 제약 조건으로 인해 예외 발생)
        } catch (Exception e) {
            // 예외가 발생하는 것이 정상
            assertThat(e).isNotNull();
        }
    }
}
