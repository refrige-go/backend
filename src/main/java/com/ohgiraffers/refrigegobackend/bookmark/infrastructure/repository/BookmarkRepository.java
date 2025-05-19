package com.ohgiraffers.refrigegobackend.bookmark.infrastructure.repository;

import com.ohgiraffers.refrigegobackend.bookmark.domain.Bookmark;
import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByUserIdAndRecipeRcpSeq(Long userId, String rcpSeq);


    @Query("SELECT l.recipe.rcpSeq FROM Bookmark l WHERE l.userId = :userId")
    List<String> findRecipeIdsByUserId(@Param("userId") Long userId);

    void deleteByUserIdAndRecipe_RcpSeq(Long userId, String rcpSeq);
    List<Bookmark> findAllByUserId(Long userId);
}
