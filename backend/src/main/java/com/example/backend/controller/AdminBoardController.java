package com.example.backend.controller;

import com.example.backend.dto.BoardResponse;
import com.example.backend.service.AdminBoardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/boards")
public class AdminBoardController {

    private final AdminBoardService adminBoardService;

    public AdminBoardController(AdminBoardService adminBoardService) {
        this.adminBoardService = adminBoardService;
    }

    @GetMapping
    public List<BoardResponse> list() {
        return adminBoardService.findAll();
    }

    @DeleteMapping("/{boardId}")
    public void delete(@PathVariable Long boardId) {
        adminBoardService.delete(boardId);
    }
}
