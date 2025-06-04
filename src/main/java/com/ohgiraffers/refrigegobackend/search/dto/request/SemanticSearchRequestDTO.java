package com.ohgiraffers.refrigegobackend.search.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SemanticSearchRequestDTO {
    
    private String query;
    private String searchType = "all"; // all, recipe, ingredient
    private int limit = 10;
    
    public SemanticSearchRequestDTO(String query) {
        this.query = query;
    }
    
    public SemanticSearchRequestDTO(String query, int limit) {
        this.query = query;
        this.limit = limit;
    }
}
