package com.example.backend.security;

public class AdminAuth {

    public static void check(UserContext user) {
        if (user == null || !"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Forbidden");
        }
    }
}
