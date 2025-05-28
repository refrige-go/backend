package com.ohgiraffers.refrigegobackend.user.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/secure") // ✅ 공통 prefix 경로
public class SecureController {

    @GetMapping("/admin")
    public String adminP() {
        return "Admin Controller";
    }

    @GetMapping("/ping") // 프론트에서 로그인 여부 체크용으로 사용
    public String ping() {
        return "로그인된 사용자입니다.";
    }
}

