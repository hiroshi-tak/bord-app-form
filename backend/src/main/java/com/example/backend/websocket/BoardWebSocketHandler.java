package com.example.backend.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


@Component
public class BoardWebSocketHandler extends TextWebSocketHandler {

    private static final int MAX_CONNECTIONS = 5;

    private final Set<String> joinedSessions = ConcurrentHashMap.newKeySet();

    private final ObjectMapper objectMapper = new ObjectMapper();

    // boardId -> sessions
    private final Map<String, List<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    // sessionId -> boardId
    private final Map<String, String> sessionBoardMap = new ConcurrentHashMap<>();

    // username -> sessions
    private final Map<String, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    private void broadcast(String boardId, TextMessage message) {
        List<WebSocketSession> sessions = rooms.get(boardId);
        if (sessions == null)
            return;

        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(message);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void sendMemberCount(String boardId) {

        List<WebSocketSession> sessions = rooms.get(boardId);

        int count = sessions == null ? 0 : sessions.size();

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode json = mapper.createObjectNode();

        json.put("type", "member:count");
        json.put("count", count);

        try {

            TextMessage message = new TextMessage(json.toString());

            broadcast(boardId, message);

        } catch (Exception e) {

        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        String username = (String) session.getAttributes()
                .get("username");

        if (username == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        // 接続ユーザー管理
        userSessions
            .computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet())
            .add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        JsonNode json = objectMapper.readTree(message.getPayload());
        String type = json.get("type").asText();

        // JOIN（接続数アップ）
        if ("join".equals(type)) {

            String role = (String) session.getAttributes().get("role");
            
            if (role == null) {
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }

            String sessionId = session.getId();

            String boardId = json.get("boardId").asText();

            // すでにjoin済みなら無視
            if (!joinedSessions.add(sessionId)) {
                return;
            }

            // 上限チェック（join単位）
            if (joinedSessions.size() > MAX_CONNECTIONS) {
                joinedSessions.remove(sessionId);
                session.close(
                    new CloseStatus(
                        3003,
                        "MAX_CONNECTIONS_REACHED"));
                return;
            }

            sessionBoardMap.put(sessionId, boardId);

            rooms.computeIfAbsent(boardId, k -> new CopyOnWriteArrayList<>())
                    .add(session);

            sendMemberCount(boardId);

            return;
        }

        // 通常メッセージ
        if ("stroke:add".equals(type)) {
            broadcast(json.get("boardId").asText(), message);
            return;
        }

        // undo
        if ("stroke:undo".equals(type)) {
            broadcast(json.get("boardId").asText(), message);
            return;
        }

        //redo
        if ("stroke:redo".equals(type)) {
            broadcast(json.get("boardId").asText(), message);
            return;
        }

        if ("board:clear".equals(type)) {
            broadcast(json.get("boardId").asText(), message);
            return;
        }

        if ("cursor:move".equals(type)) {
            broadcast(json.get("boardId").asText(), message);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        String sessionId = session.getId();

        joinedSessions.remove(sessionId);

        String username = (String) session.getAttributes()
                .get("username");

        if (username != null) {

            Set<WebSocketSession> sessions = userSessions.get(username);

            if (sessions != null) {

                sessions.remove(session);

                if (sessions.isEmpty()) {
                    userSessions.remove(username, sessions);
                }
            }
        }

        String boardId = sessionBoardMap.remove(sessionId);

        if (boardId != null) {
            List<WebSocketSession> sessions = rooms.get(boardId);

            if (sessions != null) {
                sessions.remove(session);

                if (sessions.isEmpty()) {
                    rooms.remove(boardId);
                }
                
                sendMemberCount(boardId);
            }

            // カーソル削除通知
            String userId = (String) session.getAttributes().get("userId");

            if (userId != null) {

                ObjectNode json = objectMapper.createObjectNode();

                json.put("type", "cursor:leave");
                json.put("userId", userId);

                broadcast(
                        boardId,
                        new TextMessage(json.toString()));
            }
        }
    }


    public void forceCloseBoard(String boardId) {

        List<WebSocketSession> sessions = rooms.get(boardId);

        if (sessions == null)
            return;

        for (WebSocketSession s : sessions) {
            try {
                if (s.isOpen()) {
                    s.close(
                        new CloseStatus(
                            3002,
                            "BOARD_DELETED"));
                }
            } catch (Exception ignored) {
            }
        }

        rooms.remove(boardId);
    }


    public void forceCloseUser(String username) {

        Set<WebSocketSession> sessions = userSessions.get(username);

        if (sessions == null)
            return;

        for (WebSocketSession session : sessions) {

            try {

                if (session.isOpen()) {

                    session.close(
                        new CloseStatus(
                            3001,
                            "USER_DELETED"));
                }

            } catch (Exception ignored) {

            }
        }

        userSessions.remove(username);
    }
}