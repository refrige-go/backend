package com.ohgiraffers.refrigegobackend.recipe.service;

import com.ohgiraffers.refrigegobackend.bookmark.domain.Bookmark;
import com.ohgiraffers.refrigegobackend.bookmark.infrastructure.repository.BookmarkRepository;
import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recipe.dto.response.RecipeByCategoryDTO;
import com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository.RecipeRepository;
import com.ohgiraffers.refrigegobackend.user.entity.User;
import com.ohgiraffers.refrigegobackend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;

    public RecipeService(RecipeRepository recipeRepository, UserRepository userRepository, BookmarkRepository bookmarkRepository) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    @Transactional
    public Page<RecipeByCategoryDTO> findByCategory(String category, Pageable pageable, String username) {
        Set<String> bookmarkedRecipeIds;

        if (username != null) {
            User user = userRepository.findByUsername(username);
            List<Bookmark> bookmarks = bookmarkRepository.findAllByUserId(user.getId());
            bookmarkedRecipeIds = bookmarks.stream()
                    .map(bookmark -> bookmark.getRecipe().getRcpSeq())
                    .collect(Collectors.toSet());
        } else {
            bookmarkedRecipeIds = Collections.emptySet();
        }

        Page<Recipe> recipePage = recipeRepository.findByCategory(category, pageable);

        return recipePage.map(recipe -> {
            boolean isBookmarked = bookmarkedRecipeIds.contains(recipe.getRcpSeq());

            return new RecipeByCategoryDTO(
                    recipe.getRcpNm(),
                    recipe.getRcpSeq(),
                    recipe.getCategory(),
                    recipe.getImage(),
                    recipe.getRcpPartsDtls(),
                    recipe.getCuisineType(),
                    recipe.getRcpWay2(),
                    isBookmarked
            );
        });
    }

}
