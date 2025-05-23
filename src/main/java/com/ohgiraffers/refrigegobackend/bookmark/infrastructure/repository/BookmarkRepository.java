package com.ohgiraffers.refrigegobackend.bookmark.infrastructure.repository;

import com.ohgiraffers.refrigegobackend.bookmark.domain.Bookmark;
import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByUserUserIdAndRecipeRcpSeq(Long userId, String rcpSeq);

    @Query("SELECT l.recipe.rcpSeq FROM Bookmark l WHERE l.user.userId = :userId")
    List<String> findRecipeIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT b.recipe FROM Bookmark b WHERE b.user.userId = :userId")
    List<Recipe> findRecipesByUserId(@Param("userId") Long userId);

    List<Bookmark> findByUserUserId(Long userId);
}
