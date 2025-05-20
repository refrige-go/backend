package com.ohgiraffers.refrigegobackend.recipe.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "recipes")
@Getter
@Setter
@NoArgsConstructor
public class Recipe {

   @Id
   private String rcpSeq; // 레시피 고유번호

   private String rcpNm;  // 레시피 이름

   @Lob
   @Column(name = "rcp_parts_dtls", columnDefinition = "TEXT")
   private String rcpPartsDtls; // 재료 상세 설명

   @Column(name = "cuisine_type")
   private String cuisineType; // 요리 종류 (밥, 반찬 등)

   @Column(name = "rcp_way2")
   private String rcpWay2;     // 조리 방법 상세 (찌기, 굽기 등)

   @Column(name = "image", columnDefinition = "TEXT")
   private String image;       // 메인 이미지 (ATT_FILE_NO_MAIN)

   @Column(name = "thumbnail", columnDefinition = "TEXT")
   private String thumbnail;   // 썸네일 이미지 (ATT_FILE_NO_MK)

   @Column(name = "hash_tag")
   private String hashTag;     // 해시태그

   @Lob
   @Column(name = "manual01", columnDefinition = "TEXT")
   private String manual01;

   @Lob
   @Column(name = "manual02", columnDefinition = "TEXT")
   private String manual02;

   @Lob
   @Column(name = "manual03", columnDefinition = "TEXT")
   private String manual03;

   @Lob
   @Column(name = "manual04", columnDefinition = "TEXT")
   private String manual04;

   @Lob
   @Column(name = "manual05", columnDefinition = "TEXT")
   private String manual05;

   @Lob
   @Column(name = "manual06", columnDefinition = "TEXT")
   private String manual06;

   @Lob
   @Column(name = "manual07", columnDefinition = "TEXT")
   private String manual07;

   @Lob
   @Column(name = "manual08", columnDefinition = "TEXT")
   private String manual08;

   @Lob
   @Column(name = "manual09", columnDefinition = "TEXT")
   private String manual09;

   @Lob
   @Column(name = "manual10", columnDefinition = "TEXT")
   private String manual10;

   @Lob
   @Column(name = "manual11", columnDefinition = "TEXT")
   private String manual11;

   @Lob
   @Column(name = "manual12", columnDefinition = "TEXT")
   private String manual12;

   @Lob
   @Column(name = "manual13", columnDefinition = "TEXT")
   private String manual13;

   @Lob
   @Column(name = "manual14", columnDefinition = "TEXT")
   private String manual14;

   @Lob
   @Column(name = "manual15", columnDefinition = "TEXT")
   private String manual15;

   @Lob
   @Column(name = "manual16", columnDefinition = "TEXT")
   private String manual16;

   @Lob
   @Column(name = "manual17", columnDefinition = "TEXT")
   private String manual17;

   @Lob
   @Column(name = "manual18", columnDefinition = "TEXT")
   private String manual18;

   @Lob
   @Column(name = "manual19", columnDefinition = "TEXT")
   private String manual19;

   @Lob
   @Column(name = "manual20", columnDefinition = "TEXT")
   private String manual20;

   @Column(name = "info_eng")
   private String infoEng;     // 열량

   @Column(name = "info_car")
   private String infoCar;     // 탄수화물

   @Column(name = "info_pro")
   private String infoPro;     // 단백질

   @Column(name = "info_fat")
   private String infoFat;     // 지방

   @Column(name = "info_na")
   private String infoNa;      // 나트륨
}
