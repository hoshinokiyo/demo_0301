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
import com.example.demo.model.TodoStatus;
import com.example.demo.service.TodoService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/todo")
public class TodoController {

    private static final String SESSION_CURRENT_USER = "currentUser";
    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @ModelAttribute("categories")
    public List<String> categories() {
        return List.of("家事", "買い物", "学校", "仕事", "約束", "その他");
    }

    @GetMapping({"", "/"})
    public String list(@RequestParam(value = "currentUser", required = false) String currentUserInput,
                       Model model,
                       HttpSession session) {
        FamilyAssignee currentUser = resolveCurrentUser(currentUserInput, session);
        List<Todo> todos = todoService.findAll();
        model.addAttribute("todos", todos);
        model.addAttribute("currentUser", currentUser.code());
        model.addAttribute("currentUserLabel", currentUser.label());
        model.addAttribute("isMother", FamilyAssignee.MOTHER.code().equals(currentUser.code()));
        model.addAttribute("statusActive", TodoStatus.ACTIVE.code());
        model.addAttribute("statusDeleteRequested", TodoStatus.DELETE_REQUESTED.code());
        return "todo/list";
    }

    @GetMapping("/history")
    public String history(Model model) {
        model.addAttribute("todos", todoService.findHistory());
        return "todo/history";
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
            redirectAttributes.addFlashAttribute("errorMessage", "更新できませんでした");
        }
        return "redirect:/todo";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        if (!todoService.toggleCompleted(id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "おねがい中は変更できません");
        }
        return "redirect:/todo";
    }

    @PostMapping("/{id}/request-delete")
    public String requestDelete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        if (todoService.requestDelete(id)) {
            redirectAttributes.addFlashAttribute("successMessage", "ママに「やめる」おねがいを出したよ");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "このToDoはおねがいできません");
        }
        return "redirect:/todo";
    }

    @PostMapping("/{id}/approve-delete")
    public String approveDelete(@PathVariable("id") Long id,
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {
        FamilyAssignee currentUser = resolveCurrentUser(null, session);
        if (!FamilyAssignee.MOTHER.code().equals(currentUser.code())) {
            redirectAttributes.addFlashAttribute("errorMessage", "ママだけがOKできるよ");
            return "redirect:/todo";
        }
        if (todoService.approveDelete(id)) {
            redirectAttributes.addFlashAttribute("successMessage", "けしていいよ！履歴にうつしたよ");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "OKできませんでした");
        }
        return "redirect:/todo";
    }

    private FamilyAssignee resolveCurrentUser(String input, HttpSession session) {
        if (input != null) {
            FamilyAssignee.fromInput(input).ifPresent(v -> session.setAttribute(SESSION_CURRENT_USER, v.code()));
        }
        Object inSession = session.getAttribute(SESSION_CURRENT_USER);
        if (inSession instanceof String code) {
            return FamilyAssignee.fromInput(code).orElse(FamilyAssignee.ME);
        }
        session.setAttribute(SESSION_CURRENT_USER, FamilyAssignee.ME.code());
        return FamilyAssignee.ME;
    }
}
