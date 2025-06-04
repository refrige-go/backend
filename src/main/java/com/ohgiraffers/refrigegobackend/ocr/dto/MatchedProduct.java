package com.ohgiraffers.refrigegobackend.ocr.dto;

import lombok.Data;

@Data
public class MatchedProduct {
    private String originalName;
    private String matchedName;
    private int similarity;
    private String mainCategory;
    private String subCategory;
}
