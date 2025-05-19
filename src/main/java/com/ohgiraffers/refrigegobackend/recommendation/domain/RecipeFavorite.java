package com.ohgiraffers.refrigegobackend.recommendation.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사용자 레시피 찜하기 엔티티
 * - 사용자가 찜한 레시피 정보를 저장
 */
@Entity
@Table(name = "recipe_favorites")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 ID
     */
    @Column(nullable = false)
    private String userId;

    /**
     * 레시피 고유번호 (Recipe 엔티티와 연결)
     */
    @Column(nullable = false)
    private String recipeId;

    /**
     * 찜한 날짜
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
