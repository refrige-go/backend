package com.ohgiraffers.refrigegobackend.recipe.controller;

import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recipe.dto.response.RecipeByCategoryDTO;
import com.ohgiraffers.refrigegobackend.recipe.service.RecipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipe")
public class RecipeController {

    private RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<RecipeByCategoryDTO>> findByCategory(@PathVariable String category) {
        List<RecipeByCategoryDTO> recipes = recipeService.findByCategory(category);
        return ResponseEntity.ok(recipes);
    }
}
