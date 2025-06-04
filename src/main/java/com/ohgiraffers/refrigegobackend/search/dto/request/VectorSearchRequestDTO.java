package com.ohgiraffers.refrigegobackend.search.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VectorSearchRequestDTO {
    
    private String query;
    private int limit = 10;
    
    public VectorSearchRequestDTO(String query) {
        this.query = query;
    }
}
