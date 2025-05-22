package com.ohgiraffers.refrigegobackend.user.controller;


import com.ohgiraffers.refrigegobackend.user.dto.JoinDTO;
import com.ohgiraffers.refrigegobackend.user.service.JoinService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JoinController {

    private final JoinService joinService;

    public JoinController(JoinService joinService) {

        this.joinService = joinService;
    }

    @PostMapping("/join")
    public ResponseEntity<String> joinProcess(@RequestBody JoinDTO joinDTO) {

        joinService.joinProcess(joinDTO);


        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }
}
