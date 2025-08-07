package com.ohgiraffers.refrigegobackend.user.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminFileUploadService {

    private final AmazonS3 amazonS3;

    @Value("${aws.bucket-name}")
    private String bucketName;

    public String uploadIngredientImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        try {
            // 1. 파일명 안전하게 생성 (UUID + 확장자 유지)
            String originalFilename = imageFile.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String safeFilename = "images/" + UUID.randomUUID() + extension;

            // 2. 메타데이터 설정
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageFile.getSize());
            metadata.setContentType(imageFile.getContentType());

            // 3. MultipartFile을 InputStream으로 변환
            InputStream inputStream = imageFile.getInputStream();

            // 4. S3에 업로드
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,        // aws.s3.bucket 값 사용
                    safeFilename,      // S3 내 파일 경로+이름
                    inputStream,       // InputStream
                    metadata           // 메타데이터
            );

            amazonS3.putObject(putObjectRequest);

            // 5. 업로드된 이미지의 퍼블릭 URL 반환
            String imageUrl = amazonS3.getUrl(bucketName, safeFilename).toString();
            log.info("S3 이미지 업로드 완료 - 파일명: {}, URL: {}", safeFilename, imageUrl);
            return imageUrl;

        } catch (IOException e) {
            log.error("이미지 업로드 실패: {}", e.getMessage());
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }
}
