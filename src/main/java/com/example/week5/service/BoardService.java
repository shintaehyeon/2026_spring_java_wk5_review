package com.example.week5.service;

import com.example.week5.entity.Board;
import java.util.List;

public interface BoardService {
    List<Board> findAll();
    Board findById(Long id);
    Board create(String title, String content);
    Board update(Long id, String title, String content);
    void delete(Long id);
}

