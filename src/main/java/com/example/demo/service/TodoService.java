package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.form.TodoForm;
import com.example.demo.mapper.TodoMapper;
import com.example.demo.model.FamilyAssignee;
import com.example.demo.model.Todo;
import com.example.demo.model.TodoStatus;

@Service
public class TodoService {

    private final TodoMapper todoMapper;

    public TodoService(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    public List<Todo> findAll() {
        List<Todo> todos = todoMapper.findAll();
        todos.forEach(this::normalizeFields);
        return todos;
    }

    public List<Todo> findDeleteRequested() {
        return findAll().stream()
                .filter(t -> TodoStatus.DELETE_REQUESTED.code().equals(t.getStatus()))
                .toList();
    }

    public List<Todo> findHistory() {
        List<Todo> todos = todoMapper.findHistory();
        todos.forEach(this::normalizeFields);
        return todos;
    }

    public Todo findById(Long id) {
        Todo todo = todoMapper.findById(id);
        if (todo != null) {
            normalizeFields(todo);
        }
        return todo;
    }

    public void create(TodoForm form) {
        FamilyAssignee assignee = FamilyAssignee.fromInput(form.getAssignee())
                .orElseThrow(() -> new IllegalArgumentException("Assignee is required"));
        if (form.getTitle() == null || form.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (form.getCategory() == null || form.getCategory().isBlank()) {
            throw new IllegalArgumentException("Category is required");
        }
        if (form.getDeadline() == null) {
            throw new IllegalArgumentException("Deadline is required");
        }
        Todo todo = new Todo();
        todo.setTitle(form.getTitle().trim());
        todo.setAssignee(assignee.code());
        todo.setCategory(form.getCategory().trim());
        todo.setDeadline(form.getDeadline());
        todo.setStatus(TodoStatus.ACTIVE.code());
        todo.setDeletedAt(null);
        todo.setCompleted(false);
        todoMapper.insert(todo);
    }

    public boolean update(Long id, String title) {
        Todo todo = todoMapper.findById(id);
        if (todo == null) {
            return false;
        }
        normalizeFields(todo);
        if (!TodoStatus.ACTIVE.code().equals(todo.getStatus())) {
            return false;
        }
        todo.setTitle(title);
        return todoMapper.update(todo) > 0;
    }

    public boolean toggleCompleted(Long id) {
        Todo todo = todoMapper.findById(id);
        if (todo == null) {
            return false;
        }
        normalizeFields(todo);
        if (!TodoStatus.ACTIVE.code().equals(todo.getStatus())) {
            return false;
        }
        todo.setCompleted(!Boolean.TRUE.equals(todo.getCompleted()));
        return todoMapper.update(todo) > 0;
    }

    public boolean requestDelete(Long id) {
        return todoMapper.requestDelete(id) > 0;
    }

    public boolean approveDelete(Long id) {
        return todoMapper.approveDelete(id, LocalDateTime.now()) > 0;
    }

    public boolean deleteById(Long id) {
        return todoMapper.deleteById(id) > 0;
    }

    private void normalizeFields(Todo todo) {
        FamilyAssignee.fromInput(todo.getAssignee()).ifPresent(v -> todo.setAssignee(v.code()));
        todo.setStatus(TodoStatus.fromCode(todo.getStatus()).code());
    }
}
