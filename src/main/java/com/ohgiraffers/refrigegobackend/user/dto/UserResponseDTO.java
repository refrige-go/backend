package com.ohgiraffers.refrigegobackend.user.dto;

import com.ohgiraffers.refrigegobackend.user.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

//회원 조회용

@Getter
@Setter
public class UserResponseDTO {
    private Long id;
    private String username;
    private String nickname;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private boolean deleted;
    private String profileImageUrl;

}
