package com.ohgiraffers.refrigegobackend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JoinDTO {

    @NotBlank(message = "아이디는 필수 입력값입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하여야 합니다.")
    @Pattern(regexp = "^[a-z0-9]+$", message = "아이디는 영어 소문자와 숫자만 사용 가능합니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 6,max = 20, message = "비밀번호는 6자 이상 20자 이하여야 합니다.")
    @Pattern(regexp = "^[A-Za-z\\d@$!%*?&]+$",
            message = "비밀번호는 영문, 숫자, 특수문자를 사용할 수 있습니다.")
    private String password;

    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    @Size(min = 2, max = 15, message = "닉네임은 2자 이상 15자 이하여야 합니다.")
    private String nickname;

    private String role = "ROLE_USER";
}
