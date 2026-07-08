package com.example.backend.dto;

import com.example.backend.entity.Board;

public class BoardResponse {

    private Long id;
    private String title;
    private String ownerName;

    public BoardResponse(Board board) {
        this.id = board.getId();
        this.title = board.getTitle();
        this.ownerName = board.getOwner().getUsername();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getOwnerName() {
        return ownerName;
    }
}