package com.ohgiraffers.refrigegobackend.bookmark.service;

import com.ohgiraffers.refrigegobackend.bookmark.domain.Bookmark;
import com.ohgiraffers.refrigegobackend.bookmark.dto.response.BookmarkRecipeResponseDTO;
import com.ohgiraffers.refrigegobackend.bookmark.dto.response.CuisineTypeRecipeResponseDTO;
import com.ohgiraffers.refrigegobackend.bookmark.dto.response.UserIngredientRecipeResponseDTO;
import com.ohgiraffers.refrigegobackend.bookmark.infrastructure.repository.BookmarkRepository;
import com.ohgiraffers.refrigegobackend.ingredient.domain.UserIngredient;
import com.ohgiraffers.refrigegobackend.ingredient.infrastructure.repository.UserIngredientRepository;
import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository.RecipeRepository;
import com.ohgiraffers.refrigegobackend.user.entity.User;
import com.ohgiraffers.refrigegobackend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookmarkService {

    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;
    private final RecipeRepository recipeRepository;
    private final UserIngredientRepository userIngredientRepository;

    @Autowired
    public BookmarkService(UserRepository userRepository, BookmarkRepository bookmarkRepository, RecipeRepository recipeRepository, UserIngredientRepository userIngredientRepository) {
        this.userRepository = userRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.recipeRepository = recipeRepository;
        this.userIngredientRepository = userIngredientRepository;
    }

    // 레시피 찜하기
    public boolean toggleBookmark(String username, String recipeId) {
        User user = userRepository.findByUsername(username);

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("레시피 없음"));

        try {
            Optional<Bookmark> existing = bookmarkRepository.findByUserIdAndRecipeRcpSeq(user.getId(), recipeId);

            if (existing.isPresent()) {
                bookmarkRepository.delete(existing.get());
                return false; // 찜 해제
            } else {
                Bookmark bookmark = new Bookmark();
                bookmark.setUser(user);
                bookmark.setRecipe(recipe);
                bookmark.setCreatedAt(LocalDateTime.now());
                bookmarkRepository.save(bookmark);
                return true; // 찜 추가
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 찜한 레시피 목록 조회
    public List<BookmarkRecipeResponseDTO> getBookmarkedRecipes(String username) {
        User user = userRepository.findByUsername(username);
        List<Recipe> recipes = bookmarkRepository.findRecipesByUserId(user.getId());

        List<BookmarkRecipeResponseDTO> result = recipes.stream()
                .map(BookmarkRecipeResponseDTO::new) // Recipe -> DTO
                .collect(Collectors.toList());       // 리스트로 변환
                
        return result;
    }


    // 찜한 레시피와 비슷한 레시피 목록 - 메인화면 (요리 종류 기준)
    public List<CuisineTypeRecipeResponseDTO> getRecommendedRecipesByBookmarked(String username) {
        User user = userRepository.findByUsername(username);

        // 1. 유저가 찜한 레시피 ID 목록 (Set으로 변환)
        List<String> bookmarkedRecipeIdsList = bookmarkRepository.findRecipeIdsByUserId(user.getId());
        if (bookmarkedRecipeIdsList.isEmpty()) return Collections.emptyList();

        Set<String> bookmarkedRecipeIds = new HashSet<>(bookmarkedRecipeIdsList);

        // 2. 찜한 레시피들의 요리 종류 가져오기
        List<Recipe> likedRecipes = recipeRepository.findAllById(bookmarkedRecipeIdsList);
        List<String> cuisineTypes = likedRecipes.stream()
                .map(Recipe::getCuisineType)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (cuisineTypes.isEmpty()) return Collections.emptyList();

        // 3. 찜하지 않은 같은 요리 종류 레시피 조회
        List<Recipe> recommendedRecipes = recipeRepository.findByCuisineTypeInAndRcpSeqNotIn(cuisineTypes, bookmarkedRecipeIdsList);

        // 4. DTO 변환 (추천 목록이니 bookmarked false or 포함 여부 체크)
        return recommendedRecipes.stream()
                .map(recipe -> new CuisineTypeRecipeResponseDTO(recipe, bookmarkedRecipeIds.contains(recipe.getRcpSeq())))
                .collect(Collectors.toList());
    }

    // 찜한 레시피 중 현재 만들 수 있는 레시피 목록 - 메인화면
    public List<UserIngredientRecipeResponseDTO> getRecommendedRecipesByUserIngredient(String username) {
        User user = userRepository.findByUsername(username);

        List<UserIngredient> userIngredients = userIngredientRepository.findByUserId(user.getId());

        // 사용자 재료명 추출 (customName과 표준 재료명 둘 다 고려)
        List<String> fridgeIngredientNames = userIngredients.stream()
                .map(userIngredient -> {
                    // customName이 있으면 customName, 없으면 표준 재료명
                    if (userIngredient.getCustomName() != null && !userIngredient.getCustomName().trim().isEmpty()) {
                        return userIngredient.getCustomName().trim();
                    } else if (userIngredient.getIngredient() != null) {
                        return userIngredient.getIngredient().getName().trim();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        System.out.println("🧊 사용자 냉장고 재료: " + fridgeIngredientNames);

        List<Bookmark> bookmarks = bookmarkRepository.findByUserId(user.getId());
        List<Recipe> likedRecipes = bookmarks.stream()
                .map(Bookmark::getRecipe)
                .toList();

        System.out.println("⭐ 찜한 레시피 수: " + likedRecipes.size());

        List<Recipe> matchedRecipes = likedRecipes.stream()
                .filter(recipe -> {
                    String parts = recipe.getRcpPartsDtls();
                    if (parts == null) return false;

                    List<String> recipeIngredients = Arrays.stream(parts.split("[●•\\n]"))
                            .flatMap(section -> Arrays.stream(section.split("[:,]")))
                            .map(s -> s.trim().split(" ")[0])
                            .map(s -> s.replaceAll("[^가-힣a-zA-Z]", "").trim())
                            .filter(s -> !s.isBlank())
                            .toList();

                    boolean hasMatch = recipeIngredients.stream().anyMatch(
                            ri -> fridgeIngredientNames.stream().anyMatch(fi -> 
                                ri.contains(fi) || fi.contains(ri) // 양방향 체크
                            )
                    );

                    if (hasMatch) {
                        System.out.println("✅ 매칭된 레시피: " + recipe.getRcpNm());
                    }

                    return hasMatch;
                })
                .collect(Collectors.toList());

        System.out.println("🍳 최종 매칭된 레시피 수: " + matchedRecipes.size());

        // 여기서 bookmarked=true를 명확히 전달
        return matchedRecipes.stream()
                .map(recipe -> new UserIngredientRecipeResponseDTO(recipe, true))
                .collect(Collectors.toList());
    }

}
