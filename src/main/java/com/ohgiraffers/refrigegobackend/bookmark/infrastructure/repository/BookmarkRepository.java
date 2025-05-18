package com.ohgiraffers.refrigegobackend.bookmark.infrastructure.repository;

import com.ohgiraffers.refrigegobackend.bookmark.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByUserIdAndRecipeId(Long userId, Long recipeId);
    void deleteByUserIdAndRecipeId(Long userId, Long recipeId);
    List<Bookmark> findAllByUserId(Long userId);
}
