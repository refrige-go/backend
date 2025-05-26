package com.ohgiraffers.refrigegobackend.recipe.service;

import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recipe.dto.response.RecipeByCategoryDTO;
import com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository.RecipeRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @Transactional
    public Page<RecipeByCategoryDTO> findByCategory(String category, Pageable pageable) {
        Page<Recipe> recipePage = recipeRepository.findByCategory(category, pageable);

        return recipePage.map(recipe ->
                new RecipeByCategoryDTO(recipe.getRcpNm(), recipe.getCategory(), recipe.getImage(), recipe.getRcpPartsDtls(), recipe.getCuisineType(), recipe.getRcpWay2())
        );
    }

}
