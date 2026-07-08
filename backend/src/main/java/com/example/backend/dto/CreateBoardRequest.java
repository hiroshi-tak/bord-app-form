package com.example.backend.dto;

public class CreateBoardRequest {

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