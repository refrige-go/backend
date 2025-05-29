package com.ohgiraffers.refrigegobackend.ingredient.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 기준 재료 테이블 매핑용 엔티티
 * - 전체 공통 재료 목록 (예: 계란, 양파 등)
 */
@Entity
@Table(name = "ingredients")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient {

    // 기본 키 (자동 증가)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 재료명 (중복 불가, 길이 제한)
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    // 카테고리 (enum 사용)
    @Enumerated(EnumType.STRING) // DB에 문자열로 저장됨
    @Column(nullable = false, length = 50)
    private IngredientCategory category;

    // 기본 소비기한 (일 단위, 기본값 7일)
    @Column(name = "default_expiry_days")
    @Builder.Default
    private Integer defaultExpiryDays = 7;

    // 보관 방법 (예: 냉장, 냉동)
    @Column(name = "storage_method", length = 50)
    private String storageMethod;

    // 이미지 URL
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // 생성 일시
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 수정 일시
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 생성 시 자동 시간 설정
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // 수정 시 자동 시간 갱신
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
