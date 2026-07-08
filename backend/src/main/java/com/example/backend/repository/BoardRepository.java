package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.entity.Board;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    // 作成者で検索
    List<Board> findByOwnerUsername(String username);

}