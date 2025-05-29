package com.ohgiraffers.refrigegobackend.user.service;

import com.ohgiraffers.refrigegobackend.user.entity.User;
import com.ohgiraffers.refrigegobackend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 생성자 주입
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void updateUserInfo(String username, String newNickname, String newPassword) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        // 닉네임, 비밀번호 수정
        user.setNickname(newNickname);
        user.setPassword(passwordEncoder.encode(newPassword));

    }
}
