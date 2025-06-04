package com.ohgiraffers.refrigegobackend.user.repository;


import com.ohgiraffers.refrigegobackend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByUsername(String username);
    User findByUsernameAndDeletedFalse(String username);
    List<User> findAllByDeletedFalse();
}