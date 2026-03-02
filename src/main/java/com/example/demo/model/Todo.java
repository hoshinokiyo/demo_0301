package com.example.demo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Todo {
    private Long id;
    private String title;
    private String assignee;
    private String category;
    private LocalDate deadline;
    private Boolean completed;
    private String status;
    private LocalDateTime deletedAt;

    public String getAssigneeCode() {
        return FamilyAssignee.fromInput(assignee).map(FamilyAssignee::code).orElse("");
    }

    public String getAssigneeLabel() {
        return FamilyAssignee.fromInput(assignee).map(FamilyAssignee::label).orElse(assignee);
    }

    public String getStatusCode() {
        return TodoStatus.fromCode(status).code();
    }

    public String getStatusLabel() {
        return TodoStatus.fromCode(status).label();
    }
}
