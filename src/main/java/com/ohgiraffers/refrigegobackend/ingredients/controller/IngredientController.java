package com.ohgiraffers.refrigegobackend.ingredients.controller;

import com.ohgiraffers.refrigegobackend.ingredients.dto.IngredientResponseDto;
import com.ohgiraffers.refrigegobackend.ingredients.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     */
    @GetMapping
    public ResponseEntity<List<IngredientResponseDto>> getAllIngredients() {
        List<IngredientResponseDto> list = ingredientService.getAllIngredients();
        return ResponseEntity.ok(list);
    }
}