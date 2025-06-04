package com.ohgiraffers.refrigegobackend.notification.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    @Field("userId")
    private Long userId;

    @Field("type")
    private NotificationType type;

    @Field("title")
    private String title;

    @Field("content")
    private String content;

    @Field("ingredientsId")
    private List<Long> ingredientsId;

    @Field("recipeId")
    private String recipeId;

    @Field("isRead")
    private Boolean isRead;

    @CreatedDate
    @Field("createdAt")
    private LocalDateTime createdAt;

    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    public static class Builder {
        private Long userId;
        private NotificationType type;
        private String title;
        private String content;
        private List<Long> ingredientsId;
        private String recipeId;
        private Boolean isRead = false;
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder type(NotificationType type) {
            this.type = type;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder ingredientsId(List<Long> ingredientsId) {
            this.ingredientsId = ingredientsId;
            return this;
        }

        public Builder recipeId(String recipeId) {
            this.recipeId = recipeId;
            return this;
        }

        public Builder isRead(Boolean isRead) {
            this.isRead = isRead;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Notification build() {
            Notification notification = new Notification();
            notification.userId = this.userId;
            notification.type = this.type;
            notification.title = this.title;
            notification.content = this.content;
            notification.ingredientsId = this.ingredientsId;
            notification.recipeId = this.recipeId;
            notification.isRead = this.isRead;
            notification.createdAt = this.createdAt;
            return notification;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
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

    public List<Long> getIngredientsId() {
        return ingredientsId;
    }

    public void setIngredientsId(List<Long> ingredientsId) {
        this.ingredientsId = ingredientsId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
