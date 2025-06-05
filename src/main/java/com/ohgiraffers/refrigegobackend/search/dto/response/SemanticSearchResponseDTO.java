package com.ohgiraffers.refrigegobackend.search.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SemanticSearchResponseDTO {
    
    private List<RecipeSearchResultDTO> recipes;
    private List<IngredientSearchResultDTO> ingredients;
    private int totalMatches;
    private double processingTime;
    private String searchMethod;
    
    public SemanticSearchResponseDTO(List<RecipeSearchResultDTO> recipes, 
                                   List<IngredientSearchResultDTO> ingredients, 
                                   int totalMatches, 
                                   double processingTime) {
        this.recipes = recipes;
        this.ingredients = ingredients;
        this.totalMatches = totalMatches;
        this.processingTime = processingTime;
        this.searchMethod = "semantic";
    }
}
