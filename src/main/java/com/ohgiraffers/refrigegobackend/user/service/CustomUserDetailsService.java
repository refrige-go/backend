package com.ohgiraffers.refrigegobackend.user.service;

import com.ohgiraffers.refrigegobackend.user.domain.User;
import com.ohgiraffers.refrigegobackend.user.dto.CustomUserDetails;
import com.ohgiraffers.refrigegobackend.user.infrastructure.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


// UserDetailsService를 구현하는 클래스
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUserName(username);

        if (user != null) {
            return new CustomUserDetails(user);
        }
        return null;
    }
}
