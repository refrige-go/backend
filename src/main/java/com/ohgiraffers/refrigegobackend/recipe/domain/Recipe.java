package com.ohgiraffers.refrigegobackend.recipe.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Recipe 엔티티 클래스
 *
 * - 데이터베이스 recipes 테이블과 매핑
 * - 레시피 정보를 저장하는 도메인 객체
 */
@Entity
@Table(name = "recipes")
@Getter
@Setter
@NoArgsConstructor
public class Recipe {

    @Id
    private String rcpSeq;          // 레시피 고유번호

    private String rcpNm;           // 레시피 이름

    @Lob  // 이 어노테이션으로 길이 큰 텍스트 저장 가능
    @Column(name = "rcp_parts_dtls")
    private String rcpPartsDtls;    // 재료 상세

    @Lob
    @Column(name = "manual01")
    private String manual01;        // 조리 방법 1

    @Lob
    @Column(name = "manual02")
    private String manual02;        // 조리 방법 2
}
