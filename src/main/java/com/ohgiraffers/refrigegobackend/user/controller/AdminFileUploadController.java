package com.ohgiraffers.refrigegobackend.user.controller;

import com.ohgiraffers.refrigegobackend.user.service.AdminFileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("admin")
@RequiredArgsConstructor
@Slf4j
public class AdminFileUploadController {

    private final AdminFileUploadService adminFileUploadService;

    @PostMapping("/upload/image")
    public ResponseEntity<Map<String, String>> uploadImage (@RequestParam("file")MultipartFile file){
        Map<String, String> response = new HashMap<>();

        try{
            if(file.isEmpty()) {
                response.put("error", "파일이 비어있습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            String imageUrl = adminFileUploadService.uploadIngredientImage(file);
            response.put("imageUrl", imageUrl);

            return ResponseEntity.ok(response);
        } catch (Exception e){
            log.error("이미지 업로드 실패: {}", e.getMessage());
            response.put("error", "이미지 업로드에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
