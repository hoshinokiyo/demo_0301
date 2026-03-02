package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.mapper.TodoMapper;
import com.example.demo.model.Todo;

@Service
public class TodoService {

    private final TodoMapper todoMapper;

    public TodoService(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    public List<Todo> findAll() {
        return todoMapper.findAll();
    }

    public void create(String title) {
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setCompleted(false);
        todoMapper.insert(todo);
    }

    public boolean deleteById(Long id) {
        return todoMapper.deleteById(id) > 0;
    }
}
