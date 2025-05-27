package com.ohgiraffers.refrigegobackend.recipe.controller;

import com.ohgiraffers.refrigegobackend.recipe.dto.response.RecipeByCategoryDTO;
import com.ohgiraffers.refrigegobackend.recipe.service.RecipeService;
import com.ohgiraffers.refrigegobackend.user.dto.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/recipe")
public class RecipeController {

    private RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<RecipeByCategoryDTO>> findByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String username = null;
        if (userDetails != null) {
            username = userDetails.getUsername();
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<RecipeByCategoryDTO> recipes = recipeService.findByCategory(category, pageable, username);
        return ResponseEntity.ok(recipes);
    }

}
