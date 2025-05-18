package com.ohgiraffers.refrigegobackend.ingredients.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * 기준 재료(ingredients) 테이블 매핑용 엔티티 클래스
 * - 이 클래스는 전체 공통 재료 목록을 저장한다 (예: 계란, 브로콜리 등)
 * - 사용자 보유 재료가 아님 (user_ingredients 아님!)
 */
@Entity
@Table(name = "ingredients")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient {

    /**
     * 재료 ID (기본 키, 자동 증가)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 재료명 (예: 계란, 양파, 고기 등)
     * - null 불가
     */
    @Column(nullable = false)
    private String name;

    /**
     * 재료 카테고리 (예: 채소, 고기, 유제품 등)
     * - null 불가
     */
    @Column(nullable = false)
    private String category;
}