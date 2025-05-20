package com.ohgiraffers.refrigegobackend.bookmark.controller;

import com.ohgiraffers.refrigegobackend.bookmark.dto.response.BookmarkResponseDTO;
import com.ohgiraffers.refrigegobackend.bookmark.dto.response.CuisineTypeRecipeResponseDTO;
import com.ohgiraffers.refrigegobackend.bookmark.dto.response.SimilarRecipeResponseDTO;
import com.ohgiraffers.refrigegobackend.bookmark.dto.response.UserIngredientRecipeResponseDTO;
import com.ohgiraffers.refrigegobackend.bookmark.service.BookmarkService;
import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmark")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    // 레시피 찜하기
    @PostMapping("/toggle")
    public ResponseEntity<BookmarkResponseDTO> toggleFavorite(@RequestParam Long userId, @RequestParam String recipeId) {
        boolean isBookmarked = bookmarkService.toggleBookmark(userId, recipeId);

        BookmarkResponseDTO response = new BookmarkResponseDTO(
                isBookmarked,
                isBookmarked ? "찜 추가됨" : "찜 해제됨"
        );

        return ResponseEntity.ok(response);
    }

    // 찜한 레시피 목록
    @GetMapping("/{userId}")
    public ResponseEntity<List<Recipe>> getUserBookmarks(@PathVariable Long userId) {
        List<Recipe> bookmarkedRecipes = bookmarkService.getBookmarkedRecipes(userId);
        return ResponseEntity.ok(bookmarkedRecipes);
    }


    // 찜한 레시피 밑에 비슷한 재료로 만든 레시피 목록 - 레시피 화면 (재료 기준)
    // !!! 애매함 AI로 구현해야할 듯 - 일단 나중에 생각 !!!
    @GetMapping("/similar-recipes")
    public ResponseEntity<List<SimilarRecipeResponseDTO>> getSimilarRecipes(@RequestParam Long userId) {
        List<SimilarRecipeResponseDTO> result = bookmarkService.getSimilarRecipes(userId);
        return ResponseEntity.ok(result);
    }


    // 찜한 레시피와 비슷한 레시피 목록 - 메인화면 (요리 종류 기준)
    @GetMapping("/bookmark-recommend")
    public ResponseEntity<List<CuisineTypeRecipeResponseDTO>> getRecommendedRecipesByBookmarked(@RequestParam Long userId) {
        List<CuisineTypeRecipeResponseDTO> recommendations = bookmarkService.getRecommendedRecipesByBookmarked(userId);
        return ResponseEntity.ok(recommendations);
    }

    // 찜한 레시피 중 현재 만들 수 있는 레시피 목록 - 메인화면
    @GetMapping("/ingredient-recommend")
    public ResponseEntity<List<UserIngredientRecipeResponseDTO>> getRecommendedRecipesByUserIngredient(@RequestParam Long userId) {
        List<UserIngredientRecipeResponseDTO> recommendedRecipes = bookmarkService.getRecommendedRecipesByUserIngredient(userId);
        return ResponseEntity.ok(recommendedRecipes);
    }
}
