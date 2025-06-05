package com.ohgiraffers.refrigegobackend.search.controller;

import com.ohgiraffers.refrigegobackend.search.dto.response.SemanticSearchResponseDTO;
import com.ohgiraffers.refrigegobackend.search.dto.response.VectorSearchResponseDTO;
import com.ohgiraffers.refrigegobackend.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    
    private final SearchService searchService;
    
    /**
     * 시멘틱 검색 API
     * 검색어의 의미를 이해하여 레시피와 재료를 검색
     */
    @GetMapping("/semantic")
    public ResponseEntity<SemanticSearchResponseDTO> semanticSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "all") String searchType,
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("시멘틱 검색 API 호출: query={}, searchType={}, limit={}", query, searchType, limit);
        
        try {
            SemanticSearchResponseDTO response = searchService.performSemanticSearch(query, searchType, limit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("시멘틱 검색 API 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 벡터 검색 API  
     * OpenAI 임베딩을 사용한 의미 기반 레시피 검색
     */
    @GetMapping("/vector")
    public ResponseEntity<VectorSearchResponseDTO> vectorSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("벡터 검색 API 호출: query={}, limit={}", query, limit);
        
        try {
            VectorSearchResponseDTO response = searchService.performVectorSearch(query, limit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("벡터 검색 API 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 추천 검색 API (기본 검색)
     * 사용자 입력에 따라 최적의 검색 방법을 자동 선택
     */
    @GetMapping
    public ResponseEntity<SemanticSearchResponseDTO> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("추천 검색 API 호출: query={}, limit={}", query, limit);
        
        try {
            SemanticSearchResponseDTO response = searchService.performRecommendedSearch(query, limit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("추천 검색 API 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 레시피 전용 검색 API
     */
    @GetMapping("/recipes")
    public ResponseEntity<SemanticSearchResponseDTO> searchRecipes(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("레시피 검색 API 호출: query={}, limit={}", query, limit);
        
        try {
            SemanticSearchResponseDTO response = searchService.performSemanticSearch(query, "recipe", limit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("레시피 검색 API 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 재료 전용 검색 API
     */
    @GetMapping("/ingredients")
    public ResponseEntity<SemanticSearchResponseDTO> searchIngredients(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("재료 검색 API 호출: query={}, limit={}", query, limit);
        
        try {
            SemanticSearchResponseDTO response = searchService.performSemanticSearch(query, "ingredient", limit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("재료 검색 API 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * AI 서버 연결 상태 확인 API
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean aiServerHealthy = searchService.checkAiServerHealth();
            
            response.put("status", aiServerHealthy ? "healthy" : "unhealthy");
            response.put("ai_server_connected", aiServerHealthy);
            response.put("search_features", Map.of(
                    "semantic_search", aiServerHealthy,
                    "vector_search", aiServerHealthy,
                    "recipe_search", aiServerHealthy,
                    "ingredient_search", aiServerHealthy
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("헬스체크 오류: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("ai_server_connected", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
