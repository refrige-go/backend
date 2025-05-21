package com.ohgiraffers.refrigegobackend.ingredient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 유저가 보유 재료를 등록할 때 사용하는 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class UserIngredientRequestDto {

    private String userId;         // 유저 ID
    private Long ingredientId;    // 기준 재료 ID
    private String customName;    // 직접 입력한 재료명
    private LocalDate purchaseDate; // 구매일자
    private LocalDate expiryDate;   // 소비기한


    @JsonProperty("isFrozen")
    private boolean frozen;


}
