package com.ohgiraffers.refrigegobackend.ingredient.controller;

import com.ohgiraffers.refrigegobackend.ingredient.dto.UserIngredientRequestDto;
import com.ohgiraffers.refrigegobackend.ingredient.dto.UserIngredientResponseDto;
import com.ohgiraffers.refrigegobackend.ingredient.service.UserIngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 유저 보유 재료 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/user-ingredients")
@RequiredArgsConstructor
public class UserIngredientController {

    private final UserIngredientService service;

    /**
     * 재료 등록 API
     * POST /user-ingredients
     */
    @PostMapping
    public ResponseEntity<String> addUserIngredient(@RequestBody UserIngredientRequestDto dto) {
        service.addUserIngredient(dto);
        return ResponseEntity.ok("재료가 성공적으로 등록되었습니다.");
    }

    /**
     * 유저 냉장고 재료 전체 조회 API
     * GET /user-ingredients?userId=uuid-be-001
     */
    @GetMapping
    public ResponseEntity<List<UserIngredientResponseDto>> getUserIngredients(@RequestParam String userId) {
        List<UserIngredientResponseDto> list = service.getUserIngredients(userId);
        return ResponseEntity.ok(list);
    }

    /**
     * 보유 재료 삭제 API
     * DELETE /user-ingredients/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUserIngredient(@PathVariable Long id) {
        service.deleteUserIngredient(id);
        return ResponseEntity.ok("재료가 삭제되었습니다.");
    }
}