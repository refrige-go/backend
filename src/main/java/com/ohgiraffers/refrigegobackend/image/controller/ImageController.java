//package com.ohgiraffers.refrigegobackend.image.controller;
//
//import com.ohgiraffers.refrigegobackend.image.service.ImageService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//@RestController
//@RequestMapping("/api/images")
//@RequiredArgsConstructor
//public class ImageController {
//
//    private final ImageService imageService;
//
//    @PostMapping("/upload")
//    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
//        try {
//            String savedFileName = imageService.saveImage(file);
//            String imageUrl = "/images/" + savedFileName;
//            return ResponseEntity.ok(imageUrl);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("이미지 업로드 실패: " + e.getMessage());
//        }
//    }
//}
