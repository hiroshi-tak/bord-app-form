package com.example.backend.service;

import com.example.backend.dto.AdminUserResponse;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.websocket.BoardWebSocketHandler;

import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final BoardWebSocketHandler boardWebSocketHandler;

    public AdminUserService(
            UserRepository userRepository,
            BoardWebSocketHandler boardWebSocketHandler) {
        this.userRepository = userRepository;
        this.boardWebSocketHandler = boardWebSocketHandler;
    }

    public List<AdminUserResponse> findAll() {
        return userRepository.findAll()
            .stream()
                .map(user -> new AdminUserResponse(
                        user.getId(),
                        user.getUsername()
                ))
                .toList();
    }

    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow();

        if ("ADMIN".equals(user.getRole())) {
            throw new IllegalStateException("管理者は削除できません");
        }

        // WebSocket切断
        boardWebSocketHandler.forceCloseUser(
                user.getUsername());
        
        // DB削除
        userRepository.delete(user);        
    }
}