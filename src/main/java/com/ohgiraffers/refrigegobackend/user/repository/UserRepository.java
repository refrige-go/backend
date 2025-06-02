package com.ohgiraffers.refrigegobackend.user.repository;


import com.ohgiraffers.refrigegobackend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByUsername(String username);
    User findByUsername(String username);
    User findByUsernameAndDeletedFalse(String username);
}