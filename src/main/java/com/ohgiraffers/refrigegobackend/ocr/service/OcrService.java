package com.ohgiraffers.refrigegobackend.ocr.service;

import com.ohgiraffers.refrigegobackend.ingredient.domain.Ingredient;
import com.ohgiraffers.refrigegobackend.ingredient.domain.UserIngredient;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.IngredientRepository;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.UserIngredientRepository;
import com.ohgiraffers.refrigegobackend.user.entity.User;
import com.ohgiraffers.refrigegobackend.user.repository.UserRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@Service
public class OcrService {

    private final UserIngredientRepository userIngredientRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;

    public OcrService(UserIngredientRepository userIngredientRepository,UserRepository userRepository, IngredientRepository ingredientRepository) {
        this.userIngredientRepository = userIngredientRepository;
        this.userRepository = userRepository;
        this.ingredientRepository = ingredientRepository;
    }

    public String sendImageToAiServer(MultipartFile image) throws IOException {

        String aiUrl = "http://localhost:8000/api/v1/ocr/process";

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
        ResponseEntity<String> aiResponse = restTemplate.postForEntity(aiUrl, requestEntity, String.class);

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

            System.out.println("ingredient_id: " + ingredientId);

            Ingredient ingredientEntity = null;
            if(ingredientId != null){
                ingredientEntity = ingredientRepository.findById(ingredientId)
                        .orElse(null);
            }

            System.out.println("ingredientEntity: " + ingredientEntity);

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

        }
    }
}