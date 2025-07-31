package com.ohgiraffers.refrigegobackend.user.controller;


import com.ohgiraffers.refrigegobackend.user.dto.JoinDTO;
import com.ohgiraffers.refrigegobackend.user.service.JoinService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class JoinController {

    private final JoinService joinService;

    // 아이디 영어 소문자만 사용 가능
    private static final String USERNAME_PATTERN = "^[a-z0-9]+$";

    public JoinController(JoinService joinService) {
        this.joinService = joinService;
    }

    @PostMapping("/join")
    public String joinProcess(@Valid @RequestBody JoinDTO joinDTO) {

        //컨트롤러에서 추가 검증
        validateUsernameFormat(joinDTO.getUsername());
        validatePassword(joinDTO.getPassword());
        validateNickname(joinDTO.getNickname());

        joinService.joinProcess(joinDTO);
        return "회원가입이 완료되었습니다.";
    }

    // 아이디 형식 검증
    private void validateUsernameFormat(String username){
        if (username == null || !username.matches(USERNAME_PATTERN)){
            throw new IllegalArgumentException("아이디는 영어 소문자와 숫자만 사용 가능합니다.");
        }
    }

    // 비밀번호 검증
    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수 입력값입니다.");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("비밀번호는 6자 이상이어야 합니다.");
        }
        if (password.length() > 20) {
            throw new IllegalArgumentException("비밀번호는 20자 이하여야 합니다.");
        }
        // 영문, 숫자, 특수문자만 허용
        if (!password.matches("^[A-Za-z\\d@$!%*?&]+$")) {
            throw new IllegalArgumentException("비밀번호는 영문, 숫자, 특수문자(@$!%*?&)만 사용 가능합니다.");
        }
    }

    // 닉네임 검증
    private void validateNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수 입력값입니다.");
        }
        if (nickname.length() < 2) {
            throw new IllegalArgumentException("닉네임은 2자 이상이어야 합니다.");
        }
        if (nickname.length() > 15) {
            throw new IllegalArgumentException("닉네임은 15자 이하여야 합니다.");
        }
    }
}
