package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.form.TodoForm;
import com.example.demo.model.FamilyAssignee;
import com.example.demo.model.Todo;
import com.example.demo.service.TodoService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/todo")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @ModelAttribute("categories")
    public List<String> categories() {
        return List.of("家事", "買い物", "学校", "仕事", "その他");
    }

    @GetMapping({"", "/"})
    public String list(Model model) {
        List<Todo> todos = todoService.findAll();
        model.addAttribute("todos", todos);
        return "todo/list";
    }

    @GetMapping("/new")
    public String showNewForm(@RequestParam(value = "assignee", required = false) String assignee, Model model) {
        if (!model.containsAttribute("todoForm")) {
            TodoForm form = new TodoForm();
            FamilyAssignee.fromInput(assignee).ifPresent(v -> form.setAssignee(v.code()));
            model.addAttribute("todoForm", form);
        }
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
    public String confirm(@Valid @ModelAttribute("todoForm") TodoForm todoForm,
                          BindingResult bindingResult,
                          Model model) {
        var normalized = FamilyAssignee.fromInput(todoForm.getAssignee());
        if (normalized.isEmpty()) {
            bindingResult.rejectValue("assignee", "required", "担当者は必須です");
        } else {
            todoForm.setAssignee(normalized.get().code());
            model.addAttribute("assigneeLabel", normalized.get().label());
        }

        if (bindingResult.hasErrors()) {
            return "todo/new";
        }
        return "todo/confirm";
    }

    @PostMapping("/complete")
    public String complete(@Valid @ModelAttribute("todoForm") TodoForm todoForm,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        var normalized = FamilyAssignee.fromInput(todoForm.getAssignee());
        if (normalized.isEmpty()) {
            bindingResult.rejectValue("assignee", "required", "担当者は必須です");
        } else {
            todoForm.setAssignee(normalized.get().code());
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("assigneeLabel", normalized.map(FamilyAssignee::label).orElse(""));
            return "todo/confirm";
        }

        todoService.create(todoForm);
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
