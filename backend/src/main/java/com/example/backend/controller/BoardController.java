package com.example.backend.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend.dto.BoardResponse;
import com.example.backend.dto.CreateBoardRequest;
//import com.example.backend.dto.UpdateBoardRequest;
import com.example.backend.entity.Board;
import com.example.backend.entity.User;
import com.example.backend.service.BoardService;
import com.example.backend.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

        private final BoardService boardService;
        private final UserService userService;

        public BoardController(
                BoardService boardService,
                UserService userService) {

                this.boardService = boardService;
                this.userService = userService;
        }

        // ボード作成
        @PostMapping
        public ResponseEntity<BoardResponse> create(
                        @Valid @RequestBody CreateBoardRequest request,
                        Principal principal) {

                User user = userService.findByUsername(principal.getName());

                Board board = boardService.create(
                                request.getTitle(),
                                user);

                return ResponseEntity.ok(new BoardResponse(board));
        }

        // ボード一覧
        @GetMapping
        public List<BoardResponse> list(Principal principal) {

                return boardService.findAllBoards()
                                .stream()
                                .map(BoardResponse::new)
                                .toList();
        }

        // ボード取得
        @GetMapping("/{id}")
        public ResponseEntity<BoardResponse> detail(
                        @PathVariable Long id) {

                Board board = boardService.findById(id);

                return ResponseEntity.ok(new BoardResponse(board));
        }

/* 
        // ボード編集
        @PutMapping("/{id}")
        public ResponseEntity<BoardResponse> update(
                        @PathVariable Long id,
                        @Valid @RequestBody UpdateBoardRequest request) {

                Board board = boardService.update(
                                id,
                                request.getTitle());

                return ResponseEntity.ok(new BoardResponse(board));
        }

        // ボード削除
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(
                @PathVariable Long id) {

                boardService.delete(id);

                return ResponseEntity.noContent().build();
        }
*/
}