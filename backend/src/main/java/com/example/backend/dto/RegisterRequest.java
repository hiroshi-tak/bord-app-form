package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "ユーザー名を入力してください")
    @Size(min = 3, max = 20, message = "ユーザー名は3～20文字で入力してください")
    private String username;

    @NotBlank(message = "パスワードを入力してください")
    @Size(min = 8, max = 20, message = "パスワードは8～20文字で入力してください")
    private String password;

    public RegisterRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}