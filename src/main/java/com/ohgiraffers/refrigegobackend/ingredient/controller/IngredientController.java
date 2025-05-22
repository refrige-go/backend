package com.ohgiraffers.refrigegobackend.ingredient.controller;

import com.ohgiraffers.refrigegobackend.ingredient.domain.Ingredient;
import com.ohgiraffers.refrigegobackend.ingredient.dto.IngredientResponseDto;
import com.ohgiraffers.refrigegobackend.ingredient.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 기준 재료 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    /**
     * 전체 기준 재료 조회 API
     * GET /ingredients
     *
     * @param category 카테고리명 (옵션, null 또는 "전체"일 경우 전체 조회)
     * @return 해당 카테고리에 속한 재료 목록 (DTO 리스트)
     */
    @GetMapping
    public ResponseEntity<List<IngredientResponseDto>> getIngredients(@RequestParam(required = false) String category) {
        List<Ingredient> ingredients = ingredientService.getIngredientsByCategory(category);
        // Ingredient -> DTO 변환
        List<IngredientResponseDto> dtoList = ingredients.stream()
                .map(IngredientResponseDto::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }
}