package com.ohgiraffers.refrigegobackend.ai.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * AI 서버와의 통신을 담당하는 클라이언트 (레시피 추천용)
 */
@Component
@Slf4j
public class AiRecommendationClient {

    private final RestTemplate restTemplate;
    private final String aiServerBaseUrl;
    private final String healthEndpoint;
    private final String recipesEndpoint;

    public AiRecommendationClient(RestTemplate restTemplate, 
                         @Value("${ai.server.base-url:http://localhost:8000}") String aiServerBaseUrl,
                         @Value("${ai.server.endpoints.backend-health:/health}") String healthEndpoint,
                         @Value("${ai.server.endpoints.backend-recipes:/api/recommend/by-ingredients}") String recipesEndpoint) {
        this.restTemplate = restTemplate;
        this.aiServerBaseUrl = aiServerBaseUrl;
        this.healthEndpoint = healthEndpoint;
        this.recipesEndpoint = recipesEndpoint;
    }

    /**
     * AI 서버에 레시피 추천 요청
     * 
     * @param userId 사용자 ID
     * @param selectedIngredients 선택된 재료 목록
     * @param limit 추천 개수 제한
     * @return AI 서버 응답
     */
    public AiRecommendationResponse requestRecipeRecommendation(
            String userId, 
            List<String> selectedIngredients, 
            Integer limit) {
        
        log.info("AI 서버 레시피 추천 요청 - 사용자: {}, 재료: {}, 제한: {}", 
                userId, selectedIngredients, limit);

        try {
            // 요청 데이터 구성 (AI 서버가 기대하는 형식으로 수정)
            Map<String, Object> requestData = Map.of(
                "ingredients", selectedIngredients != null ? selectedIngredients : Collections.emptyList(),
                "limit", limit != null ? limit : 10
            );

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestData, headers);

            // AI 서버 호출
            String url = aiServerBaseUrl + recipesEndpoint;
            log.info("AI 서버 요청 URL: {}", url);
            log.info("AI 서버 요청 데이터: {}", requestData);

            // 원시 응답을 문자열로 받아서 로그 출력
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
            );
            
            log.info("AI 서버 원시 JSON 응답: {}", rawResponse.getBody());
            
            // 이제 정상적으로 파싱
            ResponseEntity<AiRecommendationResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                AiRecommendationResponse.class
            );

            AiRecommendationResponse responseBody = response.getBody();
            log.info("AI 서버 원시 응답: {}", responseBody);
            log.info("AI 서버 응답 수신 - 상태: {}, 추천 개수: {}", 
                    response.getStatusCode(), 
                    responseBody != null && responseBody.getTotal() != null ? responseBody.getTotal() : "null");

            return responseBody;

        } catch (HttpClientErrorException e) {
            log.error("AI 서버 클라이언트 오류 (4xx): 상태={}, 응답={}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI 서버 요청 오류: " + e.getMessage(), e);
            
        } catch (HttpServerErrorException e) {
            log.error("AI 서버 서버 오류 (5xx): 상태={}, 응답={}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI 서버 내부 오류: " + e.getMessage(), e);
            
        } catch (ResourceAccessException e) {
            log.error("AI 서버 연결 오류: {}", e.getMessage());
            throw new RuntimeException("AI 서버에 연결할 수 없습니다: " + e.getMessage(), e);
            
        } catch (Exception e) {
            log.error("AI 서버 통신 중 예상치 못한 오류: ", e);
            throw new RuntimeException("AI 서버 통신 오류: " + e.getMessage(), e);
        }
    }

    /**
     * AI 서버 헬스체크
     * 
     * @return 연결 상태
     */
    public boolean isAiServerHealthy() {
        try {
            String url = aiServerBaseUrl + healthEndpoint;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            Map<String, Object> healthData = response.getBody();
            String status = healthData != null ? (String) healthData.get("status") : "unknown";
            
            boolean isHealthy = "healthy".equals(status);
            log.info("AI 서버 헬스체크 - URL: {}, 상태: {}, 건강함: {}", url, status, isHealthy);
            
            return isHealthy;
            
        } catch (Exception e) {
            log.warn("AI 서버 헬스체크 실패: {}", e.getMessage());
            return false;
        }
    }
}
