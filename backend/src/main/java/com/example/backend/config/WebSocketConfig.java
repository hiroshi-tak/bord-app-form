package com.example.backend.config;

import com.example.backend.security.JwtService;
import com.example.backend.websocket.BoardWebSocketHandler;
import com.example.backend.websocket.JwtHandshakeInterceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.beans.factory.annotation.Value;


@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private final BoardWebSocketHandler boardWebSocketHandler;
    private final JwtService jwtService;

    public WebSocketConfig(BoardWebSocketHandler boardWebSocketHandler,
            JwtService jwtService) {
        this.boardWebSocketHandler = boardWebSocketHandler;
        this.jwtService = jwtService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(boardWebSocketHandler, "/ws/board")
                .addInterceptors(new JwtHandshakeInterceptor(jwtService))
                .setAllowedOrigins(frontendUrl);
    }

}