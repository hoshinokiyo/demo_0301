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

import com.example.demo.form.ApproverResetForm;
import com.example.demo.form.MomEnterForm;
import com.example.demo.form.MomSecretForm;
import com.example.demo.form.TodoForm;
import com.example.demo.model.FamilyAssignee;
import com.example.demo.model.Todo;
import com.example.demo.model.TodoStatus;
import com.example.demo.service.MomAuthService;
import com.example.demo.service.TodoService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/todo")
public class TodoController {

    private static final String SESSION_CURRENT_USER = "currentUser";
    private static final String SESSION_APPROVER_AUTHENTICATED = "approverAuthenticated";

    private final TodoService todoService;
    private final MomAuthService momAuthService;

    public TodoController(TodoService todoService, MomAuthService momAuthService) {
        this.todoService = todoService;
        this.momAuthService = momAuthService;
    }

    @ModelAttribute("categories")
    public List<String> categories() {
        return List.of("家事", "買い物", "学校", "仕事", "約束", "その他");
    }

    @GetMapping({"", "/"})
    public String list(Model model, HttpSession session) {
        List<Todo> todos = todoService.findAll();
        model.addAttribute("todos", todos);
        model.addAttribute("statusActive", TodoStatus.ACTIVE.code());
        model.addAttribute("statusDeleteRequested", TodoStatus.DELETE_REQUESTED.code());
        model.addAttribute("currentUser", resolveCurrentUser(session).code());
        return "todo/list";
    }

    @PostMapping("/switch-user")
    public String switchUser(@RequestParam("currentUser") String currentUser, HttpSession session) {
        FamilyAssignee selected = FamilyAssignee.fromInput(currentUser).orElse(FamilyAssignee.ME);
        session.setAttribute(SESSION_CURRENT_USER, selected.code());
        clearApproverAuthentication(session);
        return "redirect:/todo";
    }

    @GetMapping("/mom/start")
    public String momStart(HttpSession session) {
        FamilyAssignee approver = momAuthService.getApprover();
        session.setAttribute(SESSION_CURRENT_USER, approver.code());
        clearApproverAuthentication(session);
        if (!momAuthService.isSecretInitialized()) {
            return "redirect:/todo/mom/secret";
        }
        return "redirect:/todo/mom/enter";
    }

    @GetMapping("/mom/enter")
    public String momEnterPage(Model model,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
        if (!isCurrentUserApprover(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "今の承認者だけが入れるよ");
            return "redirect:/todo/mom";
        }
        if (!momAuthService.isSecretInitialized()) {
            return "redirect:/todo/mom/secret";
        }
        if (!model.containsAttribute("momEnterForm")) {
            model.addAttribute("momEnterForm", new MomEnterForm());
        }
        return "todo/mom_enter";
    }

