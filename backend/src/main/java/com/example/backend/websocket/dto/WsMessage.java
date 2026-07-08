package com.example.backend.websocket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class WsMessage {

    private String type; // メッセージ種別
    private String boardId; // どのボードか
    private Object data; // 本体データ（Strokeなど）

    public WsMessage() {
    }

    public WsMessage(String type, String boardId, Object data) {
        this.type = type;
        this.boardId = boardId;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
