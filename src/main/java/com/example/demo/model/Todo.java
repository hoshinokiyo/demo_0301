package com.example.demo.model;

import lombok.Data;

@Data
public class Todo {
    private Long id;
    private String title;
    private String assignee;
    private Boolean completed;

    public String getAssigneeCode() {
        return FamilyAssignee.fromInput(assignee).map(FamilyAssignee::code).orElse("");
    }

    public String getAssigneeLabel() {
        return FamilyAssignee.fromInput(assignee).map(FamilyAssignee::label).orElse(assignee);
    }
}