    @PostMapping("/mom/enter")
    public String momEnter(@Valid @ModelAttribute("momEnterForm") MomEnterForm form,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           HttpSession session) {
        if (!isCurrentUserApprover(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "今の承認者だけが入れるよ");
            return "redirect:/todo/mom";
        }
        if (!momAuthService.isSecretInitialized()) {
            return "redirect:/todo/mom/secret";
        }
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.momEnterForm", bindingResult);
            redirectAttributes.addFlashAttribute("momEnterForm", form);
            return "redirect:/todo/mom/enter";
        }
        if (!momAuthService.verify(form.getSecret())) {
            redirectAttributes.addFlashAttribute("errorMessage", "合言葉がちがうよ");
            redirectAttributes.addFlashAttribute("momEnterForm", new MomEnterForm());
            return "redirect:/todo/mom/enter";
        }
        setApproverAuthenticated(session, true);
        return "redirect:/todo/mom";
    }

    @GetMapping("/mom")
    public String momPage(Model model,
                          HttpSession session) {
        FamilyAssignee currentUser = resolveCurrentUser(session);
        FamilyAssignee approver = momAuthService.getApprover();
        boolean isApprover = approver.code().equals(currentUser.code());
        boolean isSecretInitialized = momAuthService.isSecretInitialized();
        boolean isAuthenticated = isApprover && isApproverAuthenticated(session);
        boolean canApprove = isAuthenticated && isSecretInitialized;
        model.addAttribute("isMom", canApprove);
        model.addAttribute("isApprover", isApprover);
        model.addAttribute("isSecretInitialized", isSecretInitialized);
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("approverLabel", approver.label());
        model.addAttribute("currentUserLabel", currentUser.label());
        model.addAttribute("todos", canApprove ? todoService.findDeleteRequested() : List.of());
        return "todo/mom";
    }

    @PostMapping("/mom/exit")
    public String momExit(HttpSession session) {
        session.setAttribute(SESSION_CURRENT_USER, FamilyAssignee.ME.code());
        clearApproverAuthentication(session);
        return "redirect:/todo";
    }

    @GetMapping("/mom/secret")
    public String momSecretPage(Model model,
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {
        if (!isCurrentUserApprover(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "今の承認者だけが見れるよ");
            return "redirect:/todo/mom";
        }
        boolean initialSetup = !momAuthService.isSecretInitialized();
        if (!initialSetup && !isApproverAuthenticated(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "先に合言葉を入力してね");
            return "redirect:/todo/mom/enter";
        }
        model.addAttribute("initialSetup", initialSetup);
        if (!model.containsAttribute("momSecretForm")) {
            model.addAttribute("momSecretForm", new MomSecretForm());
        }
        if (!model.containsAttribute("approverResetForm")) {
            model.addAttribute("approverResetForm", new ApproverResetForm());
        }
        return "todo/mom_secret";
    }

    @PostMapping("/mom/secret")
    public String momSecret(@ModelAttribute("momSecretForm") MomSecretForm form,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            HttpSession session) {
        if (!isCurrentUserApprover(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "今の承認者だけが見れるよ");
            return "redirect:/todo/mom";
        }

        boolean initialSetup = !momAuthService.isSecretInitialized();
        if (!initialSetup && !isApproverAuthenticated(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "先に合言葉を入力してね");
            return "redirect:/todo/mom/enter";
        }

        if (isBlank(form.getNewSecret())) {
            bindingResult.rejectValue("newSecret", "required", "新しい合言葉を入力してね");
        }
        if (isBlank(form.getConfirmSecret())) {
            bindingResult.rejectValue("confirmSecret", "required", "確認用を入力してね");
        }
        if (!initialSetup && isBlank(form.getCurrentSecret())) {
            bindingResult.rejectValue("currentSecret", "required", "今の合言葉を入力してね");
        }
        if (!isBlank(form.getNewSecret()) && !form.getNewSecret().equals(form.getConfirmSecret())) {
            bindingResult.rejectValue("confirmSecret", "mismatch", "確認用が一致しません");
        }
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.momSecretForm", bindingResult);
            redirectAttributes.addFlashAttribute("momSecretForm", form);
            return "redirect:/todo/mom/secret";
        }

        boolean updated;
        if (initialSetup) {
            updated = momAuthService.initialize(form.getNewSecret());
        } else {
            updated = momAuthService.change(form.getCurrentSecret(), form.getNewSecret());
        }
        if (!updated) {
            redirectAttributes.addFlashAttribute("errorMessage", "今の合言葉がちがうか、新しい合言葉が空です");
            return "redirect:/todo/mom/secret";
        }
        setApproverAuthenticated(session, true);
        redirectAttributes.addFlashAttribute("successMessage", initialSetup ? "合言葉を設定したよ" : "合言葉を変更したよ");
        return "redirect:/todo/mom";
    }

    @PostMapping("/mom/reset-approver")
    public String resetApprover(@Valid @ModelAttribute("approverResetForm") ApproverResetForm form,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {
        if (!isCurrentUserApprover(session) || !isApproverAuthenticated(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "承認者だけが変更できるよ");
            return "redirect:/todo/mom";
        }
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.approverResetForm", bindingResult);
            redirectAttributes.addFlashAttribute("approverResetForm", form);
            return "redirect:/todo/mom/secret";
        }

        FamilyAssignee nextApprover = FamilyAssignee.fromInput(form.getNextApprover()).orElse(null);
        if (nextApprover == null || !momAuthService.resetApprover(nextApprover.code())) {
            redirectAttributes.addFlashAttribute("errorMessage", "承認者の変更に失敗したよ");
            return "redirect:/todo/mom/secret";
        }
        session.setAttribute(SESSION_CURRENT_USER, nextApprover.code());
        clearApproverAuthentication(session);
        redirectAttributes.addFlashAttribute("successMessage", nextApprover.label() + "が新しい承認者になったよ。合言葉を設定してね。");
        return "redirect:/todo/mom/secret";
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
        if (!isCurrentUserApprover(session) || !isApproverAuthenticated(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "承認者だけがOKできます");
            return "redirect:/todo";
        }
        if (todoService.approveDelete(id)) {
            redirectAttributes.addFlashAttribute("successMessage", "けしていいよ！履歴にうつしたよ");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "OKできませんでした");
        }
        return "redirect:/todo/mom";
    }

    private boolean isCurrentUserApprover(HttpSession session) {
        return momAuthService.isApprover(resolveCurrentUser(session).code());
    }

    private boolean isApproverAuthenticated(HttpSession session) {
        Object inSession = session.getAttribute(SESSION_APPROVER_AUTHENTICATED);
        return inSession instanceof Boolean authed && authed;
    }

    private void setApproverAuthenticated(HttpSession session, boolean authenticated) {
        session.setAttribute(SESSION_APPROVER_AUTHENTICATED, authenticated);
    }

    private void clearApproverAuthentication(HttpSession session) {
        setApproverAuthenticated(session, false);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private FamilyAssignee resolveCurrentUser(HttpSession session) {
        Object inSession = session.getAttribute(SESSION_CURRENT_USER);
        if (inSession instanceof String code) {
            return FamilyAssignee.fromInput(code).orElse(FamilyAssignee.ME);
        }
        session.setAttribute(SESSION_CURRENT_USER, FamilyAssignee.ME.code());
        return FamilyAssignee.ME;
    }
}
