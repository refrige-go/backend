package com.ohgiraffers.refrigegobackend.bookmark.service;

import com.ohgiraffers.refrigegobackend.bookmark.domain.Bookmark;
import com.ohgiraffers.refrigegobackend.bookmark.infrastructure.repository.BookmarkRepository;
import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final RecipeRepository recipeRepository;

    @Autowired
    public BookmarkService(BookmarkRepository bookmarkRepository, RecipeRepository recipeRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.recipeRepository = recipeRepository;
    }

    // 레시피 찜하기
    public boolean toggleBookmark(Long userId, String recipeId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("레시피 없음"));

        try {
            Optional<Bookmark> existing = bookmarkRepository.findByUserIdAndRecipeRcpSeq(userId, recipeId);

            if (existing.isPresent()) {
                bookmarkRepository.delete(existing.get());
                return false; // 찜 해제
            } else {
                Bookmark bookmark = new Bookmark();
                bookmark.setUserId(userId);
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

    // 찜한 레시피 밑에 비슷한 재료로 만든 레시피 목록 - 레시피 화면 (재료 기준)

    // 찜한 레시피와 비슷한 레시피 목록 - 메인화면 (요리 종류 기준)
    public List<Recipe> getRecommendedRecipesByBookmarked(Long userId) {
        // 1. 유저가 찜한 레시피 ID 목록
        List<String> bookmarkedRecipeIds = bookmarkRepository.findRecipeIdsByUserId(userId);

        if (bookmarkedRecipeIds.isEmpty()) return Collections.emptyList();

        // 2. 찜한 레시피들의 요리 종류 가져오기
        List<Recipe> likedRecipes = recipeRepository.findAllById(bookmarkedRecipeIds);
        List<String> cuisineTypes = likedRecipes.stream()
                .map(Recipe::getCuisineType)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (cuisineTypes.isEmpty()) return Collections.emptyList();

        // 3. 추천 레시피 (찜하지 않은 것 중에서 요리 종류 일치)
        return recipeRepository.findByCuisineTypeInAndRcpSeqNotIn(cuisineTypes, bookmarkedRecipeIds);
    }


    // 찜한 레시피 중 현재 만들 수 있는 레시피 목록 - 메인화면
}
