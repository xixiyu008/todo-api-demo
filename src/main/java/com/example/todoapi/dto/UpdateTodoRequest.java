package com.example.todoapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateTodoRequest {

    @NotBlank(message = "title must not be blank")
    @Size(max = 120, message = "title must be at most 120 characters")
    private String title;

    @Pattern(regexp = "HIGH|MEDIUM|LOW", message = "priority must be one of HIGH, MEDIUM, LOW")
    private String priority;

    public UpdateTodoRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
