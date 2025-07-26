package com.ohgiraffers.refrigegobackend.user.dto;

import lombok.Getter;
import lombok.Setter;

//회원정보 수정용
@Getter
@Setter
public class UserUpdateRequestDTO {
    private String nickname;
    private String role; // ROLE_USER, ROLE_ADMIN
    private String profileImageUrl;
}
