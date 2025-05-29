package com.ohgiraffers.refrigegobackend.recipe.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 모르는 필드는 무시
public class RecipeApiResponseDto {

    @JsonProperty("COOKRCP01")
    private CookRcp COOKRCP01;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CookRcp {
        @JsonProperty("total_count")
        private String totalCount;

        @JsonProperty("row")
        private Recipe[] row;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Recipe {

        @JsonProperty("RCP_SEQ")
        private String rcpSeq;          // 레시피 고유번호 (Recipe Sequence)

        @JsonProperty("RCP_NM")
        private String rcpNm;           // 레시피 이름 (Recipe Name)

        @JsonProperty("RCP_PARTS_DTLS")
        private String rcpPartsDtls;    // 재료 상세 설명 (Ingredients Details)

        @JsonProperty("RCP_PAT2")
        private String cuisineType;     // 요리 종류 (예: 밥, 반찬 등) (Cuisine Type)

        @JsonProperty("MANUAL01")
        private String manual01;        // 조리 방법 1 (Cooking Manual Step 1)

        @JsonProperty("MANUAL02")
        private String manual02;        // 조리 방법 2 (Cooking Manual Step 2)

        @JsonProperty("MANUAL03")
        private String manual03;        // 조리 방법 3 (Cooking Manual Step 3)

        @JsonProperty("MANUAL04")
        private String manual04;        // 조리 방법 4 (Cooking Manual Step 4)

        @JsonProperty("MANUAL05")
        private String manual05;        // 조리 방법 5 (Cooking Manual Step 5)

        @JsonProperty("MANUAL06")
        private String manual06;        // 조리 방법 6 (Cooking Manual Step 6)

        @JsonProperty("MANUAL_IMG01")
        private String manualImg01;     // 조리 방법 1 이미지 (Image for Step 1)

        @JsonProperty("MANUAL_IMG02")
        private String manualImg02;     // 조리 방법 2 이미지 (Image for Step 2)

        @JsonProperty("MANUAL_IMG03")
        private String manualImg03;     // 조리 방법 3 이미지 (Image for Step 3)

        @JsonProperty("MANUAL_IMG04")
        private String manualImg04;     // 조리 방법 4 이미지 (Image for Step 4)

        @JsonProperty("MANUAL_IMG05")
        private String manualImg05;     // 조리 방법 5 이미지 (Image for Step 5)

        @JsonProperty("MANUAL_IMG06")
        private String manualImg06;     // 조리 방법 6 이미지 (Image for Step 6)

        @JsonProperty("RCP_WAY2")
        private String rcpWay2;         // 조리 방법 상세 (예: 찌기, 굽기 등) (Cooking Way Detail)

        @JsonProperty("ATT_FILE_NO_MAIN")
        private String attFileNoMain;   // 메인 이미지 URL (Main Image)

        @JsonProperty("ATT_FILE_NO_MK")
        private String attFileNoMk;     // 썸네일 이미지 URL (Thumbnail Image)

        @JsonProperty("INFO_WGT")
        private String infoWgt;         // 중량 (Weight)

        @JsonProperty("INFO_ENG")
        private String infoEng;         // 열량 (Calories)

        @JsonProperty("INFO_CAR")
        private String infoCar;         // 탄수화물 (Carbohydrates)

        @JsonProperty("INFO_PRO")
        private String infoPro;         // 단백질 (Protein)

        @JsonProperty("INFO_FAT")
        private String infoFat;         // 지방 (Fat)

        @JsonProperty("INFO_NA")
        private String infoNa;          // 나트륨 (Sodium)

        @JsonProperty("HASH_TAG")
        private String hashTag;         // 해시태그 (Hashtag)

        @JsonProperty("RCP_NA_TIP")
        private String rcpNaTip;        // 조리 팁 (Recipe Tip)

        @JsonProperty("RCP_CATEGORY")
        private String rcpCategory;

        // 필요시 추가 필드 넣으면 됨
    }
}
