package com.ohgiraffers.refrigegobackend.recommendation.infrastructure.repository;

import com.ohgiraffers.refrigegobackend.recommendation.domain.RecipeFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 레시피 찜하기 정보에 접근하는 JPA Repository
 */
public interface RecipeFavoriteRepository extends JpaRepository<RecipeFavorite, Long> {

    /**
     * 특정 사용자가 특정 레시피를 찜했는지 확인
     */
    boolean existsByUserIdAndRecipeId(String userId, String recipeId);

    /**
     * 특정 사용자가 찜한 모든 레시피 조회
     */
    List<RecipeFavorite> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * 특정 사용자의 특정 레시피 찜하기 정보 조회
     */
    Optional<RecipeFavorite> findByUserIdAndRecipeId(String userId, String recipeId);

    /**
     * 특정 사용자가 찜한 레시피 ID 목록 조회
     */
    @Query("SELECT rf.recipeId FROM RecipeFavorite rf WHERE rf.userId = :userId")
    List<String> findRecipeIdsByUserId(@Param("userId") String userId);
}
