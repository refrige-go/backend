package com.ohgiraffers.refrigegobackend.user.controller;

import com.ohgiraffers.refrigegobackend.user.dto.UserResponseDTO;
import com.ohgiraffers.refrigegobackend.user.dto.UserUpdateRequestDTO;
import com.ohgiraffers.refrigegobackend.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService){
        this.userService = userService;
    }

    // ==== 관리자용 API ==== //

    // 모든 회원 목록 조회
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(){
        List<UserResponseDTO> users = userService.getALlUsers();
        return ResponseEntity.ok(users);
    }

    // 특정 회원 조회
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id){
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    //회원 수정 (관리자용)
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequestDTO request){
        UserResponseDTO user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    //회원 삭제 (관리자용)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    //토큰 검증 엔드포인트
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@AuthenticationPrincipal String username) {
        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("username", username);
        return ResponseEntity.ok(response);
    }
}
