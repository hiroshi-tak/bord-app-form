package com.example.backend.service;

import com.example.backend.dto.BoardResponse;
import com.example.backend.websocket.BoardWebSocketHandler;
import com.example.backend.repository.BoardRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AdminBoardService {

    private final BoardRepository boardRepository;
    private final BoardWebSocketHandler wsHandler;

    public AdminBoardService(
            BoardRepository boardRepository,
            BoardWebSocketHandler wsHandler) {

        this.boardRepository = boardRepository;
        this.wsHandler = wsHandler;
    }

    public List<BoardResponse> findAll() {
        return boardRepository.findAll()
                .stream()
                .map(BoardResponse::new)
                .toList();
    }

    public void delete(Long id) {

        boardRepository.deleteById(id);

        wsHandler.forceCloseBoard(String.valueOf(id));
    }
}
