package com.ohgiraffers.refrigegobackend.ai.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * AI 서버에서 반환하는 전체 응답을 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendationResponse {
    
    private List<AiRecommendedRecipe> recipes;
    
    private Integer total;
    
    @JsonProperty("processing_time")
    private Double processingTime;
    
    @Override
    public String toString() {
        return "AiRecommendationResponse{" +
                "recipes=" + recipes +
                ", total=" + total +
                ", processingTime=" + processingTime +
                '}';
    }
}
