package com.ohgiraffers.refrigegobackend.bookmark.service;

import com.ohgiraffers.refrigegobackend.bookmark.domain.Bookmark;
import com.ohgiraffers.refrigegobackend.bookmark.dto.response.BookmarkRecipeResponseDTO;
import com.ohgiraffers.refrigegobackend.bookmark.dto.response.CuisineTypeRecipeResponseDTO;
import com.ohgiraffers.refrigegobackend.bookmark.dto.response.SimilarRecipeResponseDTO;
import com.ohgiraffers.refrigegobackend.bookmark.dto.response.UserIngredientRecipeResponseDTO;
import com.ohgiraffers.refrigegobackend.bookmark.infrastructure.repository.BookmarkRepository;
import com.ohgiraffers.refrigegobackend.ingredient.domain.UserIngredient;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.UserIngredientRepository;
import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository.RecipeRepository;
import com.ohgiraffers.refrigegobackend.user.entity.UserEntity;
import com.ohgiraffers.refrigegobackend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookmarkService {

    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;
    private final RecipeRepository recipeRepository;
    private final UserIngredientRepository userIngredientRepository;

    @Autowired
    public BookmarkService(UserRepository userRepository, BookmarkRepository bookmarkRepository, RecipeRepository recipeRepository, UserIngredientRepository userIngredientRepository) {
        this.userRepository = userRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.recipeRepository = recipeRepository;
        this.userIngredientRepository = userIngredientRepository;
    }

    // 레시피 찜하기
    public boolean toggleBookmark(int userId, String recipeId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("레시피 없음"));

        try {
            Optional<Bookmark> existing = bookmarkRepository.findByUserIdAndRecipeRcpSeq(userId, recipeId);

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

    // 찜한 레시피 목록 조회
    public List<BookmarkRecipeResponseDTO> getBookmarkedRecipes(int userId) {
        List<Recipe> recipes = bookmarkRepository.findRecipesByUserId(userId);

        return recipes.stream()
                .map(BookmarkRecipeResponseDTO::new) // Recipe -> DTO
                .collect(Collectors.toList());       // 리스트로 변환
    }


    // 찜한 레시피 밑에 비슷한 재료로 만든 레시피 목록 - 레시피 화면 (재료 기준)
    public List<SimilarRecipeResponseDTO> getSimilarRecipes(int userId) {
        // 1. 사용자의 찜한 레시피 목록 가져오기
        List<Bookmark> bookmarks = bookmarkRepository.findByUserId(userId);
        List<Recipe> likedRecipes = bookmarks.stream()
                .map(Bookmark::getRecipe)
                .toList();

        // 2. 찜한 레시피들의 재료 모두 수집
        Set<String> likedIngredients = likedRecipes.stream()
                .flatMap(recipe -> Arrays.stream(recipe.getRcpPartsDtls().split("[,\\n]")))
                .map(s -> s.replaceAll("[^가-힣a-zA-Z]", "").trim())
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        // 3. 전체 레시피 중 찜한 레시피를 제외
        List<Recipe> allRecipes = recipeRepository.findAll();
        List<Recipe> otherRecipes = allRecipes.stream()
                .filter(r -> !likedRecipes.contains(r))
                .toList();

        // 4. 찜한 재료 중 하나 이상 포함하는 다른 레시피 필터링
        List<Recipe> similarRecipes = otherRecipes.stream()
                .filter(recipe -> {
                    String parts = recipe.getRcpPartsDtls();
                    if (parts == null) return false;
                    List<String> recipeIngredients = Arrays.stream(parts.split("[,\\n]"))
                            .map(s -> s.replaceAll("[^가-힣a-zA-Z]", "").trim())
                            .toList();
                    return recipeIngredients.stream().anyMatch(likedIngredients::contains);
                })
                .toList();

        // 5. DTO로 변환
        return similarRecipes.stream()
                .map(SimilarRecipeResponseDTO::new)
                .toList();
    }


    // 찜한 레시피와 비슷한 레시피 목록 - 메인화면 (요리 종류 기준)
    public List<CuisineTypeRecipeResponseDTO> getRecommendedRecipesByBookmarked(int userId) {
        // 1. 유저가 찜한 레시피 ID 목록 (Set으로 변환)
        List<String> bookmarkedRecipeIdsList = bookmarkRepository.findRecipeIdsByUserId(userId);
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

        // 4. DTO 변환 (추천 목록이니 bookmarked false or 포함 여부 체크)
        return recommendedRecipes.stream()
                .map(recipe -> new CuisineTypeRecipeResponseDTO(recipe, bookmarkedRecipeIds.contains(recipe.getRcpSeq())))
                .collect(Collectors.toList());
    }

    // 찜한 레시피 중 현재 만들 수 있는 레시피 목록 - 메인화면
    public List<UserIngredientRecipeResponseDTO> getRecommendedRecipesByUserIngredient(int userId) {
        // 냉장고 재료 조회
        List<UserIngredient> userIngredients = userIngredientRepository.findByUserId(String.valueOf(userId));
        List<String> fridgeIngredientNames = userIngredients.stream()
                .map(UserIngredient::getCustomName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .toList();

        // 찜한 레시피 조회
        List<Bookmark> bookmarks = bookmarkRepository.findByUserId(userId);
        List<Recipe> likedRecipes = bookmarks.stream()
                .map(Bookmark::getRecipe)
                .toList();

        // 필터링
        List<Recipe> matchedRecipes = likedRecipes.stream()
                .filter(recipe -> {
                    String parts = recipe.getRcpPartsDtls();
                    if (parts == null) return false;

                    List<String> recipeIngredients = Arrays.stream(parts.split("[●•\\n]"))  // ● 시점으로 구분
                            .flatMap(section -> Arrays.stream(section.split("[:,]")))  // 콜론(:) 또는 쉼표(,)로 나누기
                            .map(s -> s.trim().split(" ")[0])  // 앞 단어(재료명)만 가져오기
                            .map(s -> s.replaceAll("[^가-힣a-zA-Z]", "").trim())  // 특수문자 제거
                            .filter(s -> !s.isBlank())  // 빈 문자열 제거
                            .toList();

                    return recipeIngredients.stream().anyMatch(
                            ri -> fridgeIngredientNames.stream().anyMatch(ri::contains)
                    );
                })
                .toList();

        // Dto로 변환
        return matchedRecipes.stream()
                .map(recipe -> new UserIngredientRecipeResponseDTO(recipe))
                .toList();
    }
}
