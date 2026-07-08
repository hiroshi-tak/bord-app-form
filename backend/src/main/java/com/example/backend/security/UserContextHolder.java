package com.example.backend.security;

public class UserContextHolder {

    private static final ThreadLocal<UserContext> HOLDER = new ThreadLocal<>();

    public static void set(UserContext user) {
        HOLDER.set(user);
    }

    public static UserContext get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}