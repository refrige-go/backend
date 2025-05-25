package com.ohgiraffers.refrigegobackend.user.repository;

import com.example.springjwt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    Boolean existsByUsername(String username);

    User findByUsername(String username);
}
