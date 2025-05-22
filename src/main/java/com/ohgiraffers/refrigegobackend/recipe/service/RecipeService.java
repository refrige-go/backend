package com.ohgiraffers.refrigegobackend.recipe.service;

import com.ohgiraffers.refrigegobackend.bookmark.dto.response.BookmarkRecipeResponseDTO;
import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recipe.dto.response.RecipeByCategoryDTO;
import com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository.RecipeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @Transactional
    public List<RecipeByCategoryDTO> findByCategory(String category) {
        return recipeRepository.findByCategory(category)
                .stream()
                .map(recipe -> new RecipeByCategoryDTO(recipe.getRcpNm(), recipe.getCategory(), recipe.getImage()))
                .collect(Collectors.toList());
    }
}
