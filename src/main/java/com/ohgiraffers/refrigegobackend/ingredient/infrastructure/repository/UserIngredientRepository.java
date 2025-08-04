package com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository;

import com.ohgiraffers.refrigegobackend.ingredient.domain.UserIngredient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDate;
import java.util.List;

/**
 * 유저 보유 재료(user_ingredients) 테이블에 접근하는 JPA Repository
 */
public interface UserIngredientRepository extends JpaRepository<UserIngredient, Long> {

    /**
     * 특정 유저의 냉장고 재료 전부 조회 (N+1 쿼리 방지를 위해 Ingredient와 Category를 함께 조회)
     */
    @Cacheable(value = "userIngredients", key = "#userId")
    @EntityGraph(value = "UserIngredient.withIngredientAndCategory")
    List<UserIngredient> findByUserId(@Param("userId") Long userId);

    /**
     * 특정 유저의 특정 재료들 조회 (재료 소비용)
     */
    List<UserIngredient> findByUserIdAndIdIn(Long userId, List<Long> ingredientIds);

    @Query("SELECT i FROM UserIngredient i WHERE i.expiryDate <= :targetDate AND i.expiryDate >= :today")
    List<UserIngredient> findExpiringIngredients(@Param("targetDate") LocalDate targetDate,
            @Param("today") LocalDate today);
}