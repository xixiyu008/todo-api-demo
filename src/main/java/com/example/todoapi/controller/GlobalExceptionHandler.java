package com.example.todoapi.controller;

import com.example.todoapi.exception.TodoNotFoundException;
import com.example.todoapi.exception.UserAlreadyExistsException;
import com.example.todoapi.exception.UserNotFoundException;
import com.example.todoapi.exception.UserOperationNotAllowedException;
import com.example.todoapi.factory.ApiResponseFactory;
import com.example.todoapi.vo.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ApiResponseFactory apiResponseFactory;

    public GlobalExceptionHandler(ApiResponseFactory apiResponseFactory) {
        this.apiResponseFactory = apiResponseFactory;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidationException(MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return apiResponseFactory.validationError(errors);
    }

    @ExceptionHandler(TodoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleTodoNotFoundException(TodoNotFoundException exception) {
        return apiResponseFactory.todoNotFound(exception.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleAuthenticationException(AuthenticationException exception) {
        if (exception instanceof DisabledException) {
            return apiResponseFactory.unauthorized("用户已被禁用");
        }
        if (exception instanceof BadCredentialsException || exception instanceof AuthenticationCredentialsNotFoundException) {
            return apiResponseFactory.unauthorized("用户名或密码错误");
        }
        return apiResponseFactory.unauthorized("用户名或密码错误");
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleUserAlreadyExistsException(UserAlreadyExistsException exception) {
        return apiResponseFactory.conflict(exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleUserNotFoundException(UserNotFoundException exception) {
        return apiResponseFactory.userNotFound(exception.getMessage());
    }

    @ExceptionHandler(UserOperationNotAllowedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleUserOperationNotAllowedException(UserOperationNotAllowedException exception) {
        return apiResponseFactory.badRequest("USER_OPERATION_NOT_ALLOWED", exception.getMessage());
    }
}
