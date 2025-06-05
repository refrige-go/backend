package com.ohgiraffers.refrigegobackend.recommendation.service;

import com.ohgiraffers.refrigegobackend.bookmark.domain.Bookmark;
import com.ohgiraffers.refrigegobackend.bookmark.dto.response.UserIngredientRecipeResponseDTO;
import com.ohgiraffers.refrigegobackend.bookmark.infrastructure.repository.BookmarkRepository;
import com.ohgiraffers.refrigegobackend.ingredient.domain.Ingredient;
import com.ohgiraffers.refrigegobackend.ingredient.domain.UserIngredient;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.IngredientRepository;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.UserIngredientRepository;
import com.ohgiraffers.refrigegobackend.notification.service.NotificationService;
import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository.RecipeRepository;
import com.ohgiraffers.refrigegobackend.recommendation.dto.*;
import java.util.Arrays;
import com.ohgiraffers.refrigegobackend.recommendation.infrastructure.repository.RecipeIngredientRepository;
import com.ohgiraffers.refrigegobackend.user.entity.User;
import com.ohgiraffers.refrigegobackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 레시피 추천 서비스
 * - RecipeIngredient 매핑 테이블을 활용한 정확한 추천
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecipeRecommendationService {

    private final RecipeIngredientRepository recipeIngredientRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final UserIngredientRepository userIngredientRepository;
    private final NotificationService notificationService;

    /**
     * 사용자가 선택한 재료를 기반으로 레시피 추천
     * - 매핑 테이블을 활용한 정확한 매칭
     * - DB 레벨에서 매칭 비율 계산
     * 
     * @param requestDto 추천 요청 정보 (선택한 재료들)
     * @return 추천된 레시피 목록
     */
    public RecipeRecommendationResponseDto recommendRecipes(RecipeRecommendationRequestDto requestDto) {
        log.info("레시피 추천 시작 - 선택한 재료: {}", requestDto.getSelectedIngredients());

        // 1. 재료명 → 재료 ID 변환
        List<Long> ingredientIds = convertIngredientNamesToIds(requestDto.getSelectedIngredients());
        
        if (ingredientIds.isEmpty()) {
            log.warn("매칭되는 표준 재료가 없습니다: {}", requestDto.getSelectedIngredients());
            return new RecipeRecommendationResponseDto(List.of(), 0, requestDto.getSelectedIngredients());
        }

        log.info("변환된 재료 ID: {}", ingredientIds);

        // 2. DB에서 매칭 비율 기반 레시피 조회 (최소 30% 이상 매칭)
        List<Object[]> matchResults = recipeIngredientRepository
                .findRecipesByIngredientsWithMatchRatio(ingredientIds, 30.0);

        // 3. 결과를 DTO로 변환
        List<RecommendedRecipeDto> recommendedRecipes = matchResults.stream()
                .limit(requestDto.getLimit() != null ? requestDto.getLimit() : 10)
                .map(this::convertToRecommendedRecipeDto)
                .collect(Collectors.toList());

        log.info("레시피 추천 완료 - 추천된 레시피 수: {}", recommendedRecipes.size());

        return new RecipeRecommendationResponseDto(
                recommendedRecipes,
                recommendedRecipes.size(),
                requestDto.getSelectedIngredients()
        );
    }

    /**
     * 특정 레시피 상세 정보 조회
     * 
     * @param recipeId 레시피 ID
     * @return 레시피 상세 정보
     */
    public RecommendedRecipeDto getRecipeDetail(String recipeId) {
        log.info("레시피 상세 조회 - 레시피: {}", recipeId);

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("레시피를 찾을 수 없습니다. ID: " + recipeId));

        return RecommendedRecipeDto.builder()
                .recipeId(recipe.getRcpSeq())
                .recipeName(recipe.getRcpNm())
                .ingredients(recipe.getRcpPartsDtls())
                .cookingMethod1(recipe.getManual01())
                .cookingMethod2(recipe.getManual02())
                .imageUrl(recipe.getImage())
                .matchedIngredientCount(0) // 상세 조회에서는 의미없음
                .matchedIngredients(List.of())
                .isFavorite(false) // TODO: 북마크 서비스와 연동
                .matchScore(0.0)
                .build();
    }

    /**
     * 주재료 기반 추천 (더 정확한 추천)
     * 
     * @param ingredientNames 재료명 목록
     * @return 주재료 기반 추천 레시피
     */
    public RecipeRecommendationResponseDto recommendByMainIngredients(List<String> ingredientNames) {
        log.info("주재료 기반 추천 시작 - 재료: {}", ingredientNames);

        List<Long> ingredientIds = convertIngredientNamesToIds(ingredientNames);
        
        if (ingredientIds.isEmpty()) {
            return new RecipeRecommendationResponseDto(List.of(), 0, ingredientNames);
        }

        // 주재료만 매칭하는 레시피 조회
        List<com.ohgiraffers.refrigegobackend.recommendation.domain.RecipeIngredient> mainIngredientRecipes = 
                recipeIngredientRepository.findRecipesByMainIngredients(ingredientIds);

        List<RecommendedRecipeDto> recommendations = mainIngredientRecipes.stream()
                .map(ri -> RecommendedRecipeDto.builder()
                        .recipeId(ri.getRecipe().getRcpSeq())
                        .recipeName(ri.getRecipe().getRcpNm())
                        .ingredients(ri.getRecipe().getRcpPartsDtls())
                        .cookingMethod1(ri.getRecipe().getManual01())
                        .cookingMethod2(ri.getRecipe().getManual02())
                        .imageUrl(ri.getRecipe().getImage())
                        .matchedIngredientCount(1)
                        .matchScore(1.0) // 주재료 매칭이므로 높은 점수
                        .isFavorite(false)
                        .build())
                .distinct()
                .limit(10)
                .collect(Collectors.toList());

        log.info("주재료 기반 추천 완료 - 추천된 레시피 수: {}", recommendations.size());

        return new RecipeRecommendationResponseDto(recommendations, recommendations.size(), ingredientNames);
    }

    /**
     * 재료명을 표준 재료 ID로 변환 (개선된 버전)
     */
    private List<Long> convertIngredientNamesToIds(List<String> ingredientNames) {
        // 한 번의 쿼리로 모든 재료 조회 (성능 개선)
        List<Ingredient> ingredients = ingredientRepository.findByNameIn(ingredientNames);
        
        // 찾지 못한 재료들 로깅
        List<String> foundNames = ingredients.stream()
                .map(Ingredient::getName)
                .collect(Collectors.toList());
        
        List<String> notFoundNames = ingredientNames.stream()
                .filter(name -> !foundNames.contains(name))
                .collect(Collectors.toList());
        
        if (!notFoundNames.isEmpty()) {
            log.warn("표준 재료 테이블에서 찾을 수 없는 재료들: {}", notFoundNames);
        }
        
        return ingredients.stream()
                .map(Ingredient::getId)
                .collect(Collectors.toList());
    }

    /**
     * DB 쿼리 결과를 DTO로 변환
     */
    private RecommendedRecipeDto convertToRecommendedRecipeDto(Object[] result) {
        String recipeId = (String) result[0];
        String recipeName = (String) result[1];
        Long totalIngredients = (Long) result[2];
        Long matchedIngredients = (Long) result[3];
        Double matchPercentage = (Double) result[4];

        // 레시피 상세 정보 조회
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElse(null);

        return RecommendedRecipeDto.builder()
                .recipeId(recipeId)
                .recipeName(recipeName)
                .ingredients(recipe != null ? recipe.getRcpPartsDtls() : "")
                .cookingMethod1(recipe != null ? recipe.getManual01() : "")
                .cookingMethod2(recipe != null ? recipe.getManual02() : "")
                .imageUrl(recipe != null ? recipe.getImage() : "")
                .matchedIngredientCount(matchedIngredients.intValue())
                .matchedIngredients(List.of()) // TODO: 성능 최적화 후 구현
                .matchScore(matchPercentage / 100.0) // 0.0 ~ 1.0 범위로 정규화
                .isFavorite(false) // TODO: 북마크 서비스와 연동
                .build();
    }


    /**
     * 해당 레시피의 주재료를 사용한 다른 레시피 추천
     * @param username 사용자 아이디
     * @param recipeId 레시피 아이디
     * @return
     */
    public List<RecipeRecommendationDto> recommendSimilarByMainIngredients(String username, String recipeId) {

        User user = userRepository.findByUsernameAndDeletedFalse(username);

        // 1. 기준 레시피 주재료 아이디들 조회
        List<Long> mainIngredientIds = recipeIngredientRepository.findMainIngredientIdsByRecipeId(recipeId);

        if (mainIngredientIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 기준 레시피와 다른, 동일 주재료를 사용하는 레시피들 조회
        List<Recipe> similarRecipes = recipeIngredientRepository.findRecipesByMainIngredientIds(mainIngredientIds, recipeId);

        // 3. 북마크 여부 체크 및 DTO 변환
        return similarRecipes.stream()
                .map(recipe -> {
                    boolean bookmarked = bookmarkRepository.existsByUserIdAndRecipeRcpSeq(user.getId(), recipe.getRcpSeq());
                    return new SimilarIngredientRecipeDTO(recipe, bookmarked).toResponseDto();
                })
                .limit(10)
                .collect(Collectors.toList());
    }


    /**
     * 스마트 레시피 추천 (유통기한 고려)
     */
    public SmartRecommendationResponseDto recommendRecipesSmart(SmartRecommendationRequestDto requestDto) {
        log.info("스마트 레시피 추천 시작 - 사용자: {}, 선택한 재료: {}", 
                requestDto.getUserId(), requestDto.getSelectedIngredients());

        // 1. 기본 추천 받기
        RecipeRecommendationRequestDto basicRequest = new RecipeRecommendationRequestDto(
                requestDto.getSelectedIngredients(),
                50 // 더 많은 후보 확보
        );
        basicRequest.setUserId(requestDto.getUserId());
        
        RecipeRecommendationResponseDto basicResponse = recommendRecipes(basicRequest);
        
        // 2. 사용자 냉장고 재료 정보 조회
        List<SmartRecommendationRequestDto.UserIngredientInfo> userIngredients = 
                getUserIngredientInfos(requestDto.getUserId(), requestDto.getSelectedIngredients());
        
        // 3. 스마트 분류 및 정렬
        return categorizeAndSortRecipesSmart(basicResponse.getRecommendedRecipes(), 
                userIngredients, requestDto.getSelectedIngredients());
    }
    
    private List<SmartRecommendationRequestDto.UserIngredientInfo> getUserIngredientInfos(
            String userId, List<String> selectedIngredients) {
        
        if (userId == null) {
            // 비회원의 경우 기본값 반환
            return selectedIngredients.stream()
                    .map(ingredient -> {
                        SmartRecommendationRequestDto.UserIngredientInfo info = 
                            new SmartRecommendationRequestDto.UserIngredientInfo();
                        info.setName(ingredient);
                        info.setExpiryDaysLeft(7); // 기본 7일
                        info.setFrozen(false);
                        info.setCategory("기타");
                        return info;
                    })
                    .collect(Collectors.toList());
        }
        
        try {
            User user = userRepository.findByUsernameAndDeletedFalse(userId);
            if (user == null) {
                return Collections.emptyList();
            }
            
            List<UserIngredient> userIngredients = userIngredientRepository.findByUserId(user.getId());
            
            return userIngredients.stream()
                    .filter(ui -> {
                        String ingredientName = ui.getCustomName() != null && !ui.getCustomName().trim().isEmpty() 
                                ? ui.getCustomName().trim() 
                                : (ui.getIngredient() != null ? ui.getIngredient().getName() : "");
                        return selectedIngredients.contains(ingredientName);
                    })
                    .map(ui -> {
                        SmartRecommendationRequestDto.UserIngredientInfo info = 
                            new SmartRecommendationRequestDto.UserIngredientInfo();
                        info.setName(ui.getCustomName() != null && !ui.getCustomName().trim().isEmpty() 
                                ? ui.getCustomName().trim() 
                                : ui.getIngredient().getName());
                        // 유통기한 계산 - null 체크 없이 직접 계산
                        long expiryDays = ui.getExpiryDaysLeft();
                        info.setExpiryDaysLeft(expiryDays == Long.MAX_VALUE ? null : (int) expiryDays);
                        info.setFrozen(ui.getIsFrozen() != null ? ui.getIsFrozen() : false);
                        info.setCategory(ui.getIngredient() != null && ui.getIngredient().getCategory() != null 
                                ? ui.getIngredient().getCategory().name() : "기타");
                        info.setCustomName(ui.getCustomName());
                        return info;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("사용자 재료 정보 조회 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    private SmartRecommendationResponseDto categorizeAndSortRecipesSmart(
            List<RecommendedRecipeDto> recipes,
            List<SmartRecommendationRequestDto.UserIngredientInfo> userIngredients,
            List<String> selectedIngredients) {
        
        List<SmartRecommendedRecipeDto> perfectMatches = new ArrayList<>();
        List<SmartRecommendedRecipeDto> oneMissingMatches = new ArrayList<>();
        List<SmartRecommendedRecipeDto> twoMissingMatches = new ArrayList<>();
        List<SmartRecommendedRecipeDto> otherMatches = new ArrayList<>();
        
        for (RecommendedRecipeDto recipe : recipes) {
            SmartRecommendedRecipeDto smartRecipe = convertToSmartRecipe(recipe, userIngredients, selectedIngredients);
            
            switch (smartRecipe.getMatchStatus()) {
                case "PERFECT":
                    perfectMatches.add(smartRecipe);
                    break;
                case "MISSING_1":
                    oneMissingMatches.add(smartRecipe);
                    break;
                case "MISSING_2":
                    twoMissingMatches.add(smartRecipe);
                    break;
                default:
                    otherMatches.add(smartRecipe);
                    break;
            }
        }
        
        // 각 카테고리 내에서 긴급도 순으로 정렬
        sortByUrgency(perfectMatches);
        sortByUrgency(oneMissingMatches);
        sortByUrgency(twoMissingMatches);
        
        // 최종 결과 조합
        List<SmartRecommendedRecipeDto> finalRecipes = new ArrayList<>();
        finalRecipes.addAll(perfectMatches.subList(0, Math.min(perfectMatches.size(), 5)));
        finalRecipes.addAll(oneMissingMatches.subList(0, Math.min(oneMissingMatches.size(), 3)));
        finalRecipes.addAll(twoMissingMatches.subList(0, Math.min(twoMissingMatches.size(), 2)));
        
        // 긴급 재료 추출
        List<String> urgentIngredients = userIngredients.stream()
                .filter(ui -> ui.getExpiryDaysLeft() != null && ui.getExpiryDaysLeft() <= 2 && !ui.getFrozen())
                .map(SmartRecommendationRequestDto.UserIngredientInfo::getName)
                .collect(Collectors.toList());
        
        SmartRecommendationResponseDto response = new SmartRecommendationResponseDto();
        response.setRecommendedRecipes(finalRecipes);
        response.setTotalCount(finalRecipes.size());
        response.setSelectedIngredients(selectedIngredients);
        
        SmartRecommendationResponseDto.SmartCategoryInfo categoryInfo = 
            new SmartRecommendationResponseDto.SmartCategoryInfo();
        categoryInfo.setPerfectMatches(perfectMatches.size());
        categoryInfo.setOneMissingMatches(oneMissingMatches.size());
        categoryInfo.setTwoMissingMatches(twoMissingMatches.size());
        categoryInfo.setOtherMatches(otherMatches.size());
        
        response.setCategoryInfo(categoryInfo);
        response.setUrgentIngredients(urgentIngredients);
        
        return response;
    }
    
    private SmartRecommendedRecipeDto convertToSmartRecipe(
            RecommendedRecipeDto recipe,
            List<SmartRecommendationRequestDto.UserIngredientInfo> userIngredients,
            List<String> selectedIngredients) {
        
        // 레시피 필요 재료 분석
        List<String> recipeIngredients = parseRecipeIngredients(recipe.getIngredients());
        
        // 매칭 분석
        List<String> matchedIngredients = new ArrayList<>();
        List<String> missingIngredients = new ArrayList<>();
        List<String> urgentIngredientsForRecipe = new ArrayList<>();
        
        for (String recipeIng : recipeIngredients) {
            boolean found = false;
            for (String selectedIng : selectedIngredients) {
                if (isIngredientMatch(recipeIng, selectedIng)) {
                    matchedIngredients.add(selectedIng);
                    
                    // 긴급도 체크
                    userIngredients.stream()
                            .filter(ui -> ui.getName().equals(selectedIng))
                            .filter(ui -> ui.getExpiryDaysLeft() != null && ui.getExpiryDaysLeft() <= 2 && !ui.getFrozen())
                            .findFirst()
                            .ifPresent(ui -> urgentIngredientsForRecipe.add(ui.getName()));
                    
                    found = true;
                    break;
                }
            }
            if (!found) {
                missingIngredients.add(recipeIng);
            }
        }
        
        // 상태 결정
        String matchStatus;
        int missingCount = missingIngredients.size();
        if (missingCount == 0) {
            matchStatus = "PERFECT";
        } else if (missingCount == 1) {
            matchStatus = "MISSING_1";
        } else if (missingCount == 2) {
            matchStatus = "MISSING_2";
        } else {
            matchStatus = "OTHER";
        }
        
        // 긴급도 점수 계산
        int urgencyScore = calculateUrgencyScore(matchedIngredients, userIngredients);
        
        // 추천 이유 생성
        String recommendReason = generateRecommendReason(urgentIngredientsForRecipe, missingIngredients, matchStatus);
        
        return new SmartRecommendedRecipeDto(
                recipe.getRecipeId(),
                recipe.getRecipeName(),
                recipe.getIngredients(),
                recipe.getCookingMethod1(),
                recipe.getCookingMethod2(),
                recipe.getImageUrl(),
                matchedIngredients.size(),
                matchedIngredients,
                missingIngredients,
                recipe.getMatchScore(),
                recipe.isFavorite(),
                matchStatus,
                urgencyScore,
                urgentIngredientsForRecipe,
                recommendReason
        );
    }
    
    private List<String> parseRecipeIngredients(String ingredientsText) {
        if (ingredientsText == null || ingredientsText.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        return Arrays.stream(ingredientsText.split(",|\\|"))  
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .limit(10) // 최대 10개만
                .collect(Collectors.toList());
    }
    
    private boolean isIngredientMatch(String recipeIngredient, String userIngredient) {
        String recipe = recipeIngredient.toLowerCase().trim();
        String user = userIngredient.toLowerCase().trim();
        
        // 정확 매칭
        if (recipe.equals(user)) return true;
        
        // 포함 관계
        if (recipe.contains(user) || user.contains(recipe)) return true;
        
        // 동의어 사전 (간단한 버전)
        Map<String, List<String>> synonyms = Map.of(
                "파프리카", List.of("피망", "빨간피망", "노란피망"),
                "피망", List.of("파프리카"),
                "양배추", List.of("배추", "캐비지"),
                "배추", List.of("양배추"),
                "대파", List.of("파", "쪽파"),
                "파", List.of("대파", "쪽파"),
                "삼겹살", List.of("돼지고기", "돼지삼겹살"),
                "돼지고기", List.of("삼겹살"),
                "고춧가루", List.of("고추가루"),
                "고추가루", List.of("고춧가루")
        );
        
        return synonyms.getOrDefault(user, Collections.emptyList()).contains(recipe) ||
               synonyms.getOrDefault(recipe, Collections.emptyList()).contains(user);
    }
    
    private int calculateUrgencyScore(List<String> matchedIngredients, 
            List<SmartRecommendationRequestDto.UserIngredientInfo> userIngredients) {
        
        return matchedIngredients.stream()
                .mapToInt(ingredient -> 
                        userIngredients.stream()
                                .filter(ui -> ui.getName().equals(ingredient))
                                .mapToInt(ui -> ui.getExpiryDaysLeft() != null ? ui.getExpiryDaysLeft() : 999)
                                .min()
                                .orElse(999))
                .min()
                .orElse(999);
    }
    
    private String generateRecommendReason(List<String> urgentIngredients, List<String> missingIngredients, String matchStatus) {
        // 긴급 재료가 있으면 우선 표시
        if (!urgentIngredients.isEmpty()) {
            return String.format("%s이(가) 곧 만료되니 빨리 사용하세요!", String.join(", ", urgentIngredients));
        }
        
        // 매칭 상태에 따른 메시지
        switch (matchStatus) {
            case "PERFECT":
                return "모든 재료가 준비되어 있어요! 바로 만들 수 있어요.";
            case "MISSING_1":
            case "MISSING_2":
                if (!missingIngredients.isEmpty()) {
                    return String.format("💡 %s만 더 있으면 완성!", String.join(", ", missingIngredients));
                }
                break;
        }
        
        return "추천 레시피에요!";
    }
    
    private void sortByUrgency(List<SmartRecommendedRecipeDto> recipes) {
        recipes.sort((r1, r2) -> {
            // 1차: 매칭 점수 (높을수록 우선) - 점수 우선순위!
            int scoreCompare = Double.compare(r2.getMatchScore(), r1.getMatchScore());
            if (scoreCompare != 0) return scoreCompare;
            
            // 2차: 긴급도 (낮을수록 우선) - 동점일 때만 고려
            return Integer.compare(r1.getUrgencyScore(), r2.getUrgencyScore());
        });
    }

    /**
     * 보유 중인 식재료로 만들 수 있는 레시피 랜덤 1개 반환
     */
    public void generateRecipeRecommendationForAllUsers() {
        List<User> users = userRepository.findAllByDeletedFalse();

        log.info("🍳 전체 사용자 수: {}", users.size());

        for (User user : users) {
            log.info("👤 [유저 {}] 추천 레시피 생성 시작", user.getUsername());

            // ↓ 직접 인라인 처리
            List<UserIngredient> userIngredients = userIngredientRepository.findByUserId(user.getId());

            List<String> fridgeIngredientNames = userIngredients.stream()
                    .map(ui -> {
                        if (ui.getCustomName() != null && !ui.getCustomName().trim().isEmpty()) {
                            return ui.getCustomName().trim();
                        } else if (ui.getIngredient() != null) {
                            return ui.getIngredient().getName().trim();
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .toList();

            List<String> bookmarkedRecipeIds = bookmarkRepository.findByUserId(user.getId()).stream()
                    .map(b -> b.getRecipe().getRcpSeq())
                    .toList();

            List<Recipe> matched = recipeIngredientRepository.findRecipesByIngredientsExcludingRecipeIds(
                    fridgeIngredientNames, bookmarkedRecipeIds
            );

            if (matched.isEmpty()) {
                log.info("❌ [유저 {}] 추천 가능한 레시피 없음", user.getUsername());
                continue;
            }

            Recipe recipe = matched.get(new Random().nextInt(matched.size()));

            log.info("✅ [유저 {}] 추천 레시피: {}", user.getUsername(), recipe.getRcpNm());

            notificationService.sendRecipeRecommendation(user.getId(), recipe);
        }
    }


}
