package com.example.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.backend.entity.Board;
import com.example.backend.entity.User;
import com.example.backend.repository.BoardRepository;

@Service
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    // ボード作成
    public Board create(String title, User owner) {

        Board board = new Board();

        board.setTitle(title);
        board.setOwner(owner);

        return boardRepository.save(board);
    }

    // 自分が作成したボード一覧
    public List<Board> findMyBoards(String username) {

        return boardRepository.findByOwnerUsername(username);
    }

    // ボード一覧
    public List<Board> findAllBoards() {

        return boardRepository.findAll();
    }

    // ボード取得
    public Board findById(Long id) {

        return boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Board not found"));
    }

    // タイトル変更
    public Board update(Long id, String title) {

        Board board = findById(id);

        board.setTitle(title);

        return boardRepository.save(board);
    }

    // ボード削除
    public void delete(Long id) {

        Board board = findById(id);

        boardRepository.delete(board);
    }

}