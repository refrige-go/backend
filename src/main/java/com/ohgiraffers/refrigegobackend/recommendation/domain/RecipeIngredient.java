package com.ohgiraffers.refrigegobackend.recommendation.domain;

import com.ohgiraffers.refrigegobackend.ingredient.domain.Ingredient;
import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 레시피-식재료 연결 테이블 (링크 테이블)
 * - 레시피와 식재료의 다대다 관계를 연결
 * - 추천 시스템에서 사용자 재료와 레시피 매칭에 활용
 */
@Entity
@Table(name = "recipe_ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 레시피와의 연관관계 (다대일)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    // 식재료와의 연관관계 (다대일)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    // 주재료 여부 (추천 알고리즘에서 가중치 적용 가능)
    @Column(name = "is_main_ingredient")
    @Builder.Default
    private Boolean isMainIngredient = false;

    // 생성 일시
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 생성 시 자동 시간 설정
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ✅ 기존 코드 호환성을 위한 편의 메서드
    public String getRecipeId() {
        return recipe != null ? recipe.getRcpSeq() : null;
    }

    public Long getIngredientId() {
        return ingredient != null ? ingredient.getId() : null;
    }

    // ✅ 객체 참조 활용 메서드들 (추천 시스템에서 유용)
    public String getRecipeName() {
        return recipe != null ? recipe.getRcpNm() : null;
    }

    public String getIngredientName() {
        return ingredient != null ? ingredient.getName() : null;
    }

    public String getIngredientCategory() {
        return ingredient != null ? ingredient.getCategory().getDisplayName() : null;
    }
}
