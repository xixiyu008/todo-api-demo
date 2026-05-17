package com.example.todoapi.controller;

import com.example.todoapi.dto.CreateTodoRequest;
import com.example.todoapi.dto.UpdateTodoRequest;
import com.example.todoapi.factory.ApiResponseFactory;
import com.example.todoapi.mapper.TodoResponseMapper;
import com.example.todoapi.model.Todo;
import com.example.todoapi.service.TodoService;
import com.example.todoapi.vo.ApiResponse;
import com.example.todoapi.vo.TodoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;
    private final TodoResponseMapper todoResponseMapper;
    private final ApiResponseFactory apiResponseFactory;

    public TodoController(
            TodoService todoService,
            TodoResponseMapper todoResponseMapper,
            ApiResponseFactory apiResponseFactory
    ) {
        this.todoService = todoService;
        this.todoResponseMapper = todoResponseMapper;
        this.apiResponseFactory = apiResponseFactory;
    }

    @GetMapping
    public ApiResponse<List<TodoResponse>> getTodos(Authentication authentication) {
        List<TodoResponse> todos = todoResponseMapper.toResponseList(todoService.findAll(authentication.getName()));
        return apiResponseFactory.ok("Todos fetched successfully", todos);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TodoResponse> createTodo(@Valid @RequestBody CreateTodoRequest request, Authentication authentication) {
        Todo todo = todoService.create(request.getTitle(), request.getPriority(), authentication.getName());
        return apiResponseFactory.created("Todo created successfully", todoResponseMapper.toResponse(todo));
    }

    @PutMapping("/{id}/done")
    public ApiResponse<TodoResponse> markTodoDone(@PathVariable Long id, Authentication authentication) {
        Todo todo = todoService.markDone(id, authentication.getName());
        return apiResponseFactory.ok("Todo marked as done", todoResponseMapper.toResponse(todo));
    }

    @PutMapping("/{id}")
    public ApiResponse<TodoResponse> updateTodo(@PathVariable Long id, @Valid @RequestBody UpdateTodoRequest request, Authentication authentication) {
        Todo todo = todoService.update(id, request.getTitle(), request.getPriority(), authentication.getName());
        return apiResponseFactory.ok("Todo updated successfully", todoResponseMapper.toResponse(todo));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTodo(@PathVariable Long id, Authentication authentication) {
        todoService.delete(id, authentication.getName());
    }
}
