package com.ohgiraffers.refrigegobackend.bookmark.service;

import com.ohgiraffers.refrigegobackend.ai.service.AiRecommendationService;
import com.ohgiraffers.refrigegobackend.bookmark.domain.Bookmark;
import com.ohgiraffers.refrigegobackend.bookmark.dto.response.BookmarkRecipeResponseDTO;
import com.ohgiraffers.refrigegobackend.bookmark.dto.response.CuisineTypeRecipeResponseDTO;
import com.ohgiraffers.refrigegobackend.bookmark.dto.response.UserIngredientRecipeResponseDTO;
import com.ohgiraffers.refrigegobackend.bookmark.infrastructure.repository.BookmarkRepository;
import com.ohgiraffers.refrigegobackend.ingredient.domain.UserIngredient;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.UserIngredientRepository;
import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository.RecipeRepository;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecipeRecommendationRequestDto;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecipeRecommendationResponseDto;
import com.ohgiraffers.refrigegobackend.recommendation.dto.RecommendedRecipeDto;
import com.ohgiraffers.refrigegobackend.recommendation.infrastructure.repository.RecipeIngredientRepository;
import com.ohgiraffers.refrigegobackend.search.dto.response.RecipeSearchResultDTO;
import com.ohgiraffers.refrigegobackend.search.dto.response.VectorSearchResponseDTO;
import com.ohgiraffers.refrigegobackend.search.service.SearchService;
import com.ohgiraffers.refrigegobackend.user.entity.User;
import com.ohgiraffers.refrigegobackend.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookmarkService {

    Logger log = LoggerFactory.getLogger(BookmarkService.class);

    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;
    private final RecipeRepository recipeRepository;
    private final UserIngredientRepository userIngredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final AiRecommendationService aiRecommendationService;
    private final SearchService searchService;

    @Autowired
    public BookmarkService(UserRepository userRepository,
            BookmarkRepository bookmarkRepository,
            RecipeRepository recipeRepository,
            UserIngredientRepository userIngredientRepository,
            RecipeIngredientRepository recipeIngredientRepository,
            AiRecommendationService aiRecommendationService,
            SearchService searchService) {
        this.userRepository = userRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.recipeRepository = recipeRepository;
        this.userIngredientRepository = userIngredientRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.aiRecommendationService = aiRecommendationService;
        this.searchService = searchService;
    }

    /**
     * 레시피 찜 / 해제
     * 
     * @param username
     * @param recipeId
     */
    @CacheEvict(value = "bookmarks", key = "#username")
    public boolean toggleBookmark(String username, String recipeId) {
        User user = userRepository.findByUsernameAndDeletedFalse(username);
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("레시피 없음"));

        try {
            Optional<Bookmark> existing = bookmarkRepository.findByUserIdAndRecipeRcpSeq(user.getId(), recipeId);

            if (existing.isPresent()) {
                bookmarkRepository.delete(existing.get());
                return false; // 찜 해제
            } else {
                Bookmark bookmark = new Bookmark();
                bookmark.setUser(user);
                bookmark.setRecipe(recipe);
                bookmark.setCreatedAt(LocalDateTime.now());
                bookmarkRepository.save(bookmark);
                return true; // 찜 추가
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 사용자의 찜한 레시피 목록 조회
     * 
     * @param username
     */
    public List<BookmarkRecipeResponseDTO> getBookmarkedRecipes(String username) {
        User user = userRepository.findByUsernameAndDeletedFalse(username);
        List<Recipe> recipes = bookmarkRepository.findRecipesByUserId(user.getId());

        try {
            List<BookmarkRecipeResponseDTO> result = recipes.stream()
                    .map(BookmarkRecipeResponseDTO::new) // Recipe -> DTO
                    .collect(Collectors.toList()); // 리스트로 변환

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 사용자의 찜한 레시피 목록 조회 (Recipe 엔티티 포함)
     * 
     * @param username
     * @return Recipe 엔티티와 DTO를 함께 반환
     */
    private Map<BookmarkRecipeResponseDTO, Recipe> getBookmarkedRecipesWithEntities(String username) {
        User user = userRepository.findByUsernameAndDeletedFalse(username);
        List<Recipe> recipes = bookmarkRepository.findRecipesByUserId(user.getId());

        try {
            Map<BookmarkRecipeResponseDTO, Recipe> result = recipes.stream()
                    .collect(Collectors.toMap(
                            BookmarkRecipeResponseDTO::new, // Recipe -> DTO
                            recipe -> recipe // Recipe 엔티티 그대로
                    ));

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    /**
     * 사용자 맞춤 레시피 추천
     * - 찜한 레시피와 요리 타입이 같은 전체 레시피 목록 조회 (ex. 반찬, 후식...)
     * 
     * @param username
     */
    public List<CuisineTypeRecipeResponseDTO> getRecommendedRecipesByBookmarked(String username) {
        User user = userRepository.findByUsernameAndDeletedFalse(username);

        // 1. 유저가 찜한 레시피 ID 목록 (Set으로 변환)
        List<String> bookmarkedRecipeIdsList = bookmarkRepository.findRecipeIdsByUserId(user.getId());
        if (bookmarkedRecipeIdsList.isEmpty())
            return Collections.emptyList();

        Set<String> bookmarkedRecipeIds = new HashSet<>(bookmarkedRecipeIdsList);

        // 2. 찜한 레시피들의 요리 종류 가져오기
        List<Recipe> likedRecipes = recipeRepository.findAllById(bookmarkedRecipeIdsList);
        List<String> cuisineTypes = likedRecipes.stream()
                .map(Recipe::getCuisineType)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (cuisineTypes.isEmpty())
            return Collections.emptyList();

        // 3. 찜하지 않은 같은 요리 종류 레시피 조회
        List<Recipe> recommendedRecipes = recipeRepository.findByCuisineTypeInAndRcpSeqNotIn(cuisineTypes,
                bookmarkedRecipeIdsList);

        // 4. DTO 변환
        return recommendedRecipes.stream()
                .map(recipe -> new CuisineTypeRecipeResponseDTO(recipe,
                        bookmarkedRecipeIds.contains(recipe.getRcpSeq())))
                .collect(Collectors.toList());
    }

    /**
     * 보유 중인 식재료로 만들 수 있는 찜한 레시피 조회 (링크 테이블 사용)
     * 
     * @param username
     * @return
     */
    public List<UserIngredientRecipeResponseDTO> getRecommendedRecipesByUserIngredient(String username) {
        User user = userRepository.findByUsernameAndDeletedFalse(username);
        List<UserIngredient> userIngredients = userIngredientRepository
                .findByUserId(user.getId());

        // 사용자 재료명 추출 (customName과 표준 재료명 둘 다 고려)
        List<String> fridgeIngredientNames = userIngredients.stream()
                .map(userIngredient -> {
                    // customName이 있으면 customName, 없으면 표준 재료명
                    if (userIngredient.getCustomName() != null && !userIngredient.getCustomName().trim().isEmpty()) {
                        return userIngredient.getCustomName().trim();
                    } else if (userIngredient.getIngredient() != null) {
                        return userIngredient.getIngredient().getName().trim();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("🧊 사용자 냉장고 재료 {}", fridgeIngredientNames);

        // 찜한 레시피 조회 (N+1 쿼리 방지를 위해 findRecipesByUserId 사용)
        List<Recipe> likedRecipes = bookmarkRepository.findRecipesByUserId(user.getId());

        log.info("⭐ 찜한 레시피 수 {}", likedRecipes.size());

        // 찜한 레시피 ID 목록
        List<String> likedRecipeIds = likedRecipes.stream()
                .map(Recipe::getRcpSeq)
                .collect(Collectors.toList());

        if (likedRecipeIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 링크 테이블을 사용하여 매칭되는 레시피 찾기
        List<Recipe> matchedRecipes = recipeIngredientRepository.findRecipesByIngredientsAndRecipeIds(
                fridgeIngredientNames, likedRecipeIds);

        log.info("🍳 최종 매칭된 레시피 수 {}", matchedRecipes.size());

        // 결과 DTO 변환
        return matchedRecipes.stream()
                .map(recipe -> new UserIngredientRecipeResponseDTO(recipe, true))
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 찜한 레시피와 유사한 AI 추천 레시피 조회
     * 
     * @param username 사용자명
     * @param limit    추천 개수 제한
     * @return AI 기반 추천 레시피 목록
     */
    public List<RecommendedRecipeDto> getAiRecommendedRecipesByBookmarks(String username, int limit) {
        log.info("AI 추천 시작 - 사용자: {}, 제한: {}", username, limit);

        try {
            // 1. 사용자의 찜한 레시피 조회 (Recipe 엔티티 포함)
            Map<BookmarkRecipeResponseDTO, Recipe> bookmarkedRecipesWithEntities = getBookmarkedRecipesWithEntities(
                    username);

            if (bookmarkedRecipesWithEntities.isEmpty()) {
                log.info("사용자의 찜한 레시피가 없습니다: {}", username);
                return Collections.emptyList();
            }

            // 2. 찜한 레시피들의 재료 정보 추출 (최적화된 버전)
            List<String> userIngredients = extractIngredientsFromBookmarksOptimized(bookmarkedRecipesWithEntities);

            if (userIngredients.isEmpty()) {
                log.warn("찜한 레시피에서 재료 정보를 추출할 수 없습니다: {}", username);
                return Collections.emptyList();
            }

            log.info("추출된 재료: {}", userIngredients);

            // 3. AI 서버에 추천 요청
            RecipeRecommendationRequestDto requestDto = new RecipeRecommendationRequestDto();
            requestDto.setUserId(username);
            requestDto.setSelectedIngredients(userIngredients);
            requestDto.setLimit(limit);

            RecipeRecommendationResponseDto aiResponse = aiRecommendationService.recommendRecipesWithAi(requestDto);

            log.info("AI 추천 완료 - 추천된 레시피 수: {}", aiResponse.getRecommendedRecipes().size());

            return aiResponse.getRecommendedRecipes();

        } catch (Exception e) {
            log.error("AI 추천 중 오류 발생: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 찜한 레시피와 유사한 벡터 검색 결과 조회
     * 
     * @param username 사용자명
     * @param limit    검색 결과 개수 제한
     * @return 벡터 검색 결과 목록
     */
    public List<RecipeSearchResultDTO> getVectorSearchByBookmarks(String username, int limit) {
        log.info("벡터 검색 시작 - 사용자: {}, 제한: {}", username, limit);

        try {
            // 1. 사용자의 찜한 레시피 조회 (Recipe 엔티티 포함)
            Map<BookmarkRecipeResponseDTO, Recipe> bookmarkedRecipesWithEntities = getBookmarkedRecipesWithEntities(
                    username);

            if (bookmarkedRecipesWithEntities.isEmpty()) {
                log.info("사용자의 찜한 레시피가 없습니다: {}", username);
                return Collections.emptyList();
            }

            // 2. 찜한 레시피들의 이름을 검색어로 사용
            List<BookmarkRecipeResponseDTO> bookmarkedRecipes = new ArrayList<>(bookmarkedRecipesWithEntities.keySet());
            String searchQuery = createSearchQueryFromBookmarks(bookmarkedRecipes);

            if (searchQuery.trim().isEmpty()) {
                log.warn("검색어를 생성할 수 없습니다: {}", username);
                return Collections.emptyList();
            }

            log.info("생성된 검색어: {}", searchQuery);

            // 3. 벡터 검색 수행
            VectorSearchResponseDTO vectorResponse = searchService.performVectorSearch(searchQuery, limit);

            log.info("벡터 검색 완료 - 검색 결과 수: {}", vectorResponse.getResults().size());

            return vectorResponse.getResults();

        } catch (Exception e) {
            log.error("벡터 검색 중 오류 발생: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 찜한 레시피의 재료 정보 추출 (최적화된 버전)
     * 
     * @param bookmarksWithEntities 찜한 레시피 DTO와 Recipe 엔티티 맵
     * @return 추출된 재료 목록
     */
    private List<String> extractIngredientsFromBookmarksOptimized(
            Map<BookmarkRecipeResponseDTO, Recipe> bookmarksWithEntities) {
        Set<String> ingredients = new HashSet<>();

        for (Map.Entry<BookmarkRecipeResponseDTO, Recipe> entry : bookmarksWithEntities.entrySet()) {
            Recipe recipe = entry.getValue();
            if (recipe != null && recipe.getRcpPartsDtls() != null && !recipe.getRcpPartsDtls().isEmpty()) {
                // 쉼표로 구분된 재료들을 분리
                String[] ingredientArray = recipe.getRcpPartsDtls().split(",");
                for (String ingredient : ingredientArray) {
                    String trimmedIngredient = ingredient.trim();
                    if (!trimmedIngredient.isEmpty()) {
                        ingredients.add(trimmedIngredient);
                    }
                }
            }
        }

        log.debug("추출된 재료 목록: {}", ingredients);
        return new ArrayList<>(ingredients);
    }

    /**
     * 찜한 레시피의 재료 정보 추출 (기존 버전 - 하위 호환성용)
     * 
     * @param bookmarks 찜한 레시피 목록
     * @return 추출된 재료 목록
     */
    private List<String> extractIngredientsFromBookmarks(List<BookmarkRecipeResponseDTO> bookmarks) {
        Set<String> ingredients = new HashSet<>();

        for (BookmarkRecipeResponseDTO bookmark : bookmarks) {
            // 레시피 ID로 Recipe 엔티티 조회하여 재료 정보 추출
            Recipe recipe = recipeRepository.findById(bookmark.getRcpSeq()).orElse(null);
            if (recipe != null && recipe.getRcpPartsDtls() != null && !recipe.getRcpPartsDtls().isEmpty()) {
                // 쉼표로 구분된 재료들을 분리
                String[] ingredientArray = recipe.getRcpPartsDtls().split(",");
                for (String ingredient : ingredientArray) {
                    String trimmedIngredient = ingredient.trim();
                    if (!trimmedIngredient.isEmpty()) {
                        ingredients.add(trimmedIngredient);
                    }
                }
            }
        }

        log.debug("추출된 재료 목록: {}", ingredients);
        return new ArrayList<>(ingredients);
    }

    /**
     * 찜한 레시피들로 검색어 생성
     * 
     * @param bookmarks 찜한 레시피 목록
     * @return 생성된 검색어
     */
    private String createSearchQueryFromBookmarks(List<BookmarkRecipeResponseDTO> bookmarks) {
        String searchQuery = bookmarks.stream()
                .map(BookmarkRecipeResponseDTO::getRcpNm)
                .filter(Objects::nonNull)
                .filter(name -> !name.trim().isEmpty())
                .collect(Collectors.joining(" "));

        log.debug("생성된 검색어: {}", searchQuery);
        return searchQuery;
    }

    /**
     * AI 서버 상태 확인
     * 
     * @return AI 서버 연결 상태
     */
    public boolean isAiServerAvailable() {
        return aiRecommendationService.isAiServerAvailable();
    }

    /**
     * 검색 서비스 상태 확인
     * 
     * @return 검색 서비스 연결 상태
     */
    public boolean isSearchServiceAvailable() {
        return searchService.checkAiServerHealth();
    }

    @CacheEvict(value = { "bookmarkedRecipes", "recommendedRecipes", "userIngredientRecipes" }, allEntries = true)
    public void invalidateCache() {
        log.info("BookmarkService cache evicted.");
    }
}
