package com.ohgiraffers.refrigegobackend.search.controller;

import com.ohgiraffers.refrigegobackend.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 검색 기능 테스트용 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class SearchTestController {
    
    private final SearchService searchService;
    
    /**
     * AI 서버 연결 테스트
     */
    @GetMapping("/ai-server")
    public ResponseEntity<Map<String, Object>> testAiServerConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isHealthy = searchService.checkAiServerHealth();
            
            response.put("ai_server_connected", isHealthy);
            response.put("message", isHealthy ? "AI 서버 연결 성공" : "AI 서버 연결 실패");
            response.put("status", isHealthy ? "success" : "failed");
            
            log.info("AI 서버 연결 테스트 결과: {}", isHealthy);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("AI 서버 연결 테스트 중 오류: {}", e.getMessage(), e);
            response.put("ai_server_connected", false);
            response.put("message", "AI 서버 연결 테스트 실패: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 간단한 검색 테스트
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> testSearch(
            @RequestParam(defaultValue = "김치찌개") String query
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("검색 테스트 시작: query={}", query);
            
            // 시멘틱 검색 테스트
            var semanticResult = searchService.performSemanticSearch(query, "all", 5);
            
            response.put("query", query);
            response.put("semantic_search_results", Map.of(
                    "recipes_count", semanticResult.getRecipes().size(),
                    "ingredients_count", semanticResult.getIngredients().size(),
                    "total_matches", semanticResult.getTotalMatches(),
                    "processing_time", semanticResult.getProcessingTime()
            ));
            
            // 벡터 검색 테스트
            var vectorResult = searchService.performVectorSearch(query, 5);
            
            response.put("vector_search_results", Map.of(
                    "results_count", vectorResult.getResults().size(),
                    "total", vectorResult.getTotal(),
                    "search_method", vectorResult.getSearchMethod()
            ));
            
            response.put("status", "success");
            response.put("message", "검색 테스트 완료");
            
            log.info("검색 테스트 완료: 시멘틱 {}개, 벡터 {}개 결과", 
                    semanticResult.getTotalMatches(), vectorResult.getTotal());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("검색 테스트 중 오류: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "검색 테스트 실패: " + e.getMessage());
            response.put("query", query);
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
