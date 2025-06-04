package com.ohgiraffers.refrigegobackend.search.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.refrigegobackend.config.AiServerProperties;
import com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository.RecipeRepository;
import com.ohgiraffers.refrigegobackend.search.dto.request.SemanticSearchRequestDTO;
import com.ohgiraffers.refrigegobackend.search.dto.request.VectorSearchRequestDTO;
import com.ohgiraffers.refrigegobackend.search.dto.response.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AiServerClient {
    
    private final RestTemplate restTemplate;
    private final AiServerProperties aiServerProperties;
    private final ObjectMapper objectMapper;
    private final RecipeRepository recipeRepository;
    
    public AiServerClient(AiServerProperties aiServerProperties, ObjectMapper objectMapper, RecipeRepository recipeRepository) {
        this.aiServerProperties = aiServerProperties;
        this.objectMapper = objectMapper;
        this.recipeRepository = recipeRepository;
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * 시멘틱 검색 API 호출
     */
    public SemanticSearchResponseDTO semanticSearch(SemanticSearchRequestDTO request) {
        try {
            log.info("AI 서버 시멘틱 검색 호출: query={}, searchType={}, limit={}", 
                    request.getQuery(), request.getSearchType(), request.getLimit());
            
            String url = aiServerProperties.getBaseUrl() + aiServerProperties.getEndpoints().getSemanticSearch();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<SemanticSearchRequestDTO> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                // JSON 응답을 DTO로 변환
                SemanticSearchResponseDTO result = parseSemanticSearchResponse(response.getBody());
                log.info("시멘틱 검색 성공: {} 개 레시피, {} 개 재료", 
                        result.getRecipes().size(), result.getIngredients().size());
                return result;
            } else {
                log.error("AI 서버 시멘틱 검색 실패: {}", response.getStatusCode());
                return createEmptySemanticResponse();
            }
            
        } catch (ResourceAccessException e) {
            log.error("AI 서버 연결 실패: {}", e.getMessage());
            return createEmptySemanticResponse();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI 서버 HTTP 오류: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return createEmptySemanticResponse();
        } catch (Exception e) {
            log.error("AI 서버 호출 중 예상치 못한 오류: {}", e.getMessage(), e);
            return createEmptySemanticResponse();
        }
    }
    
    /**
     * 벡터 검색 API 호출
     */
    public VectorSearchResponseDTO vectorSearch(VectorSearchRequestDTO request) {
        try {
            log.info("AI 서버 벡터 검색 호출: query={}, limit={}", 
                    request.getQuery(), request.getLimit());
            
            String url = aiServerProperties.getBaseUrl() + aiServerProperties.getEndpoints().getVectorSearch();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // AI 서버가 기대하는 형식으로 요청 생성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", request.getQuery());
            requestBody.put("limit", request.getLimit());
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                VectorSearchResponseDTO result = parseVectorSearchResponse(response.getBody());
                log.info("벡터 검색 성공: {} 개 레시피", result.getResults().size());
                return result;
            } else {
                log.error("AI 서버 벡터 검색 실패: {}", response.getStatusCode());
                return createEmptyVectorResponse(request.getQuery());
            }
            
        } catch (ResourceAccessException e) {
            log.error("AI 서버 연결 실패: {}", e.getMessage());
            return createEmptyVectorResponse(request.getQuery());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI 서버 HTTP 오류: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return createEmptyVectorResponse(request.getQuery());
        } catch (Exception e) {
            log.error("AI 서버 호출 중 예상치 못한 오류: {}", e.getMessage(), e);
            return createEmptyVectorResponse(request.getQuery());
        }
    }
    
    /**
     * 오타 교정 API 호출
     */
    public String spellCheck(String query) {
        try {
            log.info("AI 서버 오타 교정 호출: query={}", query);
            
            String url = aiServerProperties.getBaseUrl() + aiServerProperties.getEndpoints().getSpellCheck();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("query", query);
            
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
                String correctedQuery = (String) responseMap.get("corrected");
                boolean isCorrected = (Boolean) responseMap.getOrDefault("is_corrected", false);
                
                if (isCorrected) {
                    log.info("오타 교정 성공: '{}' → '{}'", query, correctedQuery);
                } else {
                    log.debug("오타 교정 불필요: '{}'", query);
                }
                
                return correctedQuery != null ? correctedQuery : query;
            } else {
                log.error("AI 서버 오타 교정 실패: {}", response.getStatusCode());
                return query;
            }
            
        } catch (Exception e) {
            log.error("오타 교정 중 오류: {}", e.getMessage(), e);
            return query; // 실패시 원본 반환
        }
    }
    
    /**
     * AI 서버 연결 상태 확인
     */
    public boolean isAiServerHealthy() {
        try {
            String url = aiServerProperties.getBaseUrl() + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.warn("AI 서버 헬스체크 실패: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 시멘틱 검색 응답 파싱
     */
    private SemanticSearchResponseDTO parseSemanticSearchResponse(String responseBody) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            
            List<RecipeSearchResultDTO> recipes = parseRecipeResults(
                (List<Map<String, Object>>) responseMap.get("recipes"));
            List<IngredientSearchResultDTO> ingredients = parseIngredientResults(
                (List<Map<String, Object>>) responseMap.get("ingredients"));
            
            int totalMatches = (Integer) responseMap.getOrDefault("total_matches", 0);
            double processingTime = ((Number) responseMap.getOrDefault("processing_time", 0.0)).doubleValue();
            
            return new SemanticSearchResponseDTO(recipes, ingredients, totalMatches, processingTime);
        } catch (Exception e) {
            log.error("시멘틱 검색 응답 파싱 오류: {}", e.getMessage());
            return createEmptySemanticResponse();
        }
    }
    
    /**
     * 벡터 검색 응답 파싱
     */
    private VectorSearchResponseDTO parseVectorSearchResponse(String responseBody) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            
            String query = (String) responseMap.get("query");
            List<RecipeSearchResultDTO> results = parseRecipeResults(
                (List<Map<String, Object>>) responseMap.get("results"));

            int total = (Integer) responseMap.getOrDefault("total", 0);
            
            return new VectorSearchResponseDTO(query, results, total);
        } catch (Exception e) {
            log.error("벡터 검색 응답 파싱 오류: {}", e.getMessage());
            return createEmptyVectorResponse("");
        }
    }
    
    /**
     * 레시피 결과 파싱 (이미지 정보 보강 포함)
     */
    private List<RecipeSearchResultDTO> parseRecipeResults(List<Map<String, Object>> recipeData) {
        if (recipeData == null) return Collections.emptyList();
        
        List<RecipeSearchResultDTO> results = recipeData.stream()
                .map(this::mapToRecipeResult)
                .toList();
        
        // MySQL에서 이미지 정보 보강
        enhanceWithImageData(results);
        
        return results;
    }
    
    /**
     * 재료 결과 파싱
     */
    private List<IngredientSearchResultDTO> parseIngredientResults(List<Map<String, Object>> ingredientData) {
        if (ingredientData == null) return Collections.emptyList();
        
        return ingredientData.stream()
                .map(this::mapToIngredientResult)
                .toList();
    }
    
    /**
     * Map을 RecipeSearchResultDTO로 변환
     */
    private RecipeSearchResultDTO mapToRecipeResult(Map<String, Object> data) {
        RecipeSearchResultDTO result = new RecipeSearchResultDTO();
        
        // AI 서버 응답 필드명에 맞춰 매핑
        result.setRcpSeq((String) data.get("rcp_seq"));
        result.setRcpNm((String) data.get("rcp_nm"));
        result.setRcpCategory((String) data.get("rcp_category"));
        result.setRcpWay2((String) data.get("rcp_way2"));
        result.setImage((String) data.get("image"));
        result.setThumbnail((String) data.get("thumbnail"));
        result.setScore(((Number) data.getOrDefault("score", 0.0)).doubleValue());
        result.setMatchReason((String) data.get("match_reason"));
        
        // 디버깅: 이미지 필드 로그
        log.debug("이미지 필드 매핑: image={}, thumbnail={}, recipe={}", 
                data.get("image"), data.get("thumbnail"), data.get("rcp_nm"));
        
        // 재료 정보 파싱
        List<Map<String, Object>> ingredientsData = (List<Map<String, Object>>) data.get("ingredients");
        if (ingredientsData != null) {
            List<RecipeSearchResultDTO.RecipeIngredientDTO> ingredients = ingredientsData.stream()
                    .map(ingredientData -> new RecipeSearchResultDTO.RecipeIngredientDTO(
                            ((Number) ingredientData.get("ingredient_id")).longValue(),
                            (String) ingredientData.get("name"),
                            (Boolean) ingredientData.getOrDefault("is_main_ingredient", false)
                    ))
                    .toList();
            result.setIngredients(ingredients);
        } else {
            result.setIngredients(Collections.emptyList());
        }
        
        return result;
    }
    
    /**
     * Map을 IngredientSearchResultDTO로 변환
     */
    private IngredientSearchResultDTO mapToIngredientResult(Map<String, Object> data) {
        IngredientSearchResultDTO result = new IngredientSearchResultDTO();
        result.setIngredientId(((Number) data.get("ingredient_id")).longValue());
        result.setName((String) data.get("name"));
        result.setCategory((String) data.get("category"));
        result.setScore(((Number) data.getOrDefault("score", 0.0)).doubleValue());
        result.setMatchReason((String) data.get("match_reason"));
        return result;
    }
    
    /**
     * 빈 시멘틱 검색 응답 생성
     */
    private SemanticSearchResponseDTO createEmptySemanticResponse() {
        return new SemanticSearchResponseDTO(
                Collections.emptyList(),
                Collections.emptyList(),
                0,
                0.0
        );
    }
    
    /**
     * 빈 벡터 검색 응답 생성
     */
    private VectorSearchResponseDTO createEmptyVectorResponse(String query) {
        return new VectorSearchResponseDTO(query, Collections.emptyList(), 0);
    }
    
    /**
     * MySQL에서 이미지 정보를 가져와서 DTO에 보강
     */
    private void enhanceWithImageData(List<RecipeSearchResultDTO> recipes) {
        if (recipes == null || recipes.isEmpty()) {
            return;
        }
        
        try {
            // rcpSeq 목록 추출
            List<String> rcpSeqs = recipes.stream()
                    .map(RecipeSearchResultDTO::getRcpSeq)
                    .filter(seq -> seq != null && !seq.trim().isEmpty())
                    .toList();
            
            if (rcpSeqs.isEmpty()) {
                log.warn("이미지 보강: 유효한 rcpSeq가 없음");
                return;
            }
            
            // MySQL에서 이미지 정보 일괄 조회
            List<Object[]> imageResults = recipeRepository.findImagesByRcpSeqIn(rcpSeqs);
            
            // Map으로 변환 (빠른 조회를 위해)
            Map<String, ImageInfo> imageMap = new HashMap<>();
            for (Object[] row : imageResults) {
                String rcpSeq = (String) row[0];
                String image = (String) row[1];
                String thumbnail = (String) row[2];
                imageMap.put(rcpSeq, new ImageInfo(image, thumbnail));
            }
            
            // DTO에 이미지 정보 설정
            for (RecipeSearchResultDTO recipe : recipes) {
                ImageInfo imageInfo = imageMap.get(recipe.getRcpSeq());
                if (imageInfo != null) {
                    recipe.setImage(imageInfo.image);
                    recipe.setThumbnail(imageInfo.thumbnail);
                    log.debug("이미지 보강 완료: {} - image={}, thumbnail={}", 
                            recipe.getRcpNm(), imageInfo.image != null, imageInfo.thumbnail != null);
                }
            }
            
            log.info("이미지 정보 보강 완료: {}개 레시피 중 {}개에 이미지 업데이트", 
                    recipes.size(), imageResults.size());
            
        } catch (Exception e) {
            log.error("이미지 정보 보강 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 이미지 정보를 담는 내부 클래스
     */
    private static class ImageInfo {
        final String image;
        final String thumbnail;
        
        ImageInfo(String image, String thumbnail) {
            this.image = image;
            this.thumbnail = thumbnail;
        }
    }
}
