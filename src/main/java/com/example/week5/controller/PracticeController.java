package com.example.week5.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PracticeController {
    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "OK";
    }

    @GetMapping("/practice/param/{word}")
    @ResponseBody
    public String pathVariable(@PathVariable("word") String word) {
        return "경로에서 받은 값: " + word;
    }

    @GetMapping("/practice/add")
    public String add(@RequestParam("a") int a, @RequestParam("b") int b, Model model) {
        model.addAttribute("title", "두 숫자 더하기");
        model.addAttribute("result", a + b);
        return "practice/result";
    }

    @GetMapping("/practice/concat")
    @ResponseBody
    public String concat(@RequestParam("a") String a, @RequestParam("b") String b) {
        return a + b;
    }

    @GetMapping("/practice/multiply")
    public String multiply(@RequestParam("a") int a, @RequestParam("b") int b, Model model) {
        model.addAttribute("title", "두 숫자 곱하기: 페이지 응답");
        model.addAttribute("result", a * b);
        return "practice/result";
    }

    @GetMapping("/practice/redirect")
    public String redirect() {
        return "redirect:/boards";
    }
}

