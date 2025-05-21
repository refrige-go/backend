package com.ohgiraffers.refrigegobackend.user.service;

import com.ohgiraffers.refrigegobackend.user.dto.CustomUserDetails;
import com.ohgiraffers.refrigegobackend.user.entity.UserEntity;
import com.ohgiraffers.refrigegobackend.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("로그인 시도 username: " + username);
        
        // 데이터베이스에서 사용자 찾기
        UserEntity userData = userRepository.findByUsername(username);

        if (userData != null) {
            System.out.println("사용자 찾음: " + userData.getUsername() + ", 역할: " + userData.getRole());
            return new CustomUserDetails(userData);
        }

        // 사용자를 찾지 못한 경우 예외 발생 (null 반환 대신)
        System.out.println("사용자를 찾을 수 없음: " + username);
        throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
    }
}
