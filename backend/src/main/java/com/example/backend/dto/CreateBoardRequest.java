package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateBoardRequest {

    @NotBlank(message = "ボード名を入力してください")
    @Size(max = 20, message = "ボード名は20文字以内で入力してください")
    private String title;

    public CreateBoardRequest() {
    }

    public CreateBoardRequest(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}