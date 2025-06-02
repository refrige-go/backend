package com.ohgiraffers.refrigegobackend.user.controller;


import com.ohgiraffers.refrigegobackend.user.dto.RequestDTO;
import com.ohgiraffers.refrigegobackend.user.entity.User;
import com.ohgiraffers.refrigegobackend.user.repository.UserRepository;
import com.ohgiraffers.refrigegobackend.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository, UserService userService ) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    // 로그인한 유저 정보조회
    @GetMapping("/mypage")
    public ResponseEntity<?> getMyInfo(HttpServletRequest request) {
        // 1. JWT 토큰에서 username 추출 (ex: SecurityContextHolder 사용)
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. username으로 DB에서 유저 조회
        User user = userRepository.findByUsernameAndDeletedFalse(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
        }

        // 3. 필요한 정보만 담아서 반환 (예: 닉네임)
        Map<String, String> result = new HashMap<>();
        result.put("nickname", user.getNickname());
        result.put("username", user.getUsername());

        return ResponseEntity.ok(result);
    }

    // 회원정보 수정 API
    @PutMapping("/mypage-update")
    public ResponseEntity<?> updateMyPage(@RequestBody RequestDTO updateUserRequest,
                                          Authentication authentication) {
        // authentication 객체에서 username 추출 (JWT 인증 후라면 자동 주입됨)
        String username = authentication.getName();

        // 서비스 호출
        userService.updateUserInfo(username, updateUserRequest.getNickname(), updateUserRequest.getPassword());

        return ResponseEntity.ok("회원정보가 수정되었습니다.");
    }

    // 회원 탈퇴 (자기 자신의 계정 삭제)
    @DeleteMapping("/withdraw")
    public ResponseEntity<?> withdraw(Authentication authentication) {
        String username = authentication.getName(); // JWT 인증된 사용자 아이디

        userService.withdrawUser(username);

        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }
}
