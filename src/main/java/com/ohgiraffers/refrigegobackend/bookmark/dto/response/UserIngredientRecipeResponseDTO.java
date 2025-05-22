package com.ohgiraffers.refrigegobackend.bookmark.dto.response;

import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;

public class UserIngredientRecipeResponseDTO {
    private String rcpSeq;
    private String rcpNm;
    private String image;

    public UserIngredientRecipeResponseDTO(Recipe recipe) {
        this.rcpSeq = recipe.getRcpSeq();
        this.rcpNm = recipe.getRcpNm();
        this.image = recipe.getImage();
    }

    public UserIngredientRecipeResponseDTO() {
    }

    public String getRcpSeq() {
        return rcpSeq;
    }

    public void setRcpSeq(String rcpSeq) {
        this.rcpSeq = rcpSeq;
    }

    public String getRcpNm() {
        return rcpNm;
    }

    public void setRcpNm(String rcpNm) {
        this.rcpNm = rcpNm;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
