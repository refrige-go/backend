package com.ohgiraffers.refrigegobackend.recipe.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private String rcpSeq;          // 레시피 고유번호 (PK 역할)

    private String rcpNm;           // 레시피 이름
    private String rcpPartsDtls;    // 재료 상세 설명
    private String manual01;        // 조리 방법 1
    private String manual02;        // 조리 방법 2

    // 필요한 경우 생성자, equals, hashCode, toString 추가 가능
}
