package com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository;

import com.ohgiraffers.refrigegobackend.ingredient.domain.UserIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 유저 보유 재료(user_ingredients) 테이블에 접근하는 JPA Repository
 */
public interface UserIngredientRepository extends JpaRepository<UserIngredient, Long> {

    /**
     * 특정 유저의 냉장고 재료 전부 조회
     */
    List<UserIngredient> findByUserId(String userId);
}