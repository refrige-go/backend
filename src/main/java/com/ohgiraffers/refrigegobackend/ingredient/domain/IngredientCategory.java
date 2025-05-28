package com.ohgiraffers.refrigegobackend.ingredient.domain;

public enum IngredientCategory {
    GRAIN_POWDER("곡류/분말"),
    MEAT("육류"),
    SEAFOOD("수산물/해산물"),
    VEGETABLE("채소"),
    FRUIT("과일"),
    MUSHROOM("버섯"),
    DAIRY("유제품"),
    BEAN("두류/콩류"),
    SEASONING("조미료/양념"),
    OIL("기름/유지"),
    NOODLE_RICE_CAKE("면/떡"),
    PROCESSED_FOOD("가공식품"),
    PICKLED("장아찌/절임"),
    ETC("기타");

    private final String displayName;

    IngredientCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // 문자열로부터 Enum 찾기
    public static IngredientCategory fromDisplayName(String displayName) {
        for (IngredientCategory category : values()) {
            if (category.displayName.equals(displayName)) {
                return category;
            }
        }
        return ETC; // 기본값
    }
}