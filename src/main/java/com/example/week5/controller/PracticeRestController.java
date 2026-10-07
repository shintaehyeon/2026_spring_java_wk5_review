package com.example.week5.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PracticeRestController {
    @GetMapping("/api/practice/multiply")
    public int multiply(@RequestParam("a") int a, @RequestParam("b") int b) {
        return a * b;
    }
}

