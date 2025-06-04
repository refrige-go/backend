package com.ohgiraffers.refrigegobackend.search.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientSearchResultDTO {
    
    private Long ingredientId;
    private String name;
    private String category;
    private double score;
    private String matchReason;
}
