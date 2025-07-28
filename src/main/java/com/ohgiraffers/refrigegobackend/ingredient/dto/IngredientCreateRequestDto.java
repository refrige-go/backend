package com.ohgiraffers.refrigegobackend.ingredient.dto;

/*도메인 페이지 - 식재료 추가용 DTO*/

import com.ohgiraffers.refrigegobackend.ingredient.domain.IngredientCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IngredientCreateRequestDto {
    private String name;            // 식재료명
    private String category;        // 카테고리
    private Integer defaultExpiryDays; // 기본 유통기한(일)
    private String storageMethod;   // 보관 방법
    private String imageUrl; //이미지 URL

    // Enum으로 변환하는 메서드
    public IngredientCategory getCategoryEnum() {
        if (category == null || category.isEmpty()) {
            throw new IllegalArgumentException("카테고리는 필수입니다.");
        }
        return IngredientCategory.fromDisplayName(category);
    }
}
