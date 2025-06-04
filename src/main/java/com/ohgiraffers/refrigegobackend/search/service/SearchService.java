package com.ohgiraffers.refrigegobackend.search.service;

import com.ohgiraffers.refrigegobackend.search.client.AiServerClient;
import com.ohgiraffers.refrigegobackend.search.dto.request.SemanticSearchRequestDTO;
import com.ohgiraffers.refrigegobackend.search.dto.request.VectorSearchRequestDTO;
import com.ohgiraffers.refrigegobackend.search.dto.response.SemanticSearchResponseDTO;
import com.ohgiraffers.refrigegobackend.search.dto.response.VectorSearchResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    
    private final AiServerClient aiServerClient;
    
    /**
     * 시멘틱 검색 수행
     */
    public SemanticSearchResponseDTO performSemanticSearch(String query, String searchType, int limit) {
        log.info("시멘틱 검색 요청: query={}, searchType={}, limit={}", query, searchType, limit);
        
        // 검색어 유효성 검사
        if (query == null || query.trim().isEmpty()) {
            log.warn("빈 검색어로 인한 검색 실패");
            return createEmptySemanticResponse();
        }
        
        // 검색 타입 유효성 검사
        if (!isValidSearchType(searchType)) {
            log.warn("유효하지 않은 검색 타입: {}", searchType);
            searchType = "all";
        }
        
        // AI 서버 연결 상태 확인
        if (!aiServerClient.isAiServerHealthy()) {
            log.error("AI 서버가 연결되지 않음");
            return createEmptySemanticResponse();
        }
        
        SemanticSearchRequestDTO request = new SemanticSearchRequestDTO(
                query.trim(), searchType, Math.max(1, Math.min(limit, 50))
        );
        
        try {
            SemanticSearchResponseDTO response = aiServerClient.semanticSearch(request);
            log.info("시멘틱 검색 완료: {} 개 결과", response.getTotalMatches());
            return response;
        } catch (Exception e) {
            log.error("시멘틱 검색 중 오류 발생: {}", e.getMessage(), e);
            return createEmptySemanticResponse();
        }
    }
    
    /**
     * 벡터 검색 수행
     */
    public VectorSearchResponseDTO performVectorSearch(String query, int limit) {
        log.info("벡터 검색 요청: query={}, limit={}", query, limit);
        
        // 검색어 유효성 검사
        if (query == null || query.trim().isEmpty()) {
            log.warn("빈 검색어로 인한 벡터 검색 실패");
            return createEmptyVectorResponse(query);
        }
        
        // AI 서버 연결 상태 확인
        if (!aiServerClient.isAiServerHealthy()) {
            log.error("AI 서버가 연결되지 않음");
            return createEmptyVectorResponse(query);
        }
        
        VectorSearchRequestDTO request = new VectorSearchRequestDTO(
                query.trim(), Math.max(1, Math.min(limit, 50))
        );
        
        try {
            VectorSearchResponseDTO response = aiServerClient.vectorSearch(request);
            log.info("벡터 검색 완료: {} 개 결과", response.getTotal());
            return response;
        } catch (Exception e) {
            log.error("벡터 검색 중 오류 발생: {}", e.getMessage(), e);
            return createEmptyVectorResponse(query);
        }
    }
    
    /**
     * 추천 검색 (사용자 입력에 따라 자동으로 검색 방법 선택)
     */
    public SemanticSearchResponseDTO performRecommendedSearch(String query, int limit) {
        log.info("추천 검색 요청: query={}, limit={}", query, limit);
        
        // 기본적으로 시멘틱 검색 사용 (레시피와 재료 모두 검색)
        return performSemanticSearch(query, "all", limit);
    }
    
    /**
     * AI 서버 상태 확인
     */
    public boolean checkAiServerHealth() {
        boolean isHealthy = aiServerClient.isAiServerHealthy();
        log.info("AI 서버 상태: {}", isHealthy ? "정상" : "오류");
        return isHealthy;
    }
    
    /**
     * 검색 타입 유효성 검사
     */
    private boolean isValidSearchType(String searchType) {
        return searchType != null && 
               (searchType.equals("all") || searchType.equals("recipe") || searchType.equals("ingredient"));
    }
    
    /**
     * 빈 시멘틱 검색 응답 생성
     */
    private SemanticSearchResponseDTO createEmptySemanticResponse() {
        return new SemanticSearchResponseDTO(
                java.util.Collections.emptyList(),
                java.util.Collections.emptyList(),
                0,
                0.0
        );
    }
    
    /**
     * 빈 벡터 검색 응답 생성
     */
    private VectorSearchResponseDTO createEmptyVectorResponse(String query) {
        return new VectorSearchResponseDTO(
                query,
                java.util.Collections.emptyList(),
                0
        );
    }
}
