package com.ohgiraffers.refrigegobackend.notification.dto;

import com.ohgiraffers.refrigegobackend.notification.domain.Notification;

import java.time.LocalDateTime;

public class NotificationResponseDto {
    private String id;
    private String title;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;


    public NotificationResponseDto(Notification notification) {
        this.id = notification.getId();
        this.title = notification.getTitle();
        this.content = notification.getContent();
        this.isRead = notification.getIsRead();
        this.createdAt = notification.getCreatedAt();
    }

    public NotificationResponseDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
