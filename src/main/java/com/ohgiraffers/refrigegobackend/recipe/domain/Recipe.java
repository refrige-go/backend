package com.ohgiraffers.refrigegobackend.recipe.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recipes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {

   @Id
   @Column(name = "rcp_seq")
   private String rcpSeq;

   @Column(name = "rcp_nm")
   private String rcpNm;

   @Lob
   @Column(name = "rcp_parts_dtls", columnDefinition = "TEXT")
   private String rcpPartsDtls;

   @Column(name = "cuisine_type", length = 100)
   private String cuisineType;

   @Column(name = "rcp_category", length = 100)
   private String rcpCategory;

   @Column(name = "rcp_way2", length = 100)
   private String rcpWay2;

   @Lob
   @Column(columnDefinition = "TEXT")
   private String image;

   @Lob
   @Column(columnDefinition = "TEXT")
   private String thumbnail;

   @Column(name = "hash_tag", length = 500)
   private String hashTag;

   @Column(name = "info_eng", length = 50)
   private String infoEng;

   @Column(name = "info_car", length = 50)
   private String infoCar;

   @Column(name = "info_pro", length = 50)
   private String infoPro;

   @Column(name = "info_fat", length = 50)
   private String infoFat;

   @Column(name = "info_na", length = 50)
   private String infoNa;

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
}
