package com.ohgiraffers.refrigegobackend.ingredient.service;

import com.ohgiraffers.refrigegobackend.config.FileStorageProperties;
import com.ohgiraffers.refrigegobackend.ingredient.domain.Ingredient;
import com.ohgiraffers.refrigegobackend.ingredient.domain.IngredientCategory;
import com.ohgiraffers.refrigegobackend.ingredient.domain.UserIngredient;
import com.ohgiraffers.refrigegobackend.ingredient.dto.*;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.IngredientRepository;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.UserIngredientRepository;
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
    private final FileStorageProperties fileStorageProperties;

    // 이미지 저장
    private String saveImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) return null;
        try {
            String uploadDir = fileStorageProperties.getUploadDir();
            String originalFilename = imageFile.getOriginalFilename();
            String storedFilename = UUID.randomUUID() + "_" + originalFilename;
            Path filePath = Paths.get(uploadDir, storedFilename);
            Files.createDirectories(filePath.getParent());
            imageFile.transferTo(filePath.toFile());
            return "/uploads/" + storedFilename;
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }

    // 재료 추가
    public void addUserIngredient(UserIngredientRequestDto dto) {
        if ((dto.getIngredientId() == null && dto.getCustomName() == null) ||
                (dto.getIngredientId() != null && dto.getCustomName() != null)) {
            throw new IllegalArgumentException("ingredientId 또는 customName 중 하나만 입력해야 합니다.");
        }

        Ingredient ingredient = dto.getIngredientId() != null
                ? ingredientRepository.findById(dto.getIngredientId()).orElse(null)
                : null;

        UserIngredient userIngredient = UserIngredient.builder()
                .userId(dto.getUserId())
                .ingredient(ingredient)
                .customName(dto.getCustomName())
                .purchaseDate(dto.getPurchaseDate())
                .expiryDate(dto.getExpiryDate())
                .isFrozen(dto.isFrozen())
                .build();

        repository.save(userIngredient);
    }

    // 이미지 포함 재료 추가
    public void addUserIngredientWithImage(UserIngredientCreateDto dto) {
        String imageUrl = saveImage(dto.getImage());

        Ingredient ingredient = dto.getIngredientId() != null
                ? ingredientRepository.findById(dto.getIngredientId()).orElse(null)
                : null;

        UserIngredient userIngredient = UserIngredient.builder()
                .userId(dto.getUserId())
                .ingredient(ingredient)
                .customName(dto.getCustomName())
                .purchaseDate(dto.getPurchaseDate())
                .expiryDate(dto.getExpiryDate())
                .isFrozen(dto.isFrozen())
                .imageUrl(imageUrl)
                .build();

        repository.save(userIngredient);
    }

    // 유저 재료 전체 조회
    public List<UserIngredientResponseDto> getUserIngredients(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(ui -> {
                    String name = ui.getIngredientName();
                    String category = ui.getIngredient() != null
                            ? ui.getIngredient().getCategory().getDisplayName()
                            : "기타";
                    return new UserIngredientResponseDto(ui, name, category);
                }).collect(Collectors.toList());
    }

    // 단건 삭제
    public void deleteUserIngredient(Long id) {
        repository.deleteById(id);
    }

    // 다건 저장
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

    // 재료 수정
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

    // 단건 상세 조회
    public UserIngredientResponseDto getUserIngredientDetail(Long id) {
        UserIngredient entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재료입니다."));

        String name = entity.getIngredientName();
        String category = entity.getIngredient() != null
                ? entity.getIngredient().getCategory().getDisplayName()
                : "기타";

        return new UserIngredientResponseDto(entity, name, category);
    }

    // 냉동 여부만 수정
    @Transactional
    public void updateFrozenStatus(Long id, boolean isFrozen) {
        UserIngredient ingredient = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("재료를 찾을 수 없습니다."));
        ingredient.setFrozen(isFrozen);
        repository.save(ingredient);
    }

    // 날짜만 수정
    public void updateDates(Long id, LocalDate purchaseDate, LocalDate expiryDate) {
        UserIngredient ingredient = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("재료를 찾을 수 없습니다."));
        ingredient.setPurchaseDate(purchaseDate);
        ingredient.setExpiryDate(expiryDate);
        repository.save(ingredient);
    }

    // 기준 재료 여러 개 추가
    public void addIngredients(Long userId, List<UserIngredientBatchRequestDto.UserIngredientItem> items) {
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

    public void saveBatchWithUser(UserIngredientBatchRequestDto dto, Long userId) {
        // userId를 dto에 넣거나 서비스 로직에 맞게 처리
        dto.setUserId(userId); // 만약 dto에 userId 필드가 있다면
        saveBatch(dto);        // 기존 saveBatch 메서드 재활용
    }
}