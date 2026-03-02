package com.example.demo.form;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TodoForm {
    @NotBlank(message = "タイトルは必須です")
    private String title;

    @NotBlank(message = "担当者は必須です")
    private String assignee;

    @NotBlank(message = "カテゴリは必須です")
    private String category;

    @NotNull(message = "期限は必須です")
    private LocalDate deadline;
}
