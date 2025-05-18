package com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository;

import com.ohgiraffers.refrigegobackend.ingredient.domain.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 기준 재료(ingredients) 테이블에 접근하는 JPA Repository
 * - JpaRepository 덕분에 CRUD 메서드 자동 제공됨
 * - ex) save(), findAll(), deleteById(), findById() 등
 */
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    /**
     * 이름 중복 검사용 메서드 (선택사항)
     * - 필요 없으면 안 써도 됨
     */
    boolean existsByName(String name);
}