package com.ohgiraffers.refrigegobackend.ingredient.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * 유저가 냉장고에 등록한 재료 정보 엔티티
 * - 기준 재료를 기반으로 유저 냉장고에 넣은 데이터
 */
@Entity
@Table(name = "user_ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 고유 ID

    @Column(nullable = false)
    private String userId;  // 유저 ID

    @Column(nullable = true)
    private Long ingredientId;  // 기준 재료 ID

    @Column(nullable = true)
    private String customName;  // 유저가 직접 입력한 재료명

    @Column(nullable = false)
    private LocalDate purchaseDate;  // 구매일자

    @Column(nullable = false)
    private LocalDate expiryDate;    // 소비기한

    @Column(nullable = false)
    private boolean isFrozen;        // 냉동 보관 여부

    @Column(nullable = true)
    private String imageUrl; // 유저가 등록한 재료 이미지 URL

    private String customCategory;

    /**
     * 유통기한까지 남은 일 수 계산
     * @return 오늘부터 expiryDate까지 남은 일수 (음수면 지난 날짜)
     */
    public long getExpiryDaysLeft() {
        if (this.expiryDate == null) {
            return Long.MAX_VALUE;  // 또는 적절한 기본값, 예: -1 등
        }
        return java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), this.expiryDate);
    }

}