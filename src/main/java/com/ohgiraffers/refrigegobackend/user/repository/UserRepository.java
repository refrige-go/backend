package com.ohgiraffers.refrigegobackend.user.repository;


import com.ohgiraffers.refrigegobackend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByUsername(String username);
    User findByUsernameAndDeletedFalse(String username);
    List<User> findAllByDeletedFalse();

    // ==== 관리자용 메서드 추가 ==== //
    // ID로 삭제되지 않은 회원 조회
    Optional<User> findByIdAndDeletedFalse(Long id);

    // 전체 회원 조회
    List<User> findAll();

    // 닉네임 중복 검사
    Boolean existsByNickname(String nickname);
}