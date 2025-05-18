package com.ohgiraffers.refrigegobackend.bookmark.service;

import com.ohgiraffers.refrigegobackend.bookmark.domain.Bookmark;
import com.ohgiraffers.refrigegobackend.bookmark.infrastructure.repository.BookmarkRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;

    public BookmarkService(BookmarkRepository bookmarkRepository) {
        this.bookmarkRepository = bookmarkRepository;
    }

    // 레시피 찜하기
    public boolean toggleBookmark(Long userId, Long recipeId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
//
//        Recipe recipe = recipeRepository.findById(recipeId)
//                .orElseThrow(() -> new IllegalArgumentException("레시피 없음"));

        try {
            Optional<Bookmark> existing = bookmarkRepository.findByUserIdAndRecipeId(userId, recipeId);

            if (existing.isPresent()) {
                bookmarkRepository.delete(existing.get());
                return false; // 찜 해제
            } else {
                Bookmark bookmark = new Bookmark();
                bookmark.setUserId(userId);
                bookmark.setRecipeId(recipeId);
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

    // 찜한 레시피 중 현재 만들 수 있는 레시피 목록 - 메인화면
}
