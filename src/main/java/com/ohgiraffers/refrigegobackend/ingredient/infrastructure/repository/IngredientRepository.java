package com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository;

import com.ohgiraffers.refrigegobackend.ingredient.domain.Ingredient;
import com.ohgiraffers.refrigegobackend.ingredient.domain.IngredientCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

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

    // category로 재료 목록 조회 (null이나 empty는 전체 조회는 서비스에서 처리)
    List<Ingredient> findByCategory(IngredientCategory category);

    @Query("SELECT DISTINCT i.category FROM Ingredient i ORDER BY i.category")
    List<String> findDistinctCategories();

    Optional<Ingredient> findByName(String name);
}