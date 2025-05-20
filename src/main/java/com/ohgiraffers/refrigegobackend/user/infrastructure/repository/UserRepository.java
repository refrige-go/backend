package com.ohgiraffers.refrigegobackend.user.infrastructure.repository;

import com.ohgiraffers.refrigegobackend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByUserName(String username);

}

