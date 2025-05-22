package com.ohgiraffers.refrigegobackend.bookmark.dto.response;

import com.ohgiraffers.refrigegobackend.recipe.domain.Recipe;

public class CuisineTypeRecipeResponseDTO {

    private String rcpSeq;
    private String rcpNm;
    private String image;
    private boolean bookmarked;

    public CuisineTypeRecipeResponseDTO() {}

    public CuisineTypeRecipeResponseDTO(Recipe recipe, boolean bookmarked) {
        this.rcpSeq = recipe.getRcpSeq();
        this.rcpNm = recipe.getRcpNm();
        this.image = recipe.getImage();
        this.bookmarked = bookmarked;
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

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }
}
