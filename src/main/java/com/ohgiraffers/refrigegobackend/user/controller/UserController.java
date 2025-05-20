package com.ohgiraffers.refrigegobackend.user.controller;

import com.ohgiraffers.refrigegobackend.user.dto.SignupDTO;
import com.ohgiraffers.refrigegobackend.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupDTO signupDTO)  {

        Integer result = userService.regist(signupDTO);
        String message;

        if(result == null){
            message = "중복회원이 존재합니다.";
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } else if (result == 0) {
            message="서버에 오류가 발생하였습니다.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
        } else {
            message ="회원가입이 완료되었습니다.";
            return ResponseEntity.ok(message);
        }
    }


}
