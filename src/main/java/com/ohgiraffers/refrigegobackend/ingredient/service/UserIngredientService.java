package com.ohgiraffers.refrigegobackend.ingredient.service;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.ohgiraffers.refrigegobackend.config.FileStorageProperties;
import com.ohgiraffers.refrigegobackend.ingredient.domain.Ingredient;
import com.ohgiraffers.refrigegobackend.ingredient.domain.IngredientCategory;
import com.ohgiraffers.refrigegobackend.ingredient.domain.UserIngredient;
import com.ohgiraffers.refrigegobackend.ingredient.dto.*;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.IngredientRepository;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.UserIngredientRepository;
import com.ohgiraffers.refrigegobackend.user.entity.User;
import com.ohgiraffers.refrigegobackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserIngredientService {

    private final UserIngredientRepository repository;
    private final IngredientRepository ingredientRepository;
    private final UserRepository userRepository; // UserRepository 추가

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private String saveImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) return null;

        try {
            String originalFilename = imageFile.getOriginalFilename();
            String safeFilename = UUID.randomUUID() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9\\.]", "");

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageFile.getSize());
            metadata.setContentType(imageFile.getContentType());

            // S3에 업로드 (PublicRead 권한 추가)
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, safeFilename, imageFile.getInputStream(), metadata);

            amazonS3.putObject(putObjectRequest);

            // S3에서 접근 가능한 URL 반환
            return amazonS3.getUrl(bucketName, safeFilename).toString();

        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }

    // username 기준 재료 추가
    public void addUserIngredient(String username, UserIngredientRequestDto dto) {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new RuntimeException("사용자를 찾을 수 없습니다.");

        if ((dto.getIngredientId() == null && dto.getCustomName() == null) ||
                (dto.getIngredientId() != null && dto.getCustomName() != null)) {
            throw new IllegalArgumentException("ingredientId 또는 customName 중 하나만 입력해야 합니다.");
        }

        Ingredient ingredient = dto.getIngredientId() != null
                ? ingredientRepository.findById(dto.getIngredientId()).orElse(null)
                : null;

        UserIngredient userIngredient = UserIngredient.builder()
                .userId(user.getId())
                .ingredient(ingredient)
                .customName(dto.getCustomName())
                .purchaseDate(dto.getPurchaseDate())
                .expiryDate(dto.getExpiryDate())
                .isFrozen(dto.isFrozen())
                .build();

        repository.save(userIngredient);
    }

    // username 기준 재료 조회
    public List<UserIngredientResponseDto> getUserIngredientsByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new RuntimeException("사용자를 찾을 수 없습니다.");

        return repository.findByUserId(user.getId()).stream()
                .map(ui -> {
                    String name = ui.getIngredientName();
                    String category = ui.getIngredient() != null
                            ? ui.getIngredient().getCategory().getDisplayName()
                            : "기타";
                    return new UserIngredientResponseDto(ui, name, category);
                }).collect(Collectors.toList());
    }

    // username 기준 다건 저장
    public void saveBatchWithUsername(UserIngredientBatchRequestDto dto, String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new RuntimeException("사용자를 찾을 수 없습니다.");

        dto.setUserId(user.getId());
        saveBatch(dto);
    }

    // 기준 재료 여러 개 추가 (username 기준)
    public void addIngredientsByUsername(String username, List<UserIngredientBatchRequestDto.UserIngredientItem> items) {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new RuntimeException("사용자를 찾을 수 없습니다.");

        List<UserIngredient> entities = items.stream()
                .map(item -> {
                    if ((item.getIngredientId() == null && item.getCustomName() == null) ||
                            (item.getIngredientId() != null && item.getCustomName() != null)) {
                        throw new IllegalArgumentException("ingredientId 또는 customName 중 하나만 입력해야 합니다.");
                    }

                    Ingredient ingredient = item.getIngredientId() != null
                            ? ingredientRepository.findById(item.getIngredientId()).orElse(null)
                            : null;

                    return UserIngredient.builder()
                            .userId(user.getId())
                            .ingredient(ingredient)
                            .customName(item.getCustomName())
                            .purchaseDate(item.getPurchaseDate())
                            .expiryDate(item.getExpiryDate())
                            .isFrozen(item.isFrozen())
                            .build();
                }).collect(Collectors.toList());

        repository.saveAll(entities);
    }

    // 이미지 포함 재료 추가 (username 기준)
    public void addUserIngredientWithImage(String username, UserIngredientCreateDto dto) {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new RuntimeException("사용자를 찾을 수 없습니다.");

        String imageUrl = saveImage(dto.getImage());

        // 기존 재료 존재 여부 확인 (이름 기준으로)
        Ingredient ingredient = null;
        if (dto.getIngredientId() != null) {
            ingredient = ingredientRepository.findById(dto.getIngredientId()).orElse(null);
        } else if (dto.getCustomCategory() != null && dto.getCustomName() != null) {
            ingredient = ingredientRepository.findByName(dto.getCustomName()).orElse(null);
            if (ingredient == null) {
                ingredient = Ingredient.builder()
                        .name(dto.getCustomName())
                        .category(IngredientCategory.fromDisplayName(dto.getCustomCategory()))
                        .build();
                ingredient = ingredientRepository.save(ingredient);
            }
        }

        UserIngredient userIngredient = UserIngredient.builder()
                .userId(user.getId())
                .ingredient(ingredient)
                .customName(dto.getCustomName())
                .purchaseDate(dto.getPurchaseDate())
                .expiryDate(dto.getExpiryDate())
                .isFrozen(dto.isFrozen())
                .imageUrl(imageUrl)
                .build();

        repository.save(userIngredient);
    }

    // 기존 saveBatch 유지
    public void saveBatch(UserIngredientBatchRequestDto batchDto) {
        Long userId = Long.valueOf(batchDto.getUserId());
        List<UserIngredient> entities = batchDto.getIngredients().stream().map(item -> {
            if ((item.getIngredientId() == null && item.getCustomName() == null) ||
                    (item.getIngredientId() != null && item.getCustomName() != null)) {
                throw new IllegalArgumentException("ingredientId 또는 customName 중 하나만 입력해야 합니다.");
            }

            Ingredient ingredient = item.getIngredientId() != null
                    ? ingredientRepository.findById(item.getIngredientId()).orElse(null)
                    : null;

            return UserIngredient.builder()
                    .userId(userId)
                    .ingredient(ingredient)
                    .customName(item.getCustomName())
                    .purchaseDate(item.getPurchaseDate())
                    .expiryDate(item.getExpiryDate())
                    .isFrozen(item.isFrozen())
                    .build();
        }).collect(Collectors.toList());

        repository.saveAll(entities);
    }

    // 이하 기존 메서드 유지
    public void updateUserIngredient(Long id, UserIngredientUpdateRequestDto dto) {
        UserIngredient entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재료입니다."));

        entity.setPurchaseDate(dto.getPurchaseDate());
        entity.setExpiryDate(dto.getExpiryDate());
        entity.setFrozen(dto.isFrozen());

        if (dto.getCustomName() != null) {
            entity.setCustomName(dto.getCustomName());
        }
        if (dto.getImageUrl() != null) {
            entity.setImageUrl(dto.getImageUrl());
        }

        repository.save(entity);
    }

    public UserIngredientResponseDto getUserIngredientDetail(Long id) {
        UserIngredient entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재료입니다."));

        String name = entity.getIngredientName();
        String category = entity.getIngredient() != null
                ? entity.getIngredient().getCategory().getDisplayName()
                : "기타";

        return new UserIngredientResponseDto(entity, name, category);
    }

    @Transactional
    public void updateFrozenStatus(Long id, boolean isFrozen) {
        UserIngredient ingredient = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("재료를 찾을 수 없습니다."));
        ingredient.setFrozen(isFrozen);
        repository.save(ingredient);
    }

    public void updateDates(Long id, LocalDate purchaseDate, LocalDate expiryDate) {
        UserIngredient ingredient = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("재료를 찾을 수 없습니다."));
        ingredient.setPurchaseDate(purchaseDate);
        ingredient.setExpiryDate(expiryDate);
        repository.save(ingredient);
    }

    public void deleteUserIngredient(Long id) {
        repository.deleteById(id);
    }

    /**
     * 요리 완료 시 재료 소비 처리
     * 해당 재료들을 냉장고에서 삭제
     */
    @Transactional
    public void consumeIngredients(String username, List<Long> ingredientIds, String recipeId) {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new RuntimeException("사용자를 찾을 수 없습니다.");

        List<UserIngredient> ingredientsToConsume = repository.findByUserIdAndIdIn(user.getId(), ingredientIds);

        if (ingredientsToConsume.size() != ingredientIds.size()) {
            throw new RuntimeException("일부 재료를 찾을 수 없거나 권한이 없습니다.");
        }

        // 유통기한 기준 오름차순 정렬 (null은 가장 뒤로)
        ingredientsToConsume.sort(Comparator.comparing(
                ui -> Optional.ofNullable(ui.getExpiryDate()).orElse(LocalDate.MAX)
        ));

        System.out.println("사용자 " + username + "가 레시피 " + recipeId + "로 재료 " + ingredientIds.size() + "개를 소비했습니다.");

        repository.deleteAll(ingredientsToConsume);
    }
}
