package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.Todo;
import com.example.demo.service.TodoService;

@Controller
@RequestMapping("/todo")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping({"", "/"})
    public String list(Model model) {
        List<Todo> todos = todoService.findAll();
        model.addAttribute("todos", todos);
        return "todo/list";
    }

    @GetMapping("/new")
    public String showNewForm(@RequestParam(value = "assignee", required = false) String assignee, Model model) {
        model.addAttribute("assignee", assignee);
        return "todo/new";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "指定されたToDoが見つかりません");
            return "redirect:/todo";
        }
        model.addAttribute("todo", todo);
        return "todo/edit";
    }

    @PostMapping("/confirm")
    public String confirm(@RequestParam("title") String title,
                          @RequestParam(value = "assignee", required = false) String assignee,
                          Model model) {
        if (assignee == null || assignee.isBlank()) {
            model.addAttribute("title", title);
            model.addAttribute("errorMessage", "担当者を選択してください");
            return "todo/new";
        }
        model.addAttribute("title", title);
        model.addAttribute("assignee", assignee);
        return "todo/confirm";
    }

    @PostMapping("/complete")
    public String complete(@RequestParam("title") String title,
                           @RequestParam(value = "assignee", required = false) String assignee,
                           RedirectAttributes redirectAttributes) {
        if (assignee == null || assignee.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "担当者を選択してください");
            return "redirect:/todo/new";
        }
        todoService.create(title, assignee);
        redirectAttributes.addFlashAttribute("successMessage", "ToDoを登録しました");
        return "redirect:/todo";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable("id") Long id,
                         @RequestParam("title") String title,
                         RedirectAttributes redirectAttributes) {
        if (todoService.update(id, title)) {
            redirectAttributes.addFlashAttribute("successMessage", "更新が完了しました");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "更新に失敗しました");
        }
        return "redirect:/todo";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable("id") Long id) {
        todoService.toggleCompleted(id);
        return "redirect:/todo";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        if (todoService.deleteById(id)) {
            redirectAttributes.addFlashAttribute("successMessage", "ToDoを削除しました");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "削除に失敗しました");
        }
        return "redirect:/todo";
    }
}
