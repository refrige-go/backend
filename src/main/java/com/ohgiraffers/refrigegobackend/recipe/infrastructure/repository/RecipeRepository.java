package com.ohgiraffers.refrigegobackend.recipe.infrastructure.repository;

import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;
import com.ohgiraffers.refrigegobackend.recipe.dto.response.RecipeApiResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Recipe 엔티티에 대한 JPA Repository 인터페이스
 * JpaRepository를 상속받아 기본 CRUD 기능 제공
 *
 * @param Recipe - 관리할 엔티티 타입
 * @param String - 엔티티의 ID 타입 (여기선 String)
 */
public interface RecipeRepository extends JpaRepository<Recipe, String> {
    // JpaRepository가 기본 CRUD 메서드를 모두 제공하므로 별도의 메서드 선언 불필요

    // 요리 종류가 같은 레시피 조회 (찜한 레시피는 제외)
    List<Recipe> findByCuisineTypeInAndRcpSeqNotIn(List<String> cuisineTypes, List<String> excludedRcpSeqs);

    Page<Recipe> findByRcpCategory(String rcpCategory, Pageable pageable);

    Optional<Recipe> findByRcpSeq(String rcpSeq);
    
    /**
     * AI 서버 검색 결과의 이미지 정보 보강용
     * rcpSeq 목록으로 이미지 정보만 조회
     */
    @Query("SELECT r.rcpSeq, r.image, r.thumbnail FROM Recipe r WHERE r.rcpSeq IN :rcpSeqs")
    List<Object[]> findImagesByRcpSeqIn(@Param("rcpSeqs") List<String> rcpSeqs);
}