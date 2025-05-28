package com.ohgiraffers.refrigegobackend.recipe.service;

import com.ohgiraffers.refrigegobackend.bookmark.domain.Bookmark;
import com.ohgiraffers.refrigegobackend.bookmark.infrastructure.repository.BookmarkRepository;
import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recipe.dto.response.RecipeApiResponseDto;
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

    public RecipeApiResponseDto.Recipe getRecipeById(String id) {
        Recipe recipe = recipeRepository.findByRcpSeq(id)
                .orElseThrow(() -> new RuntimeException("레시피를 찾을 수 없습니다."));

        return convertToResponseDto(recipe);
    }

    private RecipeApiResponseDto.Recipe convertToResponseDto(Recipe recipe) {
        RecipeApiResponseDto.Recipe responseDto = new RecipeApiResponseDto.Recipe();

        responseDto.setRcpSeq(recipe.getRcpSeq());
        responseDto.setRcpNm(recipe.getRcpNm());
        responseDto.setRcpPartsDtls(recipe.getRcpPartsDtls());
        responseDto.setCuisineType(recipe.getCuisineType());
        responseDto.setRcpWay2(recipe.getRcpWay2());
        responseDto.setAttFileNoMain(recipe.getImage());
        responseDto.setAttFileNoMk(recipe.getThumbnail());
        responseDto.setHashTag(recipe.getHashTag());

        // 조리 방법 설정
        responseDto.setManual01(recipe.getManual01());
        responseDto.setManual02(recipe.getManual02());
        responseDto.setManual03(recipe.getManual03());
        responseDto.setManual04(recipe.getManual04());
        responseDto.setManual05(recipe.getManual05());
        responseDto.setManual06(recipe.getManual06());

        // 영양 정보 설정
        responseDto.setInfoEng(recipe.getInfoEng());
        responseDto.setInfoCar(recipe.getInfoCar());
        responseDto.setInfoPro(recipe.getInfoPro());
        responseDto.setInfoFat(recipe.getInfoFat());
        responseDto.setInfoNa(recipe.getInfoNa());

        return responseDto;
    }
}
