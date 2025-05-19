package com.ohgiraffers.refrigegobackend.ingredient.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserIngredientUpdateRequestDto {
    private Long id;               // 수정할 보유 재료 ID
    private LocalDate purchaseDate;
    private LocalDate expiryDate;
    private boolean isFrozen;
    private String customName;     // 직접 추가한 이름 수정 가능
    private String imageUrl;       // 이미지 URL 추가 (이미지도 수정하려면)
}
