package com.ohgiraffers.refrigegobackend.search.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VectorSearchResponseDTO {
    
    private String query;
    private List<RecipeSearchResultDTO> results;
    private int total;
    private String searchMethod;
    
    public VectorSearchResponseDTO(String query, List<RecipeSearchResultDTO> results, int total) {
        this.query = query;
        this.results = results;
        this.total = total;
        this.searchMethod = "vector";
    }
}
