package com.ohgiraffers.refrigegobackend.ingredient.service;

import com.ohgiraffers.refrigegobackend.ingredient.domain.Ingredient;
import com.ohgiraffers.refrigegobackend.ingredient.domain.IngredientCategory;
import com.ohgiraffers.refrigegobackend.ingredient.dto.IngredientCreateRequestDto;
import com.ohgiraffers.refrigegobackend.ingredient.dto.IngredientResponseDto;
import com.ohgiraffers.refrigegobackend.ingredient.dto.IngredientUpdateRequestDto;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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
        // displayName 목록 반환
        return Arrays.stream(IngredientCategory.values())
                .map(IngredientCategory::getDisplayName)
                .collect(Collectors.toList());
    }

    // 특정 식재료 조회

    public IngredientResponseDto getIngredientById(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("식재료를 찾을 수 없습니다."));
        return new IngredientResponseDto(ingredient);
    }

    //식재료 추가
    public IngredientResponseDto createIngredient(IngredientCreateRequestDto dto){
        Ingredient ingredient = new Ingredient();
        ingredient.setName(dto.getName());
        ingredient.setCategory(dto.getCategoryEnum());
        ingredient.setDefaultExpiryDays(dto.getDefaultExpiryDays());
        ingredient.setStorageMethod(dto.getStorageMethod());
        ingredient.setImageUrl(dto.getImageUrl());
        ingredientRepository.save(ingredient);
        return new IngredientResponseDto(ingredient);
    }

    //식재료 수정
    public IngredientResponseDto updateIngredient(Long id, IngredientUpdateRequestDto dto) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("식재료를 찾을 수 없습니다."));
        ingredient.setName(dto.getName());
        ingredient.setCategory(dto.getCategoryEnum());
        ingredient.setDefaultExpiryDays(dto.getDefaultExpiryDays());
        ingredient.setStorageMethod(dto.getStorageMethod());
        ingredient.setImageUrl(dto.getImageUrl());
        ingredientRepository.save(ingredient);
        return new IngredientResponseDto(ingredient);
    }

    //식재료 삭제
    public void deleteIngredient(Long id) {
        ingredientRepository.deleteById(id);
    }

}