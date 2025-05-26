package com.ohgiraffers.refrigegobackend.bookmark.controller;

import com.ohgiraffers.refrigegobackend.bookmark.dto.response.*;
import com.ohgiraffers.refrigegobackend.bookmark.service.BookmarkService;
import com.ohgiraffers.refrigegobackend.common.util.SecurityUtil;
import com.ohgiraffers.refrigegobackend.user.dto.CustomUserDetails;
import com.ohgiraffers.refrigegobackend.user.entity.User;
import com.ohgiraffers.refrigegobackend.user.service.JoinService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmark")
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final JoinService joinService;

    public BookmarkController(BookmarkService bookmarkService, JoinService joinService) {
        this.bookmarkService = bookmarkService;
        this.joinService = joinService;
    }

    // 레시피 찜하기
    @PostMapping("/toggle")
    public ResponseEntity<BookmarkResponseDTO> toggleFavorite(@AuthenticationPrincipal CustomUserDetails details,
            @RequestParam String recipeId) {
        Long userId = details.getUserId();

        boolean isBookmarked = bookmarkService.toggleBookmark(userId, recipeId);

        BookmarkResponseDTO response = new BookmarkResponseDTO(
                isBookmarked,
                isBookmarked ? "찜 추가됨" : "찜 해제됨");

        return ResponseEntity.ok(response);
    }

    // 현재 로그인한 사용자의 찜한 레시피 목록
    @GetMapping("/list")
    public ResponseEntity<?> getCurrentUserBookmarks(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        String username = userDetails.getUsername();
        System.out.println("로그인된 유저 name: " + username);

        List<BookmarkRecipeResponseDTO> bookmarkedRecipes = bookmarkService.getBookmarkedRecipes(username);
        return ResponseEntity.ok(bookmarkedRecipes);
    }


    // 찜한 레시피와 비슷한 레시피 목록 - 메인화면 (요리 종류 기준)
    @GetMapping("/bookmark-recommend")
    public ResponseEntity<List<CuisineTypeRecipeResponseDTO>> getRecommendedRecipesByBookmarked(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails.getUsername();
        System.out.println("로그인된 유저 name: " + username);

        List<CuisineTypeRecipeResponseDTO> recommendations = bookmarkService.getRecommendedRecipesByBookmarked(username);
        return ResponseEntity.ok(recommendations);
    }

    // 찜한 레시피 중 현재 만들 수 있는 레시피 목록 - 메인화면
    @GetMapping("/ingredient-recommend")
    public ResponseEntity<List<UserIngredientRecipeResponseDTO>> getRecommendedRecipesByUserIngredient(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails.getUsername();
        System.out.println("로그인된 유저 name: " + username);

        List<UserIngredientRecipeResponseDTO> recommendedRecipes = bookmarkService
                .getRecommendedRecipesByUserIngredient(username);
        return ResponseEntity.ok(recommendedRecipes);
    }

    // // 찜한 레시피 밑에 비슷한 재료로 만든 레시피 목록 - 레시피 화면 (재료 기준)
    // // !!! 애매함 AI로 구현해야할 듯
    // // 찜한거 x 모든 레시피 밑에
    // @GetMapping("/similar-recipes")
    // public ResponseEntity<List<SimilarRecipeResponseDTO>>
    // getSimilarRecipes(@RequestParam Long userId) {
    // List<SimilarRecipeResponseDTO> result =
    // bookmarkService.getSimilarRecipes(userId);
    // return ResponseEntity.ok(result);
    // }
}
