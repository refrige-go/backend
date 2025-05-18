package com.ohgiraffers.refrigegobackend.ingredients.service;

import com.ohgiraffers.refrigegobackend.ingredients.domain.Ingredient;
import com.ohgiraffers.refrigegobackend.ingredients.dto.IngredientResponseDto;
import com.ohgiraffers.refrigegobackend.ingredients.infrastructure.repository.IngredientRepository;
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
     * 전체 기준 재료 조회
     */
    public List<IngredientResponseDto> getAllIngredients() {
        return ingredientRepository.findAll().stream()
                .map(IngredientResponseDto::new)
                .collect(Collectors.toList());
    }
}