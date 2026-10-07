package com.example.week5.controller;

import com.example.week5.entity.Board;
import com.example.week5.service.BoardService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boards")
public class BoardRestController {
    private final BoardService boardService;

    public BoardRestController(BoardService boardService) {
        this.boardService = boardService;
    }

    public record BoardRequest(String title, String content) { }
    public record BoardResponse(Long id, String title, String content) {
        public static BoardResponse from(Board board) {
            return new BoardResponse(board.getId(), board.getTitle(), board.getContent());
        }
    }

    @GetMapping
    public List<BoardResponse> list() {
        return boardService.findAll().stream().map(BoardResponse::from).toList();
    }

    @GetMapping("/{id}")
    public BoardResponse detail(@PathVariable Long id) {
        return BoardResponse.from(boardService.findById(id));
    }

    @PostMapping
    public ResponseEntity<BoardResponse> create(@RequestBody BoardRequest request) {
        Board board = boardService.create(request.title(), request.content());
        return ResponseEntity.created(URI.create("/api/boards/" + board.getId()))
                .body(BoardResponse.from(board));
    }

    @PutMapping("/{id}")
    public BoardResponse update(@PathVariable Long id, @RequestBody BoardRequest request) {
        return BoardResponse.from(boardService.update(id, request.title(), request.content()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boardService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

