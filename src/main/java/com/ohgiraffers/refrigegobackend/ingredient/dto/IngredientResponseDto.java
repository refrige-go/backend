package com.ohgiraffers.refrigegobackend.ingredient.dto;

import com.ohgiraffers.refrigegobackend.ingredient.domain.Ingredient;
import lombok.Getter;

/**
 * 기준 재료 조회 응답 DTO
 * - 클라이언트에게 재료 정보를 응답할 때 사용
 */
@Getter
public class IngredientResponseDto {

    private final Long id;         // 재료 ID
    private final String name;     // 재료명
    private final String category; // 카테고리 (표시용 문자열)

    /**
     * Entity → DTO 변환 생성자
     * @param ingredient 기준 재료 엔티티
     */
    public IngredientResponseDto(Ingredient ingredient) {
        this.id = ingredient.getId();
        this.name = ingredient.getName();
        this.category = ingredient.getCategory().getDisplayName();
    }
}
