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

   @Lob
   @Column(name = "rcp_parts_dtls", columnDefinition = "TEXT")
   private String rcpPartsDtls;    // 재료 상세

   @Lob
   @Column(name = "manual01", columnDefinition = "TEXT")
   private String manual01;        // 조리 방법 1

   @Lob
   @Column(name = "manual02", columnDefinition = "TEXT")
   private String manual02;        // 조리 방법 2

   @Column(name = "cuisine_type", columnDefinition = "TEXT")
   private String cuisineType;    // 요리 종류 (예: 밥, 반찬 등)

   @Column(name = "rcp_way2", columnDefinition = "TEXT")
   private String rcpWay2;         // 조리 방법 상세 (예: 찌기, 굽기 등)

   @Column(name = "rcp_image", columnDefinition = "TEXT")
   private String image;         // 레시피 이미지 URL
}