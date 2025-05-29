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

    /**
     * 재료 등록 API
     * POST /user-ingredients
     */
    @PostMapping
    public ResponseEntity<String> addUserIngredient(@RequestBody UserIngredientRequestDto dto,
                                                    @AuthenticationPrincipal CustomUserDetails user) {
        String username = user.getUsername();  // username 받아서
        service.addUserIngredient(username, dto);  // 서비스에 username 넘김
        return ResponseEntity.ok("재료가 성공적으로 등록되었습니다.");
    }

    /**
     * 유저 냉장고 재료 전체 조회 API
     * GET /user-ingredients
     */
    @GetMapping
    public ResponseEntity<List<UserIngredientResponseDto>> getUserIngredients(@AuthenticationPrincipal CustomUserDetails user) {
        String username = user.getUsername();
        List<UserIngredientResponseDto> list = service.getUserIngredientsByUsername(username);
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
    public ResponseEntity<String> addUserIngredientsBatch(
            @RequestBody UserIngredientBatchRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        String username = user.getUsername();
        service.saveBatchWithUsername(dto, username);
        return ResponseEntity.ok("재료가 일괄 등록되었습니다.");
    }

    /**
     * 유저 보유 재료 정보 수정 API
     * PUT /user-ingredients/{id}
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
        service.updateFrozenStatus(id, dto.getFrozen());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/dates")
    public ResponseEntity<Void> updateDates(@PathVariable Long id, @RequestBody UpdateDateDto dto) {
        service.updateDates(id, dto.getPurchaseDate(), dto.getExpiryDate());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/batch-add")
    public ResponseEntity<String> addUserIngredients(
            @RequestBody UserIngredientBatchRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        String username = user.getUsername();
        service.addIngredientsByUsername(username, requestDto.getIngredients());
        return ResponseEntity.ok("재료가 일괄 등록되었습니다.");
    }

    @PostMapping(value = "", consumes = "multipart/form-data")
    public ResponseEntity<String> addUserIngredientWithImage(
            @ModelAttribute UserIngredientCreateDto dto,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        if (user == null) {
            throw new RuntimeException("JWT 토큰이 없어서 인증 실패");
        }
        String username = user.getUsername();
        service.addUserIngredientWithImage(username, dto);
        return ResponseEntity.ok("이미지 포함 재료 등록 완료");
    }
}
