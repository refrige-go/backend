package com.ohgiraffers.refrigegobackend.bookmark.controller;

import com.ohgiraffers.refrigegobackend.bookmark.dto.response.BookmarkResponseDTO;
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

    // 찜한 레시피 밑에 비슷한 재료로 만든 레시피 목록 - 레시피 화면 (재료 기준)

    // 찜한 레시피와 비슷한 레시피 목록 - 메인화면 (요리 종류 기준)
    @GetMapping("/recommendations")
    public ResponseEntity<List<Recipe>> getRecommendations(@RequestParam Long userId) {
        List<Recipe> recommendations = bookmarkService.getRecommendedRecipesByBookmarked(userId);
        return ResponseEntity.ok(recommendations);
    }

    // 찜한 레시피 중 현재 만들 수 있는 레시피 목록 - 메인화면
}
