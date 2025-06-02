package com.ohgiraffers.refrigegobackend.bookmark.controller;

import com.ohgiraffers.refrigegobackend.bookmark.dto.response.*;
import com.ohgiraffers.refrigegobackend.bookmark.service.BookmarkService;
import com.ohgiraffers.refrigegobackend.user.dto.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmark")
public class BookmarkController {

    Logger log = LoggerFactory.getLogger(BookmarkController.class);

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }


    /**
     * 레시피 찜(북마크) 토글
     * - 레시피를 찜하거나 취소할 수 있음
     * @param userDetails 로그인된 사용자 정보
     * @param recipeId 레시피 아이디
     */
    @PostMapping("/toggle")
    public ResponseEntity<BookmarkResponseDTO> toggleFavorite(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                              @RequestParam String recipeId) {
        String username = userDetails.getUsername();
        boolean isBookmarked = bookmarkService.toggleBookmark(username, recipeId);

        log.info("Bookmark status is {}", isBookmarked);

        BookmarkResponseDTO response = new BookmarkResponseDTO(
                isBookmarked,
                isBookmarked ? "찜 추가됨" : "찜 해제됨");

        return ResponseEntity.ok(response);
    }


    /**
     * 사용자의 찜한 레시피 목록 조회
     * @param userDetails 로그인된 사용자 정보
     */
    @GetMapping("/list")
    public ResponseEntity<?> getCurrentUserBookmarks(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        String username = userDetails.getUsername();
        log.info("Username is {}", username);

        List<BookmarkRecipeResponseDTO> bookmarkedRecipes = bookmarkService.getBookmarkedRecipes(username);
        return ResponseEntity.ok(bookmarkedRecipes);
    }


    /**
     * 사용자 맞춤 레시피 추천
     * - 찜한 레시피와 요리 타입이 같은 전체 레시피 목록 조회 (ex. 끓이기, 볶기...)
     * @param userDetails 로그인된 사용자 정보
     */
    @GetMapping("/bookmark-recommend")
    public ResponseEntity<List<CuisineTypeRecipeResponseDTO>> getRecommendedRecipesByBookmarked(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("Username is {}", username);

        List<CuisineTypeRecipeResponseDTO> recommendations = bookmarkService
                .getRecommendedRecipesByBookmarked(username);
        return ResponseEntity.ok(recommendations);
    }


    /**
     * 보유 중인 식재료로 만들 수 있는 찜한 레시피 조회
     * @param userDetails 로그인된 사용자 정보
     */
    @GetMapping("/ingredient-recommend")
    public ResponseEntity<List<UserIngredientRecipeResponseDTO>> getRecommendedRecipesByUserIngredient(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("Username is {}", username);

        List<UserIngredientRecipeResponseDTO> recommendedRecipes = bookmarkService
                .getRecommendedRecipesByUserIngredient(username);
        return ResponseEntity.ok(recommendedRecipes);
    }
}
