package com.ohgiraffers.refrigegobackend.ocr.service;

import com.ohgiraffers.refrigegobackend.ingredient.domain.Ingredient;
import com.ohgiraffers.refrigegobackend.ingredient.domain.UserIngredient;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.IngredientRepository;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.UserIngredientRepository;
import com.ohgiraffers.refrigegobackend.user.entity.User;
import com.ohgiraffers.refrigegobackend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@Service
public class OcrService {

    @Value("${ai-server.base-url}")
    private String aiServerBaseUrl;

    private static final Logger log = LoggerFactory.getLogger(OcrService.class);
    private final UserIngredientRepository userIngredientRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;

    public OcrService(UserIngredientRepository userIngredientRepository,UserRepository userRepository, IngredientRepository ingredientRepository) {
        this.userIngredientRepository = userIngredientRepository;
        this.userRepository = userRepository;
        this.ingredientRepository = ingredientRepository;
    }

    public String sendImageToAiServer(MultipartFile image) throws IOException {

        String aiUrl = aiServerBaseUrl + "/api/v1/ocr/process";

        log.info("OCR 요청 시작 - AI 서버 URL: {}", aiUrl);

        ByteArrayResource imageResource = new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", imageResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();

        // 타임아웃 설정
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);  // 15초 연결 타임아웃
        factory.setReadTimeout(50000);     // 50초 읽기 타임아웃
        restTemplate.setRequestFactory(factory);

        log.info("AI 서버로 요청 전송 중...");
        ResponseEntity<String> aiResponse = restTemplate.postForEntity(aiUrl, requestEntity, String.class);
        log.info("AI 서버 응답 수신 완료");

        return aiResponse.getBody(); // AI 서버의 응답(JSON 등)
    }

    // 새로운 재료 저장 메서드
    public void saveIngredients(List<Map<String, Object>> ingredients) {
        // 현재 인증된 사용자의 ID 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // username 꺼내기

        User user = userRepository.findByUsernameAndDeletedFalse(username);
        if (user == null) {
            throw new RuntimeException("유저를 찾을 수 없습니다: " + username);
        }
        Long userId = user.getId(); // user_id(PK) 값

        for (Map<String, Object> ingredient : ingredients) {
            // 날짜 처리 로직 추가
            LocalDate purchaseDate = null;
            LocalDate expiryDate = null;

            String purchaseDateStr = (String) ingredient.get("purchaseDate");
            String expiryDateStr = (String) ingredient.get("expirationDate");

            // 구매일자가 비어있지 않은 경우에만 파싱
            if (purchaseDateStr != null && !purchaseDateStr.trim().isEmpty()) {
                purchaseDate = LocalDate.parse(purchaseDateStr);
            } else {
                // 기본값으로 오늘 날짜 설정
                purchaseDate = LocalDate.now();
            }

            // 유통기한이 비어있지 않은 경우에만 파싱
            if (expiryDateStr != null && !expiryDateStr.trim().isEmpty()) {
                expiryDate = LocalDate.parse(expiryDateStr);
            } else {
                // 기본값으로 구매일 + 7일 설정
                expiryDate = purchaseDate.plusDays(7);
            }
            /*ingredient_id DB에 저장*/
            Long ingredientId = ingredient.get("ingredient_id") != null ? Long.valueOf(ingredient.get("ingredient_id").toString()) : null;

            log.debug("재료 정보 처리 중 - ingredient_id: {}, 재료명: {}", ingredientId, ingredient.get("name"));

            Ingredient ingredientEntity = null;
            if(ingredientId != null){
                ingredientEntity = ingredientRepository.findById(ingredientId)
                        .orElse(null);
            }

            log.debug("기준 재료 조회 결과 - ingredient_id: {}, 조회된 엔티티: {}",
                    ingredientId, ingredientEntity != null ? ingredientEntity.getName() : "null");

            UserIngredient userIngredient = UserIngredient.builder()
                    .userId(userId)
                    .ingredient(ingredientEntity)
                    .customName((String) ingredient.get("name"))  // OCR로 인식된 이름을 customName으로 저장
                    .purchaseDate(purchaseDate)
                    .expiryDate(expiryDate)
                    .isFrozen("냉동".equals(ingredient.get("storageMethod")))
                    .build();

            // DB에 저장
            userIngredientRepository.save(userIngredient);

            log.info("재료 저장 완료 - 사용자: {}, 재료명: {}, 구매일: {}, 유통기한: {}, 냉동여부: {}",
                    username,
                    ingredient.get("name"),
                    purchaseDate,
                    expiryDate,
                    "냉동".equals(ingredient.get("storageMethod")));
        }
        log.info("=== OCR 재료 저장 완료 - 사용자: {}, 총 저장된 재료 개수: {} ===", username, ingredients.size());
    }
}