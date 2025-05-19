package com.ohgiraffers.refrigegobackend.recipe.dto;

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

        @JsonProperty("MANUAL07")
        private String manual07;        // 조리 방법 7 (Cooking Manual Step 7)

        @JsonProperty("MANUAL08")
        private String manual08;        // 조리 방법 8 (Cooking Manual Step 8)

        @JsonProperty("MANUAL09")
        private String manual09;        // 조리 방법 9 (Cooking Manual Step 9)

        @JsonProperty("MANUAL10")
        private String manual10;        // 조리 방법 10 (Cooking Manual Step 10)

        @JsonProperty("MANUAL11")
        private String manual11;        // 조리 방법 11 (Cooking Manual Step 11)

        @JsonProperty("MANUAL12")
        private String manual12;        // 조리 방법 12 (Cooking Manual Step 12)

        @JsonProperty("MANUAL13")
        private String manual13;        // 조리 방법 13 (Cooking Manual Step 13)

        @JsonProperty("MANUAL14")
        private String manual14;        // 조리 방법 14 (Cooking Manual Step 14)

        @JsonProperty("MANUAL15")
        private String manual15;        // 조리 방법 15 (Cooking Manual Step 15)

        @JsonProperty("MANUAL16")
        private String manual16;        // 조리 방법 16 (Cooking Manual Step 16)

        @JsonProperty("MANUAL17")
        private String manual17;        // 조리 방법 17 (Cooking Manual Step 17)

        @JsonProperty("MANUAL18")
        private String manual18;        // 조리 방법 18 (Cooking Manual Step 18)

        @JsonProperty("MANUAL19")
        private String manual19;        // 조리 방법 19 (Cooking Manual Step 19)

        @JsonProperty("MANUAL20")
        private String manual20;        // 조리 방법 20 (Cooking Manual Step 20)

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

        @JsonProperty("MANUAL_IMG07")
        private String manualImg07;     // 조리 방법 7 이미지 (Image for Step 7)

        @JsonProperty("MANUAL_IMG08")
        private String manualImg08;     // 조리 방법 8 이미지 (Image for Step 8)

        @JsonProperty("MANUAL_IMG09")
        private String manualImg09;     // 조리 방법 9 이미지 (Image for Step 9)

        @JsonProperty("MANUAL_IMG10")
        private String manualImg10;     // 조리 방법 10 이미지 (Image for Step 10)

        @JsonProperty("MANUAL_IMG11")
        private String manualImg11;     // 조리 방법 11 이미지 (Image for Step 11)

        @JsonProperty("MANUAL_IMG12")
        private String manualImg12;     // 조리 방법 12 이미지 (Image for Step 12)

        @JsonProperty("MANUAL_IMG13")
        private String manualImg13;     // 조리 방법 13 이미지 (Image for Step 13)

        @JsonProperty("MANUAL_IMG14")
        private String manualImg14;     // 조리 방법 14 이미지 (Image for Step 14)

        @JsonProperty("MANUAL_IMG15")
        private String manualImg15;     // 조리 방법 15 이미지 (Image for Step 15)

        @JsonProperty("MANUAL_IMG16")
        private String manualImg16;     // 조리 방법 16 이미지 (Image for Step 16)

        @JsonProperty("MANUAL_IMG17")
        private String manualImg17;     // 조리 방법 17 이미지 (Image for Step 17)

        @JsonProperty("MANUAL_IMG18")
        private String manualImg18;     // 조리 방법 18 이미지 (Image for Step 18)

        @JsonProperty("MANUAL_IMG19")
        private String manualImg19;     // 조리 방법 19 이미지 (Image for Step 19)

        @JsonProperty("MANUAL_IMG20")
        private String manualImg20;     // 조리 방법 20 이미지 (Image for Step 20)

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

        // 필요시 추가 필드 넣으면 됨
    }
}
