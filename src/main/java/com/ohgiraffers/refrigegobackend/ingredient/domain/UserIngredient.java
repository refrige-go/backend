package com.ohgiraffers.refrigegobackend.ingredient.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 유저 냉장고 속 재료 엔티티
 * - 기준 재료를 @ManyToOne으로 참조하거나 직접 입력한 이름(customName)을 저장
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

    @Column(name = "user_id", nullable = false)
    private Long userId; // 유저 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient; // 기준 재료 엔티티

    @Column(name = "custom_name")
    private String customName;  // 직접 입력한 재료명

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;  // 구매일자

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;    // 소비기한

    @Column(name = "is_frozen", nullable = false)
    @Builder.Default
    private Boolean isFrozen = false;  // 냉동 여부

    @Column(name = "image_url", length = 500)
    private String imageUrl; // 이미지 URL

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 기준 재료명 또는 커스텀명 반환
    public String getIngredientName() {
        return ingredient != null ? ingredient.getName() : customName;
    }

    // 기준 재료 ID 반환
    public Long getIngredientId() {
        return ingredient != null ? ingredient.getId() : null;
    }

    // 소비기한까지 남은 일수 계산
    public long getExpiryDaysLeft() {
        if (this.expiryDate == null) {
            return Long.MAX_VALUE;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), this.expiryDate);
    }

    // 유통기한 임박 여부
    public boolean isExpiringSoon() {
        return getExpiryDaysLeft() <= 3 && getExpiryDaysLeft() >= 0;
    }

    // 유통기한 초과 여부
    public boolean isExpired() {
        return getExpiryDaysLeft() < 0;
    }

    public void setFrozen(Boolean isFrozen) {
        this.isFrozen = isFrozen;
    }
}