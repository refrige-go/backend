package com.ohgiraffers.refrigegobackend.user.service;

import com.ohgiraffers.refrigegobackend.user.dto.CustomUserDetails;
import com.ohgiraffers.refrigegobackend.user.entity.User;
import com.ohgiraffers.refrigegobackend.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// DB에서 사용자 조회
// UserEntity를 찾아서 CustomUserDetails로 감싸서 반환

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    // 프론트에서 로그인 시도
    // Spring Security는 CustomUserDetailsService를 호출해서
    // 아이디(username)를 기반으로 DB에서 사용자를 찾음
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User userData = userRepository.findByUsernameAndDeletedFalse(username); // 로그인 시도한 유저 db에서 조회

        if (userData == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }

        // 입력한 유저의 정보가 db에 존재하면 그 사용자의 정보를 UserDetails로 넘겨,
        // 계속해서 Security가 그 유저의 정보를 사용하게함
        return new CustomUserDetails(userData);
        // 조회한 유저를 CustomUserDetails로 감싸서 리턴
        // 이 CustomUserDetails를 Security가 계속사용
    }
}
