package com.example.week5.controller;

import com.example.week5.service.BoardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/boards")
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("boards", boardService.findAll());
        return "board/list";
    }

    @GetMapping("/new")
    public String createForm() {
        return "board/create";
    }

    @PostMapping
    public String create(@RequestParam String title, @RequestParam String content) {
        boardService.create(title, content);
        return "redirect:/boards";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("board", boardService.findById(id));
        return "board/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("board", boardService.findById(id));
        return "board/edit";
    }

    // HTML forms normally submit GET or POST, so this page uses POST for edit/delete.
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @RequestParam String title, @RequestParam String content) {
        boardService.update(id, title, content);
        return "redirect:/boards/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/boards";
    }
}

