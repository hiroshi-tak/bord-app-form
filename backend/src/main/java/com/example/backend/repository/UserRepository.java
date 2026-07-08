package com.example.backend.repository;

import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // usernameでユーザー検索（ログイン時に使用）
    Optional<User> findByUsername(String username);

    // usernameの存在チェック（登録時の重複防止）
    boolean existsByUsername(String username);
}