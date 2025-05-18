package com.ohgiraffers.refrigegobackend.ingredients.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 유저가 보유 재료를 등록할 때 사용하는 요청 DTO
 */
@Getter
@NoArgsConstructor
public class UserIngredientRequestDto {

    private String userId;         // 유저 ID
    private Long ingredientId;    // 기준 재료 ID
    private String customName;    // 직접 입력한 재료명
    private LocalDate purchaseDate; // 구매일자
    private LocalDate expiryDate;   // 소비기한
    private boolean isFrozen;       // 냉동 보관 여부
}
