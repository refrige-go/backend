package com.ohgiraffers.refrigegobackend.ingredient.service;

import com.ohgiraffers.refrigegobackend.config.FileStorageProperties;
import com.ohgiraffers.refrigegobackend.ingredient.domain.Ingredient;
import com.ohgiraffers.refrigegobackend.ingredient.domain.UserIngredient;
import com.ohgiraffers.refrigegobackend.ingredient.dto.*;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.IngredientRepository;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.UserIngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserIngredientService {

    private final UserIngredientRepository repository;
    private final IngredientRepository ingredientRepository;
    private final UserIngredientRepository userIngredientRepository;
    private final FileStorageProperties fileStorageProperties;

    // 이미지 저장 메서드
    private String saveImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) return null;

        try {
            String uploadDir = fileStorageProperties.getUploadDir();
            String originalFilename = imageFile.getOriginalFilename();
            String storedFilename = UUID.randomUUID() + "_" + originalFilename;

            Path filePath = Paths.get(uploadDir, storedFilename);
            Files.createDirectories(filePath.getParent());
            imageFile.transferTo(filePath.toFile());

            return "/uploads/" + storedFilename; // 프론트에서 접근 가능하게
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }

    public void addUserIngredient(UserIngredientRequestDto dto) {
        if ((dto.getIngredientId() == null && dto.getCustomName() == null) ||
                (dto.getIngredientId() != null && dto.getCustomName() != null)) {
            throw new IllegalArgumentException("ingredientId 또는 customName 중 하나만 입력해야 합니다.");
        }

        UserIngredient userIngredient = UserIngredient.builder()
                .userId(dto.getUserId())
                .ingredientId(dto.getIngredientId())
                .customName(dto.getCustomName())
                .purchaseDate(dto.getPurchaseDate())
                .expiryDate(dto.getExpiryDate())
                .isFrozen(dto.isFrozen())
                .build();

        repository.save(userIngredient);
    }

    public void addUserIngredientWithImage(UserIngredientCreateDto dto) {
        String imageUrl = saveImage(dto.getImage());

        UserIngredient ingredient = UserIngredient.builder()
                .userId(dto.getUserId())
                .ingredientId(dto.getIngredientId())
                .customName(dto.getCustomName())
                .customCategory(dto.getCustomCategory())
                .purchaseDate(dto.getPurchaseDate())
                .expiryDate(dto.getExpiryDate())
                .isFrozen(dto.isFrozen())
                .imageUrl(imageUrl)
                .build();

        repository.save(ingredient);
    }

    public List<UserIngredientResponseDto> getUserIngredients(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(ui -> {
                    String name;
                    String category;

                    if (ui.getIngredientId() != null) {
                        Ingredient ingredient = ingredientRepository.findById(ui.getIngredientId()).orElse(null);
                        if (ingredient != null) {
                            name = ingredient.getName();
                            category = ingredient.getCategory();
                        } else {
                            name = "(삭제된 기준 재료)";
                            category = "기타";
                        }
                    } else {
                        name = ui.getCustomName();
                        category = ui.getCustomCategory();
                    }

                    return new UserIngredientResponseDto(ui, name, category);
                })
                .collect(Collectors.toList());
    }

    public void deleteUserIngredient(Long id) {
        repository.deleteById(id);
    }

    public void saveBatch(UserIngredientBatchRequestDto batchDto) {
        Long userId = Long.valueOf(batchDto.getUserId());
        List<UserIngredient> entities = batchDto.getIngredients().stream()
                .map(item -> {
                    if ((item.getIngredientId() == null && (item.getCustomName() == null || item.getCustomName().isEmpty())) ||
                            (item.getIngredientId() != null && item.getCustomName() != null && !item.getCustomName().isEmpty())) {
                        throw new IllegalArgumentException("ingredientId 또는 customName 중 하나만 입력해야 합니다.");
                    }

                    return UserIngredient.builder()
                            .userId(userId)
                            .ingredientId(item.getIngredientId())
                            .customName(item.getCustomName())
                            .purchaseDate(item.getPurchaseDate())
                            .expiryDate(item.getExpiryDate())
                            .isFrozen(item.isFrozen())
                            .build();
                })
                .collect(Collectors.toList());

        repository.saveAll(entities);
    }

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

        String name;
        String category;

        if (entity.getIngredientId() != null) {
            Ingredient ingredient = ingredientRepository.findById(entity.getIngredientId())
                    .orElseThrow(() -> new IllegalArgumentException("기준 재료가 존재하지 않습니다."));
            name = ingredient.getName();
            category = ingredient.getCategory();
            entity.setCustomName(null);
        } else {
            name = entity.getCustomName();
            category = entity.getCustomCategory() != null ? entity.getCustomCategory() : "기타";
        }

        return new UserIngredientResponseDto(entity, name, category);
    }

    @Transactional
    public void updateFrozenStatus(Long id, boolean isFrozen) {
        UserIngredient ingredient = userIngredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("재료를 찾을 수 없습니다."));
        ingredient.setFrozen(isFrozen);
        userIngredientRepository.save(ingredient);
    }

    public void updateDates(Long id, LocalDate purchaseDate, LocalDate expiryDate) {
        UserIngredient ingredient = userIngredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("재료를 찾을 수 없습니다."));
        ingredient.setPurchaseDate(purchaseDate);
        ingredient.setExpiryDate(expiryDate);
        userIngredientRepository.save(ingredient);
    }

    public void addIngredients(Long userId, List<Long> ingredientIds) {
        List<UserIngredient> entities = ingredientIds.stream()
                .map(id -> UserIngredient.builder()
                        .userId(userId)
                        .ingredientId(id)
                        .purchaseDate(LocalDate.now())
                        .expiryDate(LocalDate.now().plusDays(7))
                        .isFrozen(false)
                        .build())
                .toList();

        userIngredientRepository.saveAll(entities);
    }
}
