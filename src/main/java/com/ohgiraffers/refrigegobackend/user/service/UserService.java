package com.ohgiraffers.refrigegobackend.user.service;

import com.ohgiraffers.refrigegobackend.common.UserRole;
import com.ohgiraffers.refrigegobackend.user.domain.User;
import com.ohgiraffers.refrigegobackend.user.dto.LoginUserDTO;
import com.ohgiraffers.refrigegobackend.user.dto.SignupDTO;
import com.ohgiraffers.refrigegobackend.user.infrastructure.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Transactional
    public Integer regist(SignupDTO signupDTO) {

        if (userRepository.existsByUserName(signupDTO.getUserName())) {
            return null;
        }

        try {
            User user = new User();

            user.setUserId(signupDTO.getUserId());
            user.setUserName(signupDTO.getUserName());
            user.setPassword(encoder.encode(signupDTO.getUserPassword()));
            user.setUserRole(UserRole.valueOf(signupDTO.getRole()));

            User savedUser = userRepository.save(user);

            return savedUser.getUserCode();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


}
