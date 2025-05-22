package com.ohgiraffers.refrigegobackend.ocr.dto;

import lombok.Data;

import java.util.List;

@Data
public class OcrResult {
    private String rawText;
    private List<String> extractedProducts;
    private int totalProducts;
}

