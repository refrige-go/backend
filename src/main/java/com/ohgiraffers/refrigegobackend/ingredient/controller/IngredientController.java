package com.ohgiraffers.refrigegobackend.ingredient.controller;

import com.ohgiraffers.refrigegobackend.ingredient.domain.Ingredient;
import com.ohgiraffers.refrigegobackend.ingredient.dto.IngredientCreateRequestDto;
import com.ohgiraffers.refrigegobackend.ingredient.dto.IngredientResponseDto;
import com.ohgiraffers.refrigegobackend.ingredient.dto.IngredientUpdateRequestDto;
import com.ohgiraffers.refrigegobackend.ingredient.service.IngredientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 기준 재료 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
@Slf4j
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
        try {
            System.out.println("=== API 호출됨 ===");

            List<Ingredient> ingredients = ingredientService.getIngredientsByCategory(category);
            System.out.println("=== Service 호출 완료, 데이터 개수: " + ingredients.size() + " ===");

            List<IngredientResponseDto> dtoList = ingredients.stream()
                    .map(ingredient -> {
                        try {
                            return new IngredientResponseDto(ingredient);
                        } catch (Exception e) {
                            System.err.println("DTO 변환 오류: " + e.getMessage());
                            throw e;
                        }
                    })
                    .collect(Collectors.toList());

            System.out.println("=== DTO 변환 완료 ===");
            return ResponseEntity.ok(dtoList);

        } catch (Exception e) {
            System.err.println("=== 전체 오류: " + e.getMessage() + " ===");
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 카테고리 목록 조회 API
     * GET /ingredients/categories
     *
     * @return 중복 없는 카테고리 목록
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = ingredientService.getCategoryList();

        return ResponseEntity.ok(categories);
    }

    /* === 어드민 페이지 === */

    /**
     * 관리자용: 식재로 등록 API
     * POST /api/ingredients
     *
     * @param dto 등록할 식재료 정보
     * @return 등록된 식재료 정보 (DTO)
     */
    @PostMapping
    public ResponseEntity<IngredientResponseDto> createIngredient(@RequestBody IngredientCreateRequestDto dto) {
        try {
            IngredientResponseDto response = ingredientService.createIngredient(dto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // 잘못된 입력값
            log.warn("잘못된 입력값: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (DataIntegrityViolationException e) {
            // DB 제약조건 위반
            log.warn("DB 제약조건 위반: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            // 기타 런타임 에러
            log.error("런타임 에러: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 관리자용: 특정 식재료 조회 API
     * GET /api/ingredients/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<IngredientResponseDto> getIngredientById(@PathVariable Long id) {
        try {
            IngredientResponseDto response = ingredientService.getIngredientById(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // 잘못된 입력값
            log.warn("잘못된 입력값: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (DataIntegrityViolationException e) {
            // DB 제약조건 위반
            log.warn("DB 제약조건 위반: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            // 기타 런타임 에러
            log.error("런타임 에러: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 관리자용: 식재료 수정 API
     * PUT /api/ingredients/{id}
     *
     * @param id 수정할 식재료 ID
     * @param dto 수정할 식재료 정보
     * @return 수정된 식재료 정보 (DTO)
     */

    @PutMapping("/{id}")
    public ResponseEntity<IngredientResponseDto> updateIngredient(@PathVariable Long id, @RequestBody IngredientUpdateRequestDto dto){
        try{
            IngredientResponseDto response = ingredientService.updateIngredient(id,dto);
            return ResponseEntity.ok(response);
        }catch (IllegalArgumentException e){
            //잘못된 입력값
            log.warn("잘못된 입력값: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }catch (DataIntegrityViolationException e) {
            // DB 제약조건 위반
            log.warn("DB 제약조건 위반: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            // 기타 런타임 에러
            log.error("런타임 에러: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * 관리자용: 식재료 삭제 API
     * DELETE /api/ingredients/{id}
     *
     * @param id 삭제할 식재료 ID
     * @return 삭제 성공 시 204 No Content
     */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long id){
        try{
            ingredientService.deleteIngredient(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            // 잘못된 입력값
            log.warn("잘못된 입력값: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (DataIntegrityViolationException e) {
            // DB 제약조건 위반
            log.warn("DB 제약조건 위반: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            // 기타 런타임 에러
            log.error("런타임 에러: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

}