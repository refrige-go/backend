package com.ohgiraffers.refrigegobackend.ingredient.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
public class UserIngredientCreateDto {
    private Long userId;
    private Long ingredientId;
    private String customName;
    private String customCategory;
    private LocalDate purchaseDate;
    private LocalDate expiryDate;
    private boolean isFrozen;

    private MultipartFile image; // 이미지 파일

    public void setIsFrozen(String frozen) {
        this.isFrozen = Boolean.parseBoolean(frozen);
    }
}
