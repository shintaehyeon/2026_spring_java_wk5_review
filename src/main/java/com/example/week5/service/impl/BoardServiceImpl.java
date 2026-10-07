package com.example.week5.service.impl;

import com.example.week5.entity.Board;
import com.example.week5.repository.BoardRepository;
import com.example.week5.service.BoardService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;

    public BoardServiceImpl(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Override
    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    @Override
    public Board findById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글이 없습니다."));
    }

    @Override
    public Board create(String title, String content) {
        return boardRepository.save(new Board(title, content));
    }

    @Override
    public Board update(Long id, String title, String content) {
        Board board = findById(id);
        board.update(title, content);
        return boardRepository.save(board);
    }

    @Override
    public void delete(Long id) {
        findById(id);
        boardRepository.deleteById(id);
    }
}

