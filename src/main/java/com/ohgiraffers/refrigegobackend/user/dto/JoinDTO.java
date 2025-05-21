package com.ohgiraffers.refrigegobackend.user.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JoinDTO {
    private String userId;
    private String username;
    private String password;

    public JoinDTO(String password, String userId, String username) {
        this.password = password;
        this.userId = userId;
        this.username = username;
    }
}
