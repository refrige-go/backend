package com.ohgiraffers.refrigegobackend.ocr.service;

import com.ohgiraffers.refrigegobackend.ingredient.domain.Ingredient;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.IngredientRepository;
import com.ohgiraffers.refrigegobackend.ocr.dto.MatchedProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ProductMatchingService {
    private final IngredientRepository ingredientRepository;

    @Autowired
    public ProductMatchingService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    public List<MatchedProduct> matchProducts(List<String> productNames) {
        List<Ingredient> allIngredients = ingredientRepository.findAll();
        System.out.println("DB에서 불러온 상품명 개수: " + allIngredients.size());

        List<MatchedProduct> matchedProducts = new ArrayList<>();
        boolean anyMatched = false; // 매칭 성공 여부 플래그

        for (String productName : productNames) {
            String cleanedName = cleanProductName(productName);
            System.out.println("OCR 추출 품목명: " + productName + ", 정제 후: " + cleanedName);

            if (cleanedName == null || cleanedName.isEmpty()) {
                continue;
            }

            boolean matchedThisProduct = false;

            for (Ingredient ingredient : allIngredients) {
                String dbName = ingredient.getName();
               // System.out.println("비교 대상: " + dbName); // DB와 비교 로그
                // DB 상품명이 정제된 OCR 품목명에 포함되어 있으면 매칭
                if (cleanedName.contains(dbName)) {
                    MatchedProduct matched = new MatchedProduct();
                    matched.setOriginalName(productName);
                    matched.setMatchedName(dbName);
                    matched.setSimilarity(100); // 포함 매칭이므로 100%로 설정
                    // 필요하다면 카테고리 등도 추가
                    matchedProducts.add(matched);
                    anyMatched = true;
                    matchedThisProduct = true;
                    System.out.println("매칭 성공: " + productName + " → " + dbName);
                    break;
                }
            }
            if (!matchedThisProduct) {
                System.out.println("매칭 실패: " + cleanedName + " (DB에 해당 상품 없음)");
        }
    }
        if (!anyMatched) {
            System.out.println("매칭된 상품이 없습니다.");
        }
        return matchedProducts;
    }

    private String cleanProductName(String name) {
        // 단위/수량 패턴 제거
        String cleaned = name.replaceAll("\\d+\\s*(g|ml|L|kg|개|봉|팩|통|캔)", "")
                .replaceAll("[^가-힣a-zA-Z0-9]", "")
                .trim();
        return cleaned;
    }

    private int calculateSimilarity(String s1, String s2) {
        // Levenshtein 거리 기반 유사도 계산
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) return 100;
        return (int) ((1.0 - (double) levenshteinDistance(s1, s2) / maxLength) * 100);
    }

    private int levenshteinDistance(String s1, String s2) {
        // Levenshtein 거리 계산 알고리즘
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i-1) == s2.charAt(j-1)) {
                    dp[i][j] = dp[i-1][j-1];
                } else {
                    dp[i][j] = Math.min(dp[i-1][j-1], Math.min(dp[i-1][j], dp[i][j-1])) + 1;
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }
}