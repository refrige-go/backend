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
     * 기준 재료 없으면 "(삭제된 기준 재료)" 표시하도록 수정 권장
     */
    public List<UserIngredientResponseDto> getUserIngredients(String userId) {
        return repository.findByUserId(userId).stream()
                .map(ui -> {
                    String name = (ui.getIngredientId() != null)
                            ? ingredientRepository.findById(ui.getIngredientId())
                            .map(Ingredient::getName)
                            .orElse("(삭제된 기준 재료)")
                            : ui.getCustomName();

                    // expiryDate null 체크 후 DTO 생성
                    return new UserIngredientResponseDto(ui, name);
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
        String userId = batchDto.getUserId();
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
     * 수정 가능한 필드 : purchaseDate, expiryDate, isFrozen, customName, imageUrl
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
     * 기준 재료가 있을 경우 해당 이름 반환, 없으면 customName 사용
     * 기준 재료면 customName은 null 처리 (수정 금지)
     */
    public UserIngredientResponseDto getUserIngredientDetail(Long id) {
        UserIngredient entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재료입니다."));
        String name;
        if (entity.getIngredientId() != null) {
            name = ingredientRepository.findById(entity.getIngredientId())
                    .map(ingredient -> ingredient.getName())
                    .orElseThrow(() -> new IllegalArgumentException("기준 재료가 존재하지 않습니다."));
        } else {
            name = entity.getCustomName();
        }
        if (entity.getIngredientId() != null) {
            entity.setCustomName(null);
        }
        return new UserIngredientResponseDto(entity, name);
    }

}
