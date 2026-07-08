package com.example.backend.controller;

import com.example.backend.service.AdminUserService;
import org.springframework.web.bind.annotation.*;
import com.example.backend.entity.User;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(
            AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public List<User> list() {
        return adminUserService.findAll();
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {

        adminUserService.delete(id);
    }

}