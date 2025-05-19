package com.ohgiraffers.refrigegobackend.notification.dto;

import java.util.List;

public class NotificationRequestDto {
    private Long userId;
    private String title;
    private String content;
    private String type;
    private List<Long> ingredientsId;

    public NotificationRequestDto() {
    }

    public NotificationRequestDto(Long userId, String title, String content, String type, List<Long> ingredientsId) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.ingredientsId = ingredientsId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Long> getIngredientsId() {
        return ingredientsId;
    }

    public void setIngredientsId(List<Long> ingredientsId) {
        this.ingredientsId = ingredientsId;
    }
}
