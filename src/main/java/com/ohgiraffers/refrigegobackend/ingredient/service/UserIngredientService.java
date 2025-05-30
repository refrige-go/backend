package com.ohgiraffers.refrigegobackend.ingredient.service;

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
    private final FileStorageProperties fileStorageProperties;

    // 이미지 저장 메서드
    private String saveImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) return null;
        try {
            String uploadDir = fileStorageProperties.getUploadDir();
            String originalFilename = imageFile.getOriginalFilename();

            // 한글, 공백, 특수문자 제거 (영어, 숫자, .만 허용)
            String cleanedName = originalFilename.replaceAll("[^a-zA-Z0-9\\.]", "");

            // 빈 문자열일 경우 기본 파일명 지정
            if (cleanedName.isEmpty()) {
                cleanedName = "file.jpeg";  // 확장자 포함 기본명
            }

            String safeFilename = UUID.randomUUID() + "_" + cleanedName;

            Path filePath = Paths.get(uploadDir, safeFilename);
            Files.createDirectories(filePath.getParent());
            imageFile.transferTo(filePath.toFile());
            return "/uploads/" + safeFilename;
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

        // 사용자의 재료인지 확인하고 삭제
        List<UserIngredient> ingredientsToConsume = repository.findByUserIdAndIdIn(user.getId(), ingredientIds);
        
        if (ingredientsToConsume.size() != ingredientIds.size()) {
            throw new RuntimeException("일부 재료를 찾을 수 없거나 권한이 없습니다.");
        }

        // 재료 소비 로그 (필요시)
        System.out.println("사용자 " + username + "가 레시피 " + recipeId + "로 재료 " + ingredientIds.size() + "개를 소비했습니다.");

        // 재료 삭제
        repository.deleteAll(ingredientsToConsume);
    }
}
