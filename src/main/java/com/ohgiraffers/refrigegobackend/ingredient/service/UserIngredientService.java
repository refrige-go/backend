package com.ohgiraffers.refrigegobackend.ingredient.service;

import com.ohgiraffers.refrigegobackend.ingredient.domain.Ingredient;
import com.ohgiraffers.refrigegobackend.ingredient.domain.UserIngredient;
import com.ohgiraffers.refrigegobackend.ingredient.dto.UserIngredientBatchRequestDto;
import com.ohgiraffers.refrigegobackend.ingredient.dto.UserIngredientRequestDto;
import com.ohgiraffers.refrigegobackend.ingredient.dto.UserIngredientResponseDto;
import com.ohgiraffers.refrigegobackend.ingredient.dto.UserIngredientUpdateRequestDto;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.IngredientRepository;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.UserIngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 유저 보유 재료 관련 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
public class UserIngredientService {

    private final UserIngredientRepository repository;
    private final IngredientRepository ingredientRepository;
    private final UserIngredientRepository userIngredientRepository;

    /**
     * 보유 재료 등록
     * ingredientId 또는 customName 중 하나만 입력 가능
     */
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

    /**
     * 유저 보유 재료 전체 조회
     * 기준 재료가 없으면 customName 사용
     * 기준 재료 없으면 "(삭제된 기준 재료)" 표시
     */
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

    /**
     * 재료 삭제
     */
    public void deleteUserIngredient(Long id) {
        repository.deleteById(id);
    }

    /**
     * 보유 재료 일괄 등록
     */
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

    /**
     * 유저 보유 재료 정보 수정
     */
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

    /**
     * 보유 재료 상세 조회
     */
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

    /**
     * 냉동 여부 수정
     */
    @Transactional
    public void updateFrozenStatus(Long id, boolean isFrozen) {
        UserIngredient ingredient = userIngredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("재료를 찾을 수 없습니다."));
        ingredient.setFrozen(isFrozen);
        userIngredientRepository.save(ingredient);
    }

    /**
     * 구매일자 및 소비기한 수정
     */
    public void updateDates(Long id, LocalDate purchaseDate, LocalDate expiryDate) {
        UserIngredient ingredient = userIngredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("재료를 찾을 수 없습니다."));
        ingredient.setPurchaseDate(purchaseDate);
        ingredient.setExpiryDate(expiryDate);
        userIngredientRepository.save(ingredient);
    }

    /**
     * 간편 등록 (기본값 포함)
     */
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
