package com.ohgiraffers.refrigegobackend.user.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JoinDTO {

    private String username;
    private String password;
    private String nickname;
    private String role = "ROLE_USER";
}
