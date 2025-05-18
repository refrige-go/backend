package com.ohgiraffers.refrigegobackend.ingredient.service;

import com.ohgiraffers.refrigegobackend.ingredient.domain.UserIngredient;
import com.ohgiraffers.refrigegobackend.ingredient.dto.UserIngredientRequestDto;
import com.ohgiraffers.refrigegobackend.ingredient.dto.UserIngredientResponseDto;
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
     */
    public void addUserIngredient(UserIngredientRequestDto dto) {

        // ingredientId 또는 customName 중 하나만 있어야 함
        if ((dto.getIngredientId() == null && dto.getCustomName() == null) ||
                (dto.getIngredientId() != null && dto.getCustomName() != null)) {
            throw new IllegalArgumentException("ingredientId 또는 customName 중 하나만 입력해야 합니다.");
        }

        UserIngredient userIngredient = UserIngredient.builder()
                .userId(dto.getUserId())
                .ingredientId(dto.getIngredientId()) // 기준 재료일 경우만 값 존재
                .customName(dto.getCustomName())     // 직접 입력일 경우만 값 존재
                .purchaseDate(dto.getPurchaseDate())
                .expiryDate(dto.getExpiryDate())
                .isFrozen(dto.isFrozen())
                .build();

        repository.save(userIngredient);
    }

    /**
     * 유저의 보유 재료 전체 조회
     */
    public List<UserIngredientResponseDto> getUserIngredients(String userId) {
        return repository.findByUserId(userId).stream()
                .map(ui -> {
                    String name = (ui.getIngredientId() != null)
                            ? ingredientRepository.findById(ui.getIngredientId()).orElseThrow().getName()
                            : ui.getCustomName();
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
}