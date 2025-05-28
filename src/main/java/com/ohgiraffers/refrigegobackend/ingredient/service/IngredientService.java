package com.ohgiraffers.refrigegobackend.ingredient.service;

import com.ohgiraffers.refrigegobackend.ingredient.domain.Ingredient;
import com.ohgiraffers.refrigegobackend.ingredient.domain.IngredientCategory;
import com.ohgiraffers.refrigegobackend.ingredient.dto.IngredientResponseDto;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 기준 재료 관련 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    /**
     * 카테고리별 기준 재료 조회
     * @param category 조회할 카테고리명 (null 또는 "전체"면 전체 조회)
     * @return 해당 카테고리 기준 재료 리스트
     */
    public List<Ingredient> getIngredientsByCategory(String category) {
        if (category == null || category.isEmpty() || category.equals("전체")) {
            return ingredientRepository.findAll();  // 전체 조회
        }

        // 문자열을 Enum으로 변환
        IngredientCategory enumCategory = IngredientCategory.fromDisplayName(category);
        return ingredientRepository.findByCategory(enumCategory);
    }

    public List<String> getCategoryList() {
        return ingredientRepository.findDistinctCategories();
    }
}