package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/todo")
public class TodoController {

    @GetMapping({"", "/"})
    public String list(Model model) {
        List<TodoItem> todos = List.of(
                new TodoItem(1L, "Learn Spring Boot", "IN_PROGRESS"),
                new TodoItem(2L, "Build ToDo list screen", "TODO"),
                new TodoItem(3L, "Add unit tests", "DONE"));

        model.addAttribute("todos", todos);
        return "todo/list";
    }

    @GetMapping("/new")
    public String showNewForm() {
        return "todo/form";
    }

    @PostMapping("/confirm")
    public String confirm(@RequestParam("title") String title, Model model) {
        model.addAttribute("title", title);
        return "todo/confirm";
    }

    @PostMapping("/complete")
    public String complete(@RequestParam("title") String title, Model model) {
        model.addAttribute("title", title);
        return "todo/complete";
    }

    private record TodoItem(Long id, String title, String status) {
    }
}
