package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.mapper.TodoMapper;
import com.example.demo.model.FamilyAssignee;
import com.example.demo.model.Todo;

@Service
public class TodoService {

    private final TodoMapper todoMapper;

    public TodoService(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    public List<Todo> findAll() {
        List<Todo> todos = todoMapper.findAll();
        todos.forEach(this::normalizeAssignee);
        return todos;
    }

    public Todo findById(Long id) {
        Todo todo = todoMapper.findById(id);
        if (todo != null) {
            normalizeAssignee(todo);
        }
        return todo;
    }

    public void create(String title, String assigneeInput) {
        FamilyAssignee assignee = FamilyAssignee.fromInput(assigneeInput)
                .orElseThrow(() -> new IllegalArgumentException("Assignee is required"));
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        Todo todo = new Todo();
        todo.setTitle(title.trim());
        todo.setAssignee(assignee.code());
        todo.setCompleted(false);
        todoMapper.insert(todo);
    }

    public boolean update(Long id, String title) {
        Todo todo = todoMapper.findById(id);
        if (todo == null) {
            return false;
        }
        normalizeAssignee(todo);
        todo.setTitle(title);
        return todoMapper.update(todo) > 0;
    }

    public boolean toggleCompleted(Long id) {
        Todo todo = todoMapper.findById(id);
        if (todo == null) {
            return false;
        }
        normalizeAssignee(todo);
        todo.setCompleted(!Boolean.TRUE.equals(todo.getCompleted()));
        return todoMapper.update(todo) > 0;
    }

    public boolean deleteById(Long id) {
        return todoMapper.deleteById(id) > 0;
    }

    private void normalizeAssignee(Todo todo) {
        FamilyAssignee.fromInput(todo.getAssignee()).ifPresent(v -> todo.setAssignee(v.code()));
    }
}
