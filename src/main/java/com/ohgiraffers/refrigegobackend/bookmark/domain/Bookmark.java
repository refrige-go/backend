package com.ohgiraffers.refrigegobackend.bookmark.domain;

import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.user.domain.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recipe_bookmarks")
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long id;

    // 찜한 레시피
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", referencedColumnName = "rcpSeq")
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NO")
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Bookmark() {}

    public Bookmark(Long id, Recipe recipe, User user, LocalDateTime createdAt) {
        this.id = id;
        this.recipe = recipe;
        this.user = user;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
