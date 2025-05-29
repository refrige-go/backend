package com.ohgiraffers.refrigegobackend.user.service;


import com.ohgiraffers.refrigegobackend.global.exception.GlobalExceptionHandler;
import com.ohgiraffers.refrigegobackend.user.dto.JoinDTO;
import com.ohgiraffers.refrigegobackend.user.entity.Role;
import com.ohgiraffers.refrigegobackend.user.entity.User;
import com.ohgiraffers.refrigegobackend.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public JoinService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {

        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    // 회원가입
    public void joinProcess(JoinDTO joinDTO) {

        String username = joinDTO.getUsername();
        String password = joinDTO.getPassword();
        String nickname = joinDTO.getNickname();
        String role = joinDTO.getRole();

        Boolean isExist = userRepository.existsByUsername(username);
        if (isExist) {
            throw new GlobalExceptionHandler.UsernameDuplicateException("유저 아이디가 이미 존재합니다");
        }

        User data = new User();

        data.setUsername(username);
        data.setNickname(nickname);
        data.setPassword(bCryptPasswordEncoder.encode(password));
        data.setRole(role);

        userRepository.save(data);
    }
}
