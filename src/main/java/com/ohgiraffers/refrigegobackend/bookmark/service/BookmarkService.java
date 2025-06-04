package com.ohgiraffers.refrigegobackend.bookmark.service;

import com.ohgiraffers.refrigegobackend.bookmark.domain.Bookmark;
import com.ohgiraffers.refrigegobackend.bookmark.dto.response.BookmarkRecipeResponseDTO;
import com.ohgiraffers.refrigegobackend.bookmark.dto.response.CuisineTypeRecipeResponseDTO;
import com.ohgiraffers.refrigegobackend.bookmark.dto.response.UserIngredientRecipeResponseDTO;
import com.ohgiraffers.refrigegobackend.bookmark.infrastructure.repository.BookmarkRepository;
import com.ohgiraffers.refrigegobackend.ingredient.domain.UserIngredient;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.UserIngredientRepository;
import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository.RecipeRepository;
import com.ohgiraffers.refrigegobackend.recommendation.infrastructure.repository.RecipeIngredientRepository;
import com.ohgiraffers.refrigegobackend.user.entity.User;
import com.ohgiraffers.refrigegobackend.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public BookmarkService(UserRepository userRepository,
                           BookmarkRepository bookmarkRepository,
                           RecipeRepository recipeRepository,
                           UserIngredientRepository userIngredientRepository,
                           RecipeIngredientRepository recipeIngredientRepository) {
        this.userRepository = userRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.recipeRepository = recipeRepository;
        this.userIngredientRepository = userIngredientRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
    }


    /**
     * 레시피 찜 / 해제
     * @param username
     * @param recipeId
     */
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
     * @param username
     */
    public List<BookmarkRecipeResponseDTO> getBookmarkedRecipes(String username) {
        User user = userRepository.findByUsernameAndDeletedFalse(username);
        List<Recipe> recipes = bookmarkRepository.findRecipesByUserId(user.getId());

        try {
            List<BookmarkRecipeResponseDTO> result = recipes.stream()
                    .map(BookmarkRecipeResponseDTO::new) // Recipe -> DTO
                    .collect(Collectors.toList());       // 리스트로 변환

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     사용자 맞춤 레시피 추천
     * - 찜한 레시피와 요리 타입이 같은 전체 레시피 목록 조회 (ex. 반찬, 후식...)
     * @param username
     */
    public List<CuisineTypeRecipeResponseDTO> getRecommendedRecipesByBookmarked(String username) {
        User user = userRepository.findByUsernameAndDeletedFalse(username);

        // 1. 유저가 찜한 레시피 ID 목록 (Set으로 변환)
        List<String> bookmarkedRecipeIdsList = bookmarkRepository.findRecipeIdsByUserId(user.getId());
        if (bookmarkedRecipeIdsList.isEmpty()) return Collections.emptyList();

        Set<String> bookmarkedRecipeIds = new HashSet<>(bookmarkedRecipeIdsList);

        // 2. 찜한 레시피들의 요리 종류 가져오기
        List<Recipe> likedRecipes = recipeRepository.findAllById(bookmarkedRecipeIdsList);
        List<String> cuisineTypes = likedRecipes.stream()
                .map(Recipe::getCuisineType)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (cuisineTypes.isEmpty()) return Collections.emptyList();

        // 3. 찜하지 않은 같은 요리 종류 레시피 조회
        List<Recipe> recommendedRecipes = recipeRepository.findByCuisineTypeInAndRcpSeqNotIn(cuisineTypes, bookmarkedRecipeIdsList);

        // 4. DTO 변환
        return recommendedRecipes.stream()
                .map(recipe -> new CuisineTypeRecipeResponseDTO(recipe, bookmarkedRecipeIds.contains(recipe.getRcpSeq())))
                .collect(Collectors.toList());
    }


    /**
     * 보유 중인 식재료로 만들 수 있는 찜한 레시피 조회 (링크 테이블 사용)
     * @param username
     * @return
     */
    public List<UserIngredientRecipeResponseDTO> getRecommendedRecipesByUserIngredient(String username) {
        User user = userRepository.findByUsernameAndDeletedFalse(username);
        List<UserIngredient> userIngredients = userIngredientRepository.findByUserId(user.getId());

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

        // 찜한 레시피 조회
        List<Bookmark> bookmarks = bookmarkRepository.findByUserId(user.getId());
        List<Recipe> likedRecipes = bookmarks.stream()
                .map(Bookmark::getRecipe)
                .toList();

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
}
