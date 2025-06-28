package com.ohgiraffers.refrigegobackend.fcm.repository;

import com.ohgiraffers.refrigegobackend.fcm.domain.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    
    /**
     * 사용자 ID로 FCM 토큰을 조회합니다.
     * @param userId 사용자 ID
     * @return FCM 토큰 (존재하지 않으면 Optional.empty())
     */
    Optional<FcmToken> findByUserId(Long userId);
    
    /**
     * FCM 토큰으로 엔티티를 조회합니다.
     * @param fcmToken FCM 토큰
     * @return FCM 토큰 엔티티 (존재하지 않으면 Optional.empty())
     */
    Optional<FcmToken> findByFcmToken(String fcmToken);
    
    /**
     * 사용자 ID로 FCM 토큰이 존재하는지 확인합니다.
     * @param userId 사용자 ID
     * @return 존재 여부
     */
    boolean existsByUserId(Long userId);
    
    /**
     * 사용자 ID로 FCM 토큰을 삭제합니다.
     * @param userId 사용자 ID
     */
    void deleteByUserId(Long userId);
} 