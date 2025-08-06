package com.ohgiraffers.refrigegobackend.ingredient.dto;

import com.ohgiraffers.refrigegobackend.ingredient.domain.IngredientCategory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/* 도메인 페이지 - 식재료 수정용 DTO */

@Slf4j
@Getter
@Setter
public class IngredientUpdateRequestDto {
    private String name;
    private String category;
    private Integer defaultExpiryDays;
    private String storageMethod;
    private String imageUrl;

    // Enum으로 변환하는 메서드
    public IngredientCategory getCategoryEnum() {
        if (category == null || category.isEmpty()) {
            throw new IllegalArgumentException("카테고리는 필수입니다.");
        }
        log.debug("받은 카테고리: {}", category);
        IngredientCategory result = IngredientCategory.fromDisplayName(category);
        log.debug("변환된 카테고리: {}", result);

        return result;
//        return IngredientCategory.fromDisplayName(category);
    }
}
