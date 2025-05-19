package com.ohgiraffers.refrigegobackend.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 레시피 찜하기 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeFavoriteRequestDto {

    /**
     * 사용자 ID
     */
    private String userId;

    /**
     * 레시피 고유번호
     */
    private String recipeId;
}
