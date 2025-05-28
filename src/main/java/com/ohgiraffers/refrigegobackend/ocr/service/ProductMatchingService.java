package com.ohgiraffers.refrigegobackend.ocr.service;

import com.ohgiraffers.refrigegobackend.ingredient.domain.Ingredient;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.IngredientRepository;
import com.ohgiraffers.refrigegobackend.ocr.dto.MatchedProduct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
        // 긴 이름 우선 정렬
        allIngredients.sort(Comparator.comparingInt((Ingredient i) -> i.getName().length()).reversed());

        List<MatchedProduct> matchedProducts = new ArrayList<>();
        boolean anyMatched = false;

        for (String productName : productNames) {
            String cleanedName = cleanProductName(productName);
            log.info("OCR 추출 품목명: {}, 정제 후: {}", productName, cleanedName);

            if (cleanedName == null || cleanedName.isEmpty()) {
                continue;
            }

            boolean matchedThisProduct = false;

            // 1. 완전일치
            Optional<Ingredient> exactMatch = allIngredients.stream()
                    .filter(ingredient -> cleanedName.equals(ingredient.getName()))
                    .findFirst();

            if (exactMatch.isPresent()) {
                matchedProducts.add(makeMatchedProduct(productName, exactMatch.get(), 100));
                log.info("완전일치 매칭 성공: {} → {}", productName, exactMatch.get().getName());
                anyMatched = true;
                continue;
            }

            // 2. 부분포함 (긴 이름 우선)
            Optional<Ingredient> partialMatch = allIngredients.stream()
                    .filter(ingredient -> cleanedName.contains(ingredient.getName()))
                    .findFirst();

            if (partialMatch.isPresent()) {
                matchedProducts.add(makeMatchedProduct(productName, partialMatch.get(), 100));
                log.info("부분포함 매칭 성공: {} → {}", productName, partialMatch.get().getName());
                anyMatched = true;
                continue;
            }

            // 3. 유사도 (Levenshtein) 75% 이상
            int maxSimilarity = 0;
            Ingredient bestMatch = null;
            for (Ingredient ingredient : allIngredients) {
                int similarity = calculateSimilarity(cleanedName, ingredient.getName());
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    bestMatch = ingredient;
                }
            }
            if (bestMatch != null && maxSimilarity >= 75) {
                matchedProducts.add(makeMatchedProduct(productName, bestMatch, maxSimilarity));
                log.info("유사도 매칭 성공: {} → {} ({}%)", productName, bestMatch.getName(), maxSimilarity);
                anyMatched = true;
                continue;
            }

            log.info("매칭 실패: {} (DB에 해당 상품 없음)", cleanedName);
        }

        if (!anyMatched) {
            log.info("매칭된 상품이 없습니다.");
        }

        return matchedProducts;
    }

    private MatchedProduct makeMatchedProduct(String originalName, Ingredient ingredient, int similarity) {
        MatchedProduct matched = new MatchedProduct();
        matched.setOriginalName(originalName);
        matched.setMatchedName(ingredient.getName());
        matched.setSimilarity(similarity);
        matched.setMainCategory(ingredient.getCategory());
        return matched;
    }

    private String cleanProductName(String name) {
        if (name == null) return "";
        // 공백, 단위, 특수문자 제거 등 전처리 강화
        return name.replaceAll("\\s+", "")
                .replaceAll("\\d+\\s*(g|ml|L|kg|개|봉|팩|통|캔)", "")
                .replaceAll("[^가-힣a-zA-Z0-9]", "")
                .trim();
    }

    // Levenshtein 거리 기반 유사도 (Apache Commons Text)
    private int calculateSimilarity(String s1, String s2) {
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) return 100;
        int distance = new LevenshteinDistance().apply(s1, s2);
        return (int) ((1.0 - (double) distance / maxLength) * 100);
    }
}