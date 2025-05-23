package com.ohgiraffers.refrigegobackend.ingredient.controller;

import com.ohgiraffers.refrigegobackend.ingredient.dto.*;
import com.ohgiraffers.refrigegobackend.ingredient.service.UserIngredientService;
import com.ohgiraffers.refrigegobackend.user.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final UserIngredientService userIngredientService;

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
    public ResponseEntity<List<UserIngredientResponseDto>> getUserIngredients(@RequestParam Long userId) {
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

    /**
     * 보유 재료 일괄 등록 API
     * POST /user-ingredients/batch
     */
    @PostMapping("/batch")
    public ResponseEntity<String> addUserIngredientsBatch(@RequestBody UserIngredientBatchRequestDto dto) {
        service.saveBatch(dto);
        return ResponseEntity.ok("재료가 일괄 등록되었습니다.");
    }

    /**
     * 유저 보유 재료 정보 수정 API
     * PUT /user-ingredients/{id}
     *
     * @param id 수정할 유저 재료 ID
     * @param dto 수정할 재료 정보 DTO
     * @return 수정 성공 메시지 반환
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateUserIngredient(@PathVariable Long id,
                                                       @RequestBody UserIngredientUpdateRequestDto dto) {

        service.updateUserIngredient(id, dto);
        return ResponseEntity.ok("재료 정보가 수정되었습니다.");
    }

    /**
     * 유저 보유 재료 상세 조회 API
     * GET /user-ingredients/{id}
     *
     * @param id 조회할 유저 재료 ID
     * @return 상세 정보가 담긴 DTO 반환
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserIngredientResponseDto> getUserIngredientDetail(@PathVariable Long id) {
        UserIngredientResponseDto dto = service.getUserIngredientDetail(id);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{id}/frozen")
    public ResponseEntity<Void> updateFrozenStatus(
            @PathVariable Long id,
            @RequestBody UpdateFrozenDto dto
    ) {
        userIngredientService.updateFrozenStatus(id, dto.getFrozen());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/dates")
    public ResponseEntity<Void> updateDates(@PathVariable Long id, @RequestBody UpdateDateDto dto) {
        userIngredientService.updateDates(id, dto.getPurchaseDate(), dto.getExpiryDate());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/batch-add")
    public ResponseEntity<?> addUserIngredients(
            @RequestBody IngredientAddRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        userIngredientService.addIngredients(
                user.getId(),
                requestDto.getIngredientIds()
        );
        return ResponseEntity.ok().build();
    }

}