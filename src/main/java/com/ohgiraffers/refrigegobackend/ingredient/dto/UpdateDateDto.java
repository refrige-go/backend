package com.ohgiraffers.refrigegobackend.ingredient.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateDateDto {
    private LocalDate purchaseDate;
    private LocalDate expiryDate;
}
