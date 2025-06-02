package com.ohgiraffers.refrigegobackend.ocr.controller;

import com.ohgiraffers.refrigegobackend.ocr.service.OcrService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/ocr")
public class OcrController {

    private final OcrService ocrService;

    public OcrController(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    @PostMapping("/process")
    public ResponseEntity<String> processOcrImage(@RequestPart("image") MultipartFile image) throws IOException {
        // Service에서 AI 서버로 이미지 전송 및 결과 받아오기
        String aiResult = ocrService.sendImageToAiServer(image);
        return ResponseEntity.ok(aiResult);
    }
}