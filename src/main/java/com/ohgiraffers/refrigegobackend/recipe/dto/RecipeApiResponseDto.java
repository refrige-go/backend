package com.ohgiraffers.refrigegobackend.recipe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 외부 API JSON 응답을 매핑하는 DTO 클래스
 *
 * @JsonProperty: JSON 필드명과 자바 필드명을 매핑
 * @JsonIgnoreProperties(ignoreUnknown = true): DTO에 없는 필드는 무시하고 매핑 오류 방지
 */
@Getter
@Setter
@NoArgsConstructor
public class RecipeApiResponseDto {

    @JsonProperty("COOKRCP01")  // JSON의 COOKRCP01 필드와 매핑
    private CookRcp COOKRCP01;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true) // 알 수 없는 필드 무시
    public static class CookRcp {
        private int total_count;  // 총 레시피 개수
        private Recipe[] row;     // 레시피 배열
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true) // 알 수 없는 필드 무시
    public static class Recipe {

        @JsonProperty("RCP_SEQ")
        private String rcpSeq;          // 레시피 고유번호

        @JsonProperty("RCP_NM")
        private String rcpNm;           // 레시피 이름

        @JsonProperty("RCP_PARTS_DTLS")
        private String rcpPartsDtls;    // 재료 상세 설명

        @JsonProperty("MANUAL01")
        private String manual01;        // 조리 방법 1

        @JsonProperty("MANUAL02")
        private String manual02;        // 조리 방법 2

        @JsonProperty("RCP_WAY2")
        private String rcpWay2;         // 조리 방법 상세 (예: 찌기, 굽기 등)

        // 필요한 필드만 선언, 나머지는 무시됨
    }
}
