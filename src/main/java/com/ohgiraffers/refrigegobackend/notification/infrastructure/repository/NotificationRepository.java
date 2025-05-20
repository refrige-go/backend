package com.ohgiraffers.refrigegobackend.notification.infrastructure.repository;

import com.ohgiraffers.refrigegobackend.notification.domain.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserId(Long userId);
}
