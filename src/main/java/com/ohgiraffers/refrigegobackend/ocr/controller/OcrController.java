package com.ohgiraffers.refrigegobackend.ocr.controller;

import com.ohgiraffers.refrigegobackend.ocr.service.OcrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(OcrController.class);


    @PostMapping("/process")
    public ResponseEntity<String> processOcrImage(@RequestPart("image") MultipartFile image) throws IOException {
        log.info("OCR 이미지 업로드 요청 수신됨"); // 요청 진입 로그

        // Service에서 AI 서버로 이미지 전송 및 결과 받아오기
        String aiResult = ocrService.sendImageToAiServer(image);

        log.info("AI 서버 응답: {}", aiResult); // AI 서버 응답 로그

        return ResponseEntity.ok(aiResult);
    }
}