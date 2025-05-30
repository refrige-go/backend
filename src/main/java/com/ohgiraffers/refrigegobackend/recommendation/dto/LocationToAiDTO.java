package com.ohgiraffers.refrigegobackend.recommendation.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LocationToAiDTO {

    private double latitude;
    private double longitude;
    private List<RecipeDTO> recipes;

    public LocationToAiDTO() {}

    public LocationToAiDTO(double latitude, double longitude, List<RecipeDTO> recipes) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.recipes = recipes;
    }
}
