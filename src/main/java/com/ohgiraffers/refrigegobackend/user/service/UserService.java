package com.ohgiraffers.refrigegobackend.user.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ohgiraffers.refrigegobackend.user.entity.User;
import com.ohgiraffers.refrigegobackend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final AmazonS3 amazonS3;
    @Value("${aws.bucket-name}")
    private String bucketName;

    // 생성자 주입
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,AmazonS3 amazonS3) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.amazonS3 = amazonS3;
    }

    @Transactional
    public void updateUserInfo(String username, String newNickname, String newPassword) {
        User user = userRepository.findByUsernameAndDeletedFalse(username);

        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        // 닉네임, 비밀번호 수정
        user.setNickname(newNickname);
        user.setPassword(passwordEncoder.encode(newPassword));

    }

    @Transactional
    public void withdrawUser(String username) {
        User user = userRepository.findByUsernameAndDeletedFalse(username);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        user.setDeleted(true);
    }  // userRepository.save(user); // JPA 트랜잭션이면 save 생략 가능

    // 사용자 이미지 저장
    public String updateProfileImage(String username, MultipartFile profileImage) {
        User user = userRepository.findByUsernameAndDeletedFalse(username);
        if (user == null) throw new RuntimeException("사용자를 찾을 수 없습니다.");

        String imageUrl = saveImage(profileImage);
        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);

        return imageUrl;
    }

    private String saveImage(MultipartFile imageFile) {
        // 0. 이미지 파일이 비어 있으면 null 반환
        if (imageFile == null || imageFile.isEmpty()) return null;

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

            // 3. S3에 업로드 (퍼블릭 읽기 권한 설정)
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,        // 버킷 이름
                    safeFilename,      // S3 내 파일 경로+이름
                    imageFile.getInputStream(), // 업로드 파일 데이터
                    metadata           // 메타데이터
            );

            amazonS3.putObject(putObjectRequest);

            // 4. 업로드된 이미지의 퍼블릭 URL 반환
            return amazonS3.getUrl(bucketName, safeFilename).toString();

        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }
}
