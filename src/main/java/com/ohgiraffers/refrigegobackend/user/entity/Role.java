package com.ohgiraffers.refrigegobackend.user.entity;

public enum Role {

    ROLE_USER("ROLE_USER"),
    ROLE_ADMIN("ROLE_ADMIN");

    private String role;

    Role(String role) { this.role = role; }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return role;
    }

}
