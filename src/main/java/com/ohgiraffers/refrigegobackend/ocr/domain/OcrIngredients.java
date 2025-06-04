package com.ohgiraffers.refrigegobackend.ocr.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data

public class OcrIngredients {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ocr_ingredient_name")
    private String name;

    @Column(name = "category")
    private String category;

}

