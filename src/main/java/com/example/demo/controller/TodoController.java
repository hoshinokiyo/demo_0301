package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/todo")
public class TodoController {

    @GetMapping({"", "/"})
    public String list(Model model) {
        List<TodoItem> todos = List.of(
                new TodoItem(1L, "Spring Bootの学習", "進行中"),
                new TodoItem(2L, "ToDoアプリの画面作成", "未着手"),
                new TodoItem(3L, "単体テストの追加", "完了"));

        model.addAttribute("todos", todos);
        return "todo/list";
    }

    private record TodoItem(Long id, String title, String status) {
    }
}
