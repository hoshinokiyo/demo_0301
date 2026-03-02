package com.example.demo.model;

import lombok.Data;

@Data
public class Todo {
    private Long id;
    private String title;
    private String assignee;
    private Boolean completed;
}
