package com.ohgiraffers.refrigegobackend.user.infrastructure.repository;

import com.ohgiraffers.refrigegobackend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUserId(String userId);
    boolean existsByUserId(String userId);
    Optional<User> findByUserName(String username);
}

