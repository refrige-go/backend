package com.ohgiraffers.refrigegobackend.bookmark.domain;

import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recipe_bookmarks")
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long id;

//    // 레시피 찜한 사람
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;

    // 찜한 레시피
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", referencedColumnName = "rcpSeq")
    private Recipe recipe;

    // 임시
    private Long userId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Bookmark() {}

    public Bookmark(Long id, Recipe recipe, Long userId, LocalDateTime createdAt) {
        this.id = id;
        this.recipe = recipe;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public Bookmark(Long id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Bookmark{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                '}';
    }
}
