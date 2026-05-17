package com.example.todoapi.mapper;

import com.example.todoapi.model.Todo;
import com.example.todoapi.vo.TodoResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TodoResponseMapper {

    public TodoResponse toResponse(Todo todo) {
        return new TodoResponse(todo.getId(), todo.getTitle(), todo.isDone(), todo.getPriority());
    }

    public List<TodoResponse> toResponseList(List<Todo> todos) {
        return todos.stream()
                .map(this::toResponse)
                .toList();
    }
}
