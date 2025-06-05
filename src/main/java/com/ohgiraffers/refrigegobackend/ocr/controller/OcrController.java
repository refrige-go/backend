package com.ohgiraffers.refrigegobackend.ocr.controller;

import com.ohgiraffers.refrigegobackend.ocr.service.OcrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ocr")
public class OcrController {
    private final OcrService ocrService;
    private static final Logger log = LoggerFactory.getLogger(OcrController.class);

    public OcrController(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    @PostMapping("/process")
    public ResponseEntity<String> processOcrImage(@RequestPart("image") MultipartFile image) throws IOException {
        log.info("OCR 이미지 업로드 요청 수신됨");
        String aiResult = ocrService.sendImageToAiServer(image);
        log.info("AI 서버 응답: {}", aiResult);
        return ResponseEntity.ok(aiResult);
    }

    // 새로운 엔드포인트 추가
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmIngredients(@RequestBody List<Map<String, Object>> ingredients) {
        log.info("재료 확인 요청 수신됨: {}", ingredients);
        try {
            ocrService.saveIngredients(ingredients);
            return ResponseEntity.ok(Map.of("result", "success"));
        } catch (Exception e) {
            log.error("재료 저장 중 오류 발생: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", "재료 저장 중 오류가 발생했습니다."));
        }
    }
}