package com.example.backend.security;

public class UserContext {
    private String userId;
    private String role;

    public UserContext(String userId, String role) {
        this.userId = userId;
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public String getUserId() {
        return userId;
    }
}
