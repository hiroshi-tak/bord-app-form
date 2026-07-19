package com.example.backend.dto;

import lombok.Getter;

@Getter
public class AdminUserResponse {

    private final Long id;
    private final String username;

    public AdminUserResponse(
            Long id,
            String username) {
        this.id = id;
        this.username = username;
    }
}
