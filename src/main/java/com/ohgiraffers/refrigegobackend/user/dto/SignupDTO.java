package com.ohgiraffers.refrigegobackend.user.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SignupDTO {
    private String userId;
    private String userName;
    private String userPassword;
    private String role;


    public SignupDTO() {
    }

    public SignupDTO(String userId, String userName, String userPassword, String role) {
        this.userId = userId;
        this.userName = userName;
        this.userPassword = userPassword;
        this.role = role;
    }

    @Override
    public String toString() {
        return "SinupDTO{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", userPassword='" + userPassword + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
