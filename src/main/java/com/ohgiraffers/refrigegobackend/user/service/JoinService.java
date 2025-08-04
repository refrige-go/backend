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

    // 영어 소문자만 허용하는 정규식
    private static final String USERNAME_PATTERN = "^[a-z0-9]+$";

    public JoinService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {

        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    // 아이디 형식 검증 메서드
    private void validateUsernameFormat(String username){
        if(username == null || !username.matches(USERNAME_PATTERN)){
            throw new GlobalExceptionHandler.UsernameDuplicateException("아이디는 영어 소문자와 숫자만 사용이 가능합니다.");
        }
    }

    // 회원가입
    public void joinProcess(JoinDTO joinDTO) {

        String username = joinDTO.getUsername();
        String password = joinDTO.getPassword();
        String nickname = joinDTO.getNickname();
        String role = joinDTO.getRole();

        validateUsernameFormat(username);

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
