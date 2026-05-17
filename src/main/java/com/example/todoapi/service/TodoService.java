package com.example.todoapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.todoapi.model.AppUser;
import com.example.todoapi.exception.TodoNotFoundException;
import com.example.todoapi.mapper.TodoMapper;
import com.example.todoapi.model.Todo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TodoService {
    private static final String DEFAULT_PRIORITY = "MEDIUM";

    private final TodoMapper todoMapper;
    private final AppUserService appUserService;

    public TodoService(TodoMapper todoMapper, AppUserService appUserService) {
        this.todoMapper = todoMapper;
        this.appUserService = appUserService;
    }

    public List<Todo> findAll(String username) {
        AppUser user = appUserService.findByUsername(username);
        return todoMapper.selectList(new LambdaQueryWrapper<Todo>()
                .eq(Todo::getUserId, user.getId())
                .orderByAsc(Todo::getId));
    }

    @Transactional
    public Todo create(String title, String priority, String username) {
        AppUser user = appUserService.findByUsername(username);
        Todo newTodo = new Todo(null, title.trim(), false, normalizePriority(priority), user.getId());
        todoMapper.insert(newTodo);
        return newTodo;
    }

    @Transactional
    public Todo markDone(Long id, String username) {
        Todo todo = findById(id, username);
        todo.setDone(true);
        todoMapper.updateById(todo);
        return todo;
    }

    @Transactional
    public Todo update(Long id, String title, String priority, String username) {
        Todo todo = findById(id, username);
        todo.setTitle(title.trim());
        if (priority != null && !priority.isBlank()) {
            todo.setPriority(normalizePriority(priority));
        }
        todoMapper.updateById(todo);
        return todo;
    }

    @Transactional
    public void delete(Long id, String username) {
        Todo todo = findById(id, username);
        todoMapper.deleteById(todo.getId());
    }

    public void clear() {
        todoMapper.delete(null);
    }

    private Todo findById(Long id, String username) {
        AppUser user = appUserService.findByUsername(username);
        Todo todo = todoMapper.selectOne(new LambdaQueryWrapper<Todo>()
                .eq(Todo::getId, id)
                .eq(Todo::getUserId, user.getId())
                .last("LIMIT 1"));
        if (todo == null) {
            throw new TodoNotFoundException(id);
        }
        return todo;
    }

    private String normalizePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return DEFAULT_PRIORITY;
        }
        return priority.trim().toUpperCase();
    }
}
